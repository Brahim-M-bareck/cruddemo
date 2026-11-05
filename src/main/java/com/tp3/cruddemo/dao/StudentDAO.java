package com.tp3.cruddemo.dao;

import com.tp3.cruddemo.entity.Student;

import java.util.List;

public interface StudentDAO {

    void save(Student theStudent);

    Student findById(Integer id);

    List<Student> findAll();

    List<Student> findAllOrderByLastNameDesc();

    List<Student> findByLastName(String theLastName);

    void update(Student theStudent);

    boolean delete(Integer id);
}
