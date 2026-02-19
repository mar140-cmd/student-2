package com.example.students.repository;

import com.example.students.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        student1 = studentRepository.save(
            new Student(null, "Ahmed", "Ben Ali", "ahmed@email.com", 21, "Informatique"));
        student2 = studentRepository.save(
            new Student(null, "Fatima", "Zahra", "fatima@email.com", 22, "Mathematiques"));
    }

    @Test
    void save_shouldPersistStudent() {
        Student saved = studentRepository.save(
            new Student(null, "Mohamed", "Trabelsi", "med@email.com", 20, "Physique"));

        assertNotNull(saved.getId());
        assertEquals("Mohamed", saved.getFirstName());
    }

    @Test
    void findById_shouldReturnStudent_whenExists() {
        Optional<Student> result = studentRepository.findById(student1.getId());

        assertTrue(result.isPresent());
        assertEquals("Ahmed", result.get().getFirstName());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Student> result = studentRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_shouldReturnAllStudents() {
        List<Student> students = studentRepository.findAll();

        assertEquals(2, students.size());
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        assertTrue(studentRepository.existsByEmail("ahmed@email.com"));
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotExists() {
        assertFalse(studentRepository.existsByEmail("inconnu@email.com"));
    }

    @Test
    void existsByEmailAndIdNot_shouldReturnTrue_whenEmailTakenByOther() {
        assertTrue(studentRepository.existsByEmailAndIdNot("fatima@email.com", student1.getId()));
    }

    @Test
    void existsByEmailAndIdNot_shouldReturnFalse_whenEmailBelongsToSameStudent() {
        assertFalse(studentRepository.existsByEmailAndIdNot("ahmed@email.com", student1.getId()));
    }

    @Test
    void search_shouldReturnMatchingStudents_byFirstName() {
        List<Student> result = studentRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("ahmed", "ahmed");

        assertEquals(1, result.size());
        assertEquals("Ahmed", result.get(0).getFirstName());
    }

    @Test
    void search_shouldReturnMatchingStudents_byLastName() {
        List<Student> result = studentRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("zahra", "zahra");

        assertEquals(1, result.size());
        assertEquals("Fatima", result.get(0).getFirstName());
    }

    @Test
    void search_shouldBeCaseInsensitive() {
        List<Student> result = studentRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("AHMED", "AHMED");

        assertEquals(1, result.size());
    }

    @Test
    void search_shouldReturnEmpty_whenNoMatch() {
        List<Student> result = studentRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("xyz", "xyz");

        assertTrue(result.isEmpty());
    }

    @Test
    void delete_shouldRemoveStudent() {
        studentRepository.delete(student1);

        Optional<Student> result = studentRepository.findById(student1.getId());
        assertFalse(result.isPresent());
        assertEquals(1, studentRepository.findAll().size());
    }
}
