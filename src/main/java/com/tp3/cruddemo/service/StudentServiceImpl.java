package com.tp3.cruddemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tp3.cruddemo.dao.StudentDAO;
import com.tp3.cruddemo.entity.Student;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentDAO studentDAO;

    @Autowired
    public StudentServiceImpl(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    @Override
    @Transactional
    public void save(Student theStudent) {
        studentDAO.save(theStudent);
    }

    @Override
    public Student findById(Integer id) {
        return studentDAO.findById(id);
    }

    @Override
    public List<Student> findAll() {
        return studentDAO.findAll();
    }

    @Override
    public List<Student> findByLastName(String theLastName) {
        return studentDAO.findByLastName(theLastName);
    }

    @Override
    @Transactional
    public void update(Student theStudent) {
        studentDAO.update(theStudent);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        return studentDAO.delete(id);
    }

    @Override
    @Transactional
    public int deleteAll() {
        return studentDAO.deleteAll();
    }

    @Override
    @Transactional
    public int updateEmailByLastName(String email, String lastName) {
        return studentDAO.updateEmailByLastName(email, lastName);
    }

    @Override
    @Transactional
    public void createTwoAndUpdateFirstName(Student first, Student second, String newFirstName) {
        studentDAO.save(first);
        studentDAO.save(second);
        second.setFirstName(newFirstName);
        studentDAO.update(second);
    }
}
