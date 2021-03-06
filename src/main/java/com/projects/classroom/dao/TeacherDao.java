package com.projects.classroom.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.projects.classroom.dao.mapper.TeacherMapper;
import com.projects.classroom.model.Teacher;

@Repository
public class TeacherDao implements Dao<Teacher>{

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	UserDao userDao;
	
	private static final String SELECT_BY_TEACHER_ID_QUERY = 
        "SELECT * FROM teachers INNER JOIN users "
        + "ON users.user_id = teachers.user_id "
        + "WHERE teacher_id=?";
	private static final String SELECT_BY_USER_ID_QUERY = 
        "SELECT * FROM teachers INNER JOIN users "
        + "ON users.user_id = teachers.user_id "
        + "WHERE teachers.user_id=?";
	private static final String SELECT_ALL_QUERY = 
        "SELECT * FROM teachers INNER JOIN users "
        + "ON users.user_id = teachers.user_id";
    private static final String LIMIT = " LIMIT ";
    private static final String OFFSET = " OFFSET ";
    private static final String TOTAL_COUNT_QUERY = "SELECT COUNT(*) FROM teachers";
	
	private static final String INSERT_QUERY = 
        "INSERT INTO public.teachers (user_id, professional_name, description)"
        + "	VALUES((SELECT user_id FROM users WHERE username=?),"
        + "?,?);";
	
	private static final String UPDATE_QUERY = 
        "UPDATE teachers "
        + "SET professional_name = ?, description = ? "
        + "WHERE teacher_id = ?;";
	
	private static final String DELETE_QUERY = "DELETE FROM teachers WHERE teacher_id = ?;";
	
	private static final Logger logger = LoggerFactory.getLogger(TeacherDao.class);
	
    @Override
    public Page<Teacher> getAll(Pageable page) {
        logger.debug(
                "Running query to retrieve all Teachers with provided limit and offset and saving contents into a list");
        List<Teacher> teachers = jdbcTemplate.query(SELECT_ALL_QUERY + LIMIT + page.getPageSize() + OFFSET + page.getOffset(), 
            new TeacherMapper());
        logger.debug("Running query to count the total number of rows in teachers table");
        int totalNumberOfTeachers = jdbcTemplate.queryForObject(TOTAL_COUNT_QUERY, Integer.class);
        logger.debug("Placing all list Teacher elements into a PageImpl object");
        return new PageImpl<Teacher>(teachers, page, totalNumberOfTeachers);
    }
	
	public Teacher getTeacherByUserId(long userId){
	    logger.debug("Running query to retrieve Teacher object by the following userId: " + userId);
	    return jdbcTemplate.queryForObject(SELECT_BY_USER_ID_QUERY, new TeacherMapper(), userId);
	}
	
	@Override
	public Teacher get(long teacherId){
	    logger.debug("Running query to retrieve the Teacher object by teacherId: " + teacherId);
	    return jdbcTemplate.queryForObject(SELECT_BY_TEACHER_ID_QUERY, new TeacherMapper(), teacherId);
	}

	@Override
	public int save(Teacher teacher) {
	    userDao.save(teacher);
	    logger.debug("Running query to save the following Teacher object into the database: " + teacher);
	    return jdbcTemplate.update(INSERT_QUERY,teacher.getUsername(), teacher.getProfessionalName(), teacher.getDescription());
	}

	@Override
	public void update(Teacher teacher) {
	    logger.debug("Running query to update the following Teacher object in the database: " + teacher);
	    jdbcTemplate.update(UPDATE_QUERY,teacher.getProfessionalName(), teacher.getDescription(), teacher.getTeacherId());
	}

	@Override
	public void delete(long teacherId) {
	    logger.debug("Running query to delete Teacher with the following teacherId: " + teacherId);
	    jdbcTemplate.update(DELETE_QUERY, teacherId);
	}

}
