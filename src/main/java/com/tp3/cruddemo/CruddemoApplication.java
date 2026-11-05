package com.tp3.cruddemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.tp3.cruddemo.dao.StudentDAO;
import com.tp3.cruddemo.entity.Student;

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
        Student student = studentDAO.findById(1);
        System.out.println("Found student: " + student);
    }
}
