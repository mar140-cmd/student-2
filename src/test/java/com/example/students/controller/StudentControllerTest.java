package com.example.students.controller;

import com.example.students.model.Student;
import com.example.students.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Fausse implementation du service sans Mockito
    @TestConfiguration
    static class FakeServiceConfig {

        @Bean
        @Primary
        public StudentService studentService() {
            return new StudentService(null) {

                private final Student s1 = new Student(1L, "Ahmed", "Ben Ali", "ahmed@email.com", 21, "Informatique");
                private final Student s2 = new Student(2L, "Fatima", "Zahra", "fatima@email.com", 22, "Mathematiques");

                @Override
                public java.util.List<Student> getAllStudents() {
                    return Arrays.asList(s1, s2);
                }

                @Override
                public Student getStudentById(Long id) {
                    if (id == 1L) return s1;
                    throw new RuntimeException("Etudiant non trouve avec l'id: " + id);
                }

                @Override
                public Student createStudent(Student student) {
                    if ("existing@email.com".equals(student.getEmail())) {
                        throw new RuntimeException("Un etudiant avec cet email existe deja");
                    }
                    return new Student(3L, student.getFirstName(), student.getLastName(),
                        student.getEmail(), student.getAge(), student.getMajor());
                }

                @Override
                public Student updateStudent(Long id, Student student) {
                    if (id == 1L) return student;
                    throw new RuntimeException("Etudiant non trouve avec l'id: " + id);
                }

                @Override
                public void deleteStudent(Long id) {
                    if (id != 1L) throw new RuntimeException("Etudiant non trouve avec l'id: " + id);
                }

                @Override
                public java.util.List<Student> searchStudents(String query) {
                    if ("ahmed".equalsIgnoreCase(query)) return Arrays.asList(s1);
                    return Collections.emptyList();
                }
            };
        }
    }

    private Student student1;

    @BeforeEach
    void setUp() {
        student1 = new Student(1L, "Ahmed", "Ben Ali", "ahmed@email.com", 21, "Informatique");
    }

    // ===================== GET ALL =====================

    @Test
    void getAllStudents_shouldReturn200AndList() throws Exception {
        mockMvc.perform(get("/api/students"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].firstName").value("Ahmed"))
            .andExpect(jsonPath("$[1].firstName").value("Fatima"));
    }

    // ===================== GET BY ID =====================

    @Test
    void getStudentById_shouldReturn200_whenExists() throws Exception {
        mockMvc.perform(get("/api/students/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Ahmed"))
            .andExpect(jsonPath("$.email").value("ahmed@email.com"));
    }

    @Test
    void getStudentById_shouldReturn400_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/students/99"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    // ===================== CREATE =====================

    @Test
    void createStudent_shouldReturn201_whenValid() throws Exception {
        Student newStudent = new Student(null, "Mohamed", "Trabelsi", "med@email.com", 20, "Physique");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newStudent)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Mohamed"));
    }

    @Test
    void createStudent_shouldReturn400_whenInvalidData() throws Exception {
        Student invalid = new Student(null, "", "", "email-invalide", -5, "");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createStudent_shouldReturn400_whenEmailExists() throws Exception {
        Student duplicate = new Student(null, "Test", "User", "existing@email.com", 20, "Informatique");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    // ===================== UPDATE =====================

    @Test
    void updateStudent_shouldReturn200_whenValid() throws Exception {
        Student updated = new Student(1L, "Ahmed Updated", "Ben Ali", "ahmed@email.com", 25, "Physique");

        mockMvc.perform(put("/api/students/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Ahmed Updated"));
    }

    // ===================== DELETE =====================

    @Test
    void deleteStudent_shouldReturn204_whenExists() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteStudent_shouldReturn400_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/students/99"))
            .andExpect(status().isBadRequest());
    }

    // ===================== SEARCH =====================

    @Test
    void searchStudents_shouldReturn200_withResults() throws Exception {
        mockMvc.perform(get("/api/students/search").param("query", "ahmed"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].firstName").value("Ahmed"));
    }

    @Test
    void searchStudents_shouldReturn200_emptyWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/students/search").param("query", "xyz"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }
}