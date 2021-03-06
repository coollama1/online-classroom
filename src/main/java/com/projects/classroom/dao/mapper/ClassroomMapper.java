package com.projects.classroom.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.projects.classroom.model.Classroom;

public class ClassroomMapper implements RowMapper<Classroom>{
	private static final String CLASS_ID_LABEL = "class_id";
	private static final String MAIN_TEACHER_ID_LABEL = "main_teacher_id";
	private static final String NAME_LABEL = "name";
	private static final String DESCRIPTION_LABEL = "description";
	
	@Override
	public Classroom mapRow(ResultSet rs, int rowNum) throws SQLException {
		Classroom classroom = new Classroom();
		classroom.setClassId(rs.getLong(CLASS_ID_LABEL));
		classroom.setMainTeacherId(rs.getLong(MAIN_TEACHER_ID_LABEL));
		classroom.setName(rs.getString(NAME_LABEL));
		classroom.setDescription(rs.getString(DESCRIPTION_LABEL));
		return classroom;
	}
}
