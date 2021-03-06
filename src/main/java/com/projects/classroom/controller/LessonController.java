package com.projects.classroom.controller;

import static com.projects.classroom.utilities.Utilities.checkSessionForClassroom;
import static com.projects.classroom.utilities.Utilities.checkSessionForStudent;
import static com.projects.classroom.utilities.Utilities.checkSessionForTeacher;
import static com.projects.classroom.utilities.Utilities.getClassroomFromSession;

import java.io.InvalidObjectException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projects.classroom.exception.DatabaseOperationException;
import com.projects.classroom.model.Classroom;
import com.projects.classroom.model.Lesson;
import com.projects.classroom.service.LessonService;

@Controller
public class LessonController {

    @Autowired
    LessonService lessonService;
    
    private static final Logger logger = LoggerFactory.getLogger(LessonController.class);
    
    @GetMapping(value="lessons")
    public String viewLessonPage(Model model, HttpSession session) {
        if(!checkSessionForClassroom(session)) {
            return "redirect:/home";
        }
        Classroom classroom = getClassroomFromSession(session);
        try {
            List<Lesson> lessons = lessonService.getLessonsByClassId(classroom.getClassId());
            model.addAttribute("listOfLessons", lessons);
        } catch (InvalidObjectException | DatabaseOperationException e) {
            logger.error(e.toString());
            model.addAttribute("errorOccured",true);
        }
        
        if(checkSessionForStudent(session)) {
            return "lessons-student";
        }
        if(checkSessionForTeacher(session)) {
            return "lessons-teacher";
        }
        return "redirect:/";
    }
    
    @GetMapping(value="create-lesson")
    public String createLesson(Model model,
            @ModelAttribute(value="title") String title,
            @ModelAttribute(value ="content") String content,
            RedirectAttributes attributes,
            HttpSession session) {
        if(!checkSessionForTeacher(session)) {
            return "redirect:/";
        }
        else if(!checkSessionForClassroom(session)) {
            return "redirect:/home";
        }
        Classroom classroom = getClassroomFromSession(session);
        Lesson lesson = new Lesson();
        lesson.setClassId(classroom.getClassId());
        lesson.setTitle(title);
        lesson.setText(content);
        try {
            lessonService.save(lesson);
            return "redirect:/lessons";
        } catch (InvalidObjectException | DatabaseOperationException e) {
            logger.error(e.toString());
            attributes.addFlashAttribute("errorOccured",true);   
        }
        
        return "redirect:/write-post-lesson";
    }
}
