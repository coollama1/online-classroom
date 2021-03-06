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
import com.projects.classroom.model.Assignment;
import com.projects.classroom.model.Classroom;
import com.projects.classroom.service.AssignmentService;
@Controller

public class AssignmentController {
    
    @Autowired
    AssignmentService assignmentService;
    
    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);
    
    @GetMapping(value = "/assignments")
    public String viewAnnouncementPage(Model model, HttpSession session) {
        if(!checkSessionForClassroom(session)) {
            return "redirect:/home";
        }
        Classroom classroom = getClassroomFromSession(session);
        try {
            List<Assignment> assignments = assignmentService.getAssignementsByClassId(classroom.getClassId());
            model.addAttribute("listOfAssignments",assignments);
        } catch (InvalidObjectException | DatabaseOperationException e) {
            logger.error(e.toString());
            model.addAttribute("errorOccured",true);
        }
        if(checkSessionForStudent(session)) {
            return "assignments-student";
        }
        if(checkSessionForTeacher(session)) {
            return "assignments-teacher";
        }
        return "redirect:/";
    }
    
    @GetMapping(value="create-assignment")
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
        Assignment assignment = new Assignment();
        assignment.setClassId(classroom.getClassId());
        assignment.setTitle(title);
        assignment.setDescription(content);
        try {
            assignmentService.save(assignment);
            return "redirect:/assignments";
        } catch (InvalidObjectException | DatabaseOperationException e) {
            logger.error(e.toString());
            attributes.addFlashAttribute("errorOccured",true);   
        }
        
        return "redirect:/write-post-assignment";
    }
}
