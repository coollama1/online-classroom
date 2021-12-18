package dev.andrylat.app.services;

import java.io.InvalidObjectException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.andrylat.app.daos.ClassroomDao;
import dev.andrylat.app.exceptions.DatabaseOperationException;
import dev.andrylat.app.models.Classroom;
import dev.andrylat.app.utilities.Utilities;

@Service
public class ClassroomService {

    @Autowired
    private ClassroomDao classroomDao;

    private static final String CLASS_ID_ERROR_MESSAGE = "";
    private static final String GET_ALL_ERROR_MESSAGE = "";
    private static final String SAVE_ERROR_MESSAGE = "";
    private static final String UPDATE_ERROR_MESSAGE = "";
    private static final String INVALID_OBJECT_ERROR_MESSAGE = "";
    private static final String NEW_LINE = "";
    private static final String GET_CLASSES_BY_MAIN_TEACHER_ID_ERROR_MESSAGE = "";
    private static final String NULL_CLASSROOM = "";

    private static final Logger logger = LoggerFactory.getLogger(ClassroomService.class);

    private void validate(Classroom classroom) throws InvalidObjectException {
        List<String> violations = Utilities.validate(classroom);
        if (classroom == null) {
            logger.error("Classroom object cannot be null. Invalid state");
            throw new InvalidObjectException(NULL_CLASSROOM);
        }
        logger.debug("Validating the following classroom object: " + classroom);
        if (!violations.isEmpty()) {
            String violationMessages = violations.stream().collect(Collectors.joining(NEW_LINE));
            logger.error("Validation of the Classroom object has failed due the following:\n" + violationMessages);
            throw new InvalidObjectException(violationMessages + INVALID_OBJECT_ERROR_MESSAGE + classroom.getClassId());
        }
    }

    public Classroom get(long classId) throws DatabaseOperationException, InvalidObjectException {
        Classroom classroom = null;
        try {
            logger.debug("Attempting to retrieve Classroom object from the database with an classId = " + classId);
            classroom = classroomDao.get(classId);
        } catch (DataAccessException e) {
            logger.error(
                    "The Classroom could not be found in the database. Check the records to make sure the following classId is present in the classes table: "
                            + classId);
            throw new DatabaseOperationException(CLASS_ID_ERROR_MESSAGE);
        }
        return classroom;
    }

    public List<Classroom> getClassesByMainTeacherId(long mainTeacherId)
            throws DatabaseOperationException, InvalidObjectException {
        List<Classroom> classrooms = Collections.EMPTY_LIST;
        try {
            logger.debug(
                    "Attempting to retrieve a list of all Classrooms that have the mainTeacherId = " + mainTeacherId);
            classrooms = classroomDao.getClassesByMainTeacherId(mainTeacherId);
        } catch (DataAccessException e) {
            logger.error(
                    "No Classrooms with the mainTeacherId were found. Please check the classes table in the database");
            throw new DatabaseOperationException(GET_CLASSES_BY_MAIN_TEACHER_ID_ERROR_MESSAGE);
        }
        logger.debug("Validating each Classroom object in the list");
        for (Classroom classroom : classrooms) {
            validate(classroom);
        }
        return classrooms;
    }

    public Page<Classroom> getAll(Pageable page) throws DatabaseOperationException, InvalidObjectException {
        Page<Classroom> classrooms = new PageImpl<>(Collections.EMPTY_LIST);
        try {
            logger.debug("Attempting to retrieve a page of all of the Classrooms");
            classrooms = classroomDao.getAll(page);
        } catch (DataAccessException e) {
            logger.error("Failed to retrieve a page of all Classrooms. Check the classes table in the database");
            throw new DatabaseOperationException(GET_ALL_ERROR_MESSAGE);
        }
        logger.debug("Validating each Classroom object in the page");
        for (Classroom classroom : classrooms) {
            validate(classroom);
        }
        return classrooms;
    }

    public int save(Classroom classroom) throws InvalidObjectException, DatabaseOperationException {
        logger.debug("Attempting to update the following Classroom object in the database: " + classroom);
        int output = 0;
        validate(classroom);
        try {
            output = classroomDao.save(classroom);
        } catch (DataAccessException e) {
            logger.error(
                    "Failed to save the Classroom. Check the database to make sure the classes table is properly initialized");
            throw new DatabaseOperationException(SAVE_ERROR_MESSAGE);
        }
        return output;
    }

    public void update(Classroom classroom) throws InvalidObjectException, DatabaseOperationException {
        logger.debug("Attempting to update the following Classroom object in the database: " + classroom);
        validate(classroom);
        try {
            classroomDao.update(classroom);
        } catch (DataAccessException e) {
            logger.error("Failed to perform update. Check the Classroom object and classes table in the database");
            throw new DatabaseOperationException(UPDATE_ERROR_MESSAGE + classroom.getClassId());
        }
    }

    public void delete(long classId) throws DatabaseOperationException {
        logger.debug("Attempting to delete Classroom with classId = " + classId);
        try {
            classroomDao.delete(classId);
        } catch (DataAccessException e) {
            logger.error(
                    "Classroom deletion failed. Check the classes table in the database and make sure the correct class_id is used");
            throw new DatabaseOperationException(CLASS_ID_ERROR_MESSAGE);
        }
    }
}
