package com.tp3.cruddemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.tp3.cruddemo.dao.StudentDAO;
import com.tp3.cruddemo.entity.Student;

import java.util.List;

@SpringBootApplication
public class CruddemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CruddemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(StudentDAO studentDAO) {
        return args -> {
            String action = args.length > 0 ? args[0] : "read";

            switch (action) {
                case "create" -> createStudents(studentDAO);
                case "read" -> readStudent(studentDAO);
                case "bylastname" -> findStudentsByLastName(studentDAO);
                case "update" -> updateStudent(studentDAO);
                case "delete" -> deleteStudent(studentDAO);
                default -> System.out.println("Action inconnue: " + action);
            }
        };
    }

    private void createStudents(StudentDAO studentDAO) {
        Student paul = new Student("Paul", "Doe", "paul@test.com");
        Student mary = new Student("Mary", "Public", "mary@test.com");
        Student susan = new Student("Susan", "Doe", "susan@test.com");

        studentDAO.save(paul);
        System.out.println("Saved student. Generated id: " + paul.getId());

        studentDAO.save(mary);
        System.out.println("Saved student. Generated id: " + mary.getId());

        studentDAO.save(susan);
        System.out.println("Saved student. Generated id: " + susan.getId());
    }

    private void readStudent(StudentDAO studentDAO) {
        List<Student> students = studentDAO.findAll();
        if (students.isEmpty()) {
            System.out.println("No students found. Run the create action first.");
            return;
        }

        Student first = students.get(0);
        System.out.println("First student (id " + first.getId() + ") via findById: "
                + studentDAO.findById(first.getId()));

        System.out.println("All students (ORDER BY lastName):");
        for (Student student : students) {
            System.out.println(student);
        }

        System.out.println("Students with lastName 'Doe':");
        for (Student student : studentDAO.findByLastName("Doe")) {
            System.out.println(student);
        }
    }

    private void findStudentsByLastName(StudentDAO studentDAO) {
        System.out.println("Students with lastName 'Doe':");
        for (Student student : studentDAO.findByLastName("Doe")) {
            System.out.println(student);
        }
    }

    private void updateStudent(StudentDAO studentDAO) {
        List<Student> students = studentDAO.findAll();
        if (students.isEmpty()) {
            System.out.println("No students found. Run the create action first.");
            return;
        }

        Student student = students.get(0);
        student.setFirstName("John");
        studentDAO.update(student);

        System.out.println("Updated student: " + studentDAO.findById(student.getId()));
    }

    private void deleteStudent(StudentDAO studentDAO) {
        List<Student> students = studentDAO.findAll();
        if (students.isEmpty()) {
            System.out.println("No students found. Run the create action first.");
        } else {
            Student student = students.get(0);
            int id = student.getId();
            boolean deleted = studentDAO.delete(id);
            System.out.println("Deleted student with id " + id + ": " + deleted);
            System.out.println("After delete, findById(" + id + ") = " + studentDAO.findById(id));
        }

        boolean deletedMissing = studentDAO.delete(9999);
        System.out.println("delete(9999) -> " + deletedMissing + " (no student with this id, nothing deleted)");
    }
}
