package com.serge.dao;

import com.serge.model.Student;
import java.sql.Connection;

/**
 * Realization dao for student.
 */
public class StudentDao extends AbstractDao<Student, Long> {
  public StudentDao(Connection connection) {
    super(connection);
  }
}
