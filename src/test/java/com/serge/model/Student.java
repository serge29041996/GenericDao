package com.serge.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Test entity of student.
 */
@Entity
@Table(name = "STUDENTS")
public class Student {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "COURSE")
  private int course;

  private Student() {
  }

  public Student(String name, String password, int course) {
    this.name = name;
    this.password = password;
    this.course = course;
  }

  public Student(Long id, String name, String password, int course) {
    this(name, password, course);
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getCourse() {
    return course;
  }

  public void setCourse(int course) {
    this.course = course;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Student student = (Student) o;

    if (course != student.course) {
      return false;
    }
    if (!name.equals(student.name)) {
      return false;
    }
    return password.equals(student.password);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + password.hashCode();
    result = 31 * result + course;
    return result;
  }
}
