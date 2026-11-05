package com.tp3.cruddemo.service;

import com.tp3.cruddemo.entity.Student;

import java.util.List;

public interface StudentService {

    void save(Student theStudent);

    Student findById(Integer id);

    List<Student> findAll();

    List<Student> findByLastName(String theLastName);

    void update(Student theStudent);

    boolean delete(Integer id);

    int deleteAll();

    int updateEmailByLastName(String email, String lastName);

    void createTwoAndUpdateFirstName(Student first, Student second, String newFirstName);
}
