package com.projects.classroom.controllers;

import static com.projects.classroom.utilities.Utilities.checkSessionForClassroom;
import static com.projects.classroom.utilities.Utilities.checkSessionForTeacher;
import static com.projects.classroom.utilities.Utilities.getClassroomFromSession;

import java.io.InvalidObjectException;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projects.classroom.exceptions.DatabaseOperationException;
import com.projects.classroom.models.Announcement;
import com.projects.classroom.models.Classroom;
import com.projects.classroom.services.AnnouncementService;

@Controller
public class AnnouncementController {

    @Autowired
    AnnouncementService announcementService;
    
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementController.class);
    
    @GetMapping(value="create-announcement")
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
        Announcement announcement = new Announcement();
        announcement.setClassId(classroom.getClassId());
        announcement.setTitle(title);
        announcement.setText(content);
        try {
            announcementService.save(announcement);
            return "redirect:/classroom";
        } catch (InvalidObjectException | DatabaseOperationException e) {
            logger.error(e.toString());
            attributes.addFlashAttribute("errorOccured",true);   
        }
        
        return "redirect:/write-post-announcement";
    }
}
