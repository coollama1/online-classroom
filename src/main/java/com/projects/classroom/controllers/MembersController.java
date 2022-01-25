package com.projects.classroom.controllers;

import static com.projects.classroom.utilities.Utilities.checkSessionForClassroom;
import static com.projects.classroom.utilities.Utilities.checkSessionForStudent;
import static com.projects.classroom.utilities.Utilities.checkSessionForTeacher;
import static com.projects.classroom.utilities.Utilities.getClassroomFromSession;

import java.io.InvalidObjectException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.projects.classroom.exceptions.DatabaseOperationException;
import com.projects.classroom.models.AssignmentGrade;
import com.projects.classroom.models.Classroom;
import com.projects.classroom.models.Submission;
import com.projects.classroom.models.Teacher;
import com.projects.classroom.models.User;
import com.projects.classroom.services.AssignmentGradeService;
import com.projects.classroom.services.ClassroomService;
import com.projects.classroom.services.StudentService;
import com.projects.classroom.services.SubmissionService;
import com.projects.classroom.services.TeacherService;
import com.projects.classroom.services.UserService;

@Controller
public class MembersController {

    @Autowired
    UserService userService;
    
    @Autowired
    StudentService studentService;
    
    @Autowired 
    TeacherService teacherService;
    
    @Autowired
    ClassroomService classroomService;
    
    @Autowired
    SubmissionService submissionService;
    
    @Autowired
    AssignmentGradeService gradeService;
    
    private static final Logger logger = LoggerFactory.getLogger(MembersController.class);
    
    @GetMapping(value="members")
    public String viewMembersPage(Model model, HttpSession session) {
        if(!checkSessionForClassroom(session)) {
            return "redirect:/home";
        }
        Classroom classroom = getClassroomFromSession(session);
        try {
            Teacher headTeacher = teacherService.get(classroom.getMainTeacherId());
            List<User> users = userService.getUsersByClassId(classroom.getClassId());
            List<User> students  = users
                                        .stream()
                                        .filter(e -> studentService.checkStudentExists(e.getUsername()))
                                        .collect(Collectors.toList());
            List<User> teachers = users
                                    .stream()
                                    .filter(e -> teacherService.checkTeacherExists(e.getUsername()) && e.getUserId() != headTeacher.getUserId())
                                    .collect(Collectors.toList());
            model.addAttribute("listOfStudents",students);
            model.addAttribute("listOfTeachers",teachers);
            model.addAttribute("headTeacher", headTeacher);
        } catch (InvalidObjectException | DatabaseOperationException ex) {
            logger.error(ex.toString());
            model.addAttribute("errorOccured",true);
        }

        if(checkSessionForStudent(session)) {
            return "members-student";
        }
        else if(checkSessionForTeacher(session)) {
            return "members-teacher";
        }
        return "redirect:/";
    }
    
    @GetMapping(value = "remove-{userId}")
    public String removeUser(Model model,
            @PathVariable(value="userId")long userId, HttpSession session) {
        if(!checkSessionForClassroom(session)) {
            return "redirect:/home";
        }
        Classroom classroom = getClassroomFromSession(session);
        try {
            long studentId = studentService.getStudentByUserId(userId).getStudentId();
            List<Submission> submissions = submissionService.getSubmissionsByStudentId(studentId);
            List<AssignmentGrade> grades = gradeService.getAssignementGradesByStudentId(studentId);
            submissions.forEach(e -> submissionService.delete(e.getSubmissionId()));
            grades.forEach(e -> gradeService.delete(e.getAssignmentGradeId()));
        } catch (InvalidObjectException | DatabaseOperationException e) {
            logger.error(e.toString());
        }
        classroomService.removeFromClassroom(classroom.getClassId(), userId);;
        return "redirect:/members";
    }
}