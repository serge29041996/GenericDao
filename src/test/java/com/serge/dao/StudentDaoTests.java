package com.serge.dao;

import com.serge.model.Student;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for student dao.
 */
public class StudentDaoTests {
  private static final String TEST_VALUE = "test";
  private StudentDao studentDao = new StudentDao(TestDbConnector.getConnection());
  private Student newStudent;
  private Student savedStudent;

  @Before
  public void initNewStudent() {
    newStudent = new Student(TEST_VALUE, TEST_VALUE, 1);
  }

  @After
  public void clearNewStudent() {
    studentDao.delete(savedStudent.getId());
  }

  @Test
  public void testSaveStudent() {
    savedStudent = studentDao.save(newStudent);
    Assert.assertNotNull(savedStudent);
  }

  @Test
  public void testGetStudent() {
    savedStudent = studentDao.save(newStudent);
    Student gettingStudent = studentDao.get(savedStudent.getId());
    Assert.assertEquals(savedStudent, gettingStudent);
  }

  @Test
  public void testUpdateStudent() {
    savedStudent = studentDao.save(newStudent);
    savedStudent.setName("Serge");
    studentDao.update(savedStudent);
    Assert.assertEquals(savedStudent, studentDao.get(savedStudent.getId()));
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteStudent() {
    savedStudent = studentDao.save(newStudent);
    studentDao.delete(savedStudent.getId());
    Assert.assertNull(studentDao.get(savedStudent.getId()));
  }

  @Test
  public void testGetAllStudents() {
    List<Student> students = new ArrayList<>();
    savedStudent = studentDao.save(newStudent);
    students.add(savedStudent);
    Student secondNewStudent = new Student("1", "1", 2);
    Student savedSecondStudent = studentDao.save(secondNewStudent);
    students.add(savedSecondStudent);
    List<Student> gettingStudents = studentDao.getAll();
    Assert.assertEquals(students, gettingStudents);
    studentDao.delete(secondNewStudent.getId());
  }
}
