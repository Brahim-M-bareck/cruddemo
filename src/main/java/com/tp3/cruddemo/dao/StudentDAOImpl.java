package com.tp3.cruddemo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tp3.cruddemo.entity.Student;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Repository
public class StudentDAOImpl implements StudentDAO {

    private final EntityManager entityManager;

    @Autowired
    public StudentDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(Student theStudent) {
        entityManager.persist(theStudent);
    }

    @Override
    public Student findById(Integer id) {
        return entityManager.find(Student.class, id);
    }

    @Override
    public List<Student> findAll() {
        TypedQuery<Student> theQuery = entityManager.createQuery("FROM Student ORDER BY lastName", Student.class);
        return theQuery.getResultList();
    }

    @Override
    public List<Student> findAllOrderByLastNameDesc() {
        TypedQuery<Student> theQuery = entityManager.createQuery("FROM Student ORDER BY lastName DESC", Student.class);
        return theQuery.getResultList();
    }

    @Override
    public List<Student> findByLastName(String theLastName) {
        TypedQuery<Student> theQuery = entityManager.createQuery("FROM Student WHERE lastName = :theData", Student.class);
        theQuery.setParameter("theData", theLastName);
        return theQuery.getResultList();
    }

    @Override
    @Transactional
    public void update(Student theStudent) {
        entityManager.merge(theStudent);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        Student theStudent = entityManager.find(Student.class, id);
        if (theStudent == null) {
            return false;
        }
        entityManager.remove(theStudent);
        return true;
    }

    @Override
    @Transactional
    public int deleteAll() {
        return entityManager.createQuery("DELETE FROM Student").executeUpdate();
    }

    @Override
    @Transactional
    public int updateEmailByLastName(String email, String lastName) {
        return entityManager.createQuery("UPDATE Student SET email = :em WHERE lastName = :ln")
                .setParameter("em", email)
                .setParameter("ln", lastName)
                .executeUpdate();
    }
}
