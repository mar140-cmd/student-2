package com.example.students.service;

import com.example.students.model.Student;
import com.example.students.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        student1 = new Student(1L, "Ahmed", "Ben Ali", "ahmed@email.com", 21, "Informatique");
        student2 = new Student(2L, "Fatima", "Zahra", "fatima@email.com", 22, "Mathematiques");
    }

    // ===================== GET ALL =====================

    @Test
    void getAllStudents_shouldReturnAllStudents() {
        when(studentRepository.findAll()).thenReturn(Arrays.asList(student1, student2));

        List<Student> result = studentService.getAllStudents();

        assertEquals(2, result.size());
        assertEquals("Ahmed", result.get(0).getFirstName());
        assertEquals("Fatima", result.get(1).getFirstName());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void getAllStudents_shouldReturnEmptyList() {
        when(studentRepository.findAll()).thenReturn(Arrays.asList());

        List<Student> result = studentService.getAllStudents();

        assertTrue(result.isEmpty());
    }

    // ===================== GET BY ID =====================

    @Test
    void getStudentById_shouldReturnStudent_whenExists() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));

        Student result = studentService.getStudentById(1L);

        assertNotNull(result);
        assertEquals("Ahmed", result.getFirstName());
        assertEquals("Ben Ali", result.getLastName());
        assertEquals("ahmed@email.com", result.getEmail());
    }

    @Test
    void getStudentById_shouldThrowException_whenNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> studentService.getStudentById(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    // ===================== CREATE =====================

    @Test
    void createStudent_shouldCreateSuccessfully() {
        when(studentRepository.existsByEmail("ahmed@email.com")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student1);

        Student result = studentService.createStudent(student1);

        assertNotNull(result);
        assertEquals("Ahmed", result.getFirstName());
        verify(studentRepository, times(1)).save(student1);
    }

    @Test
    void createStudent_shouldThrowException_whenEmailExists() {
        when(studentRepository.existsByEmail("ahmed@email.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> studentService.createStudent(student1));

        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(studentRepository, never()).save(any());
    }

    // ===================== UPDATE =====================

    @Test
    void updateStudent_shouldUpdateSuccessfully() {
        Student updated = new Student(1L, "Ahmed Updated", "Ben Ali", "ahmed@email.com", 25, "Physique");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(studentRepository.existsByEmailAndIdNot("ahmed@email.com", 1L)).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(updated);

        Student result = studentService.updateStudent(1L, updated);

        assertEquals("Ahmed Updated", result.getFirstName());
        assertEquals(25, result.getAge());
        assertEquals("Physique", result.getMajor());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void updateStudent_shouldThrowException_whenEmailTakenByOther() {
        Student updated = new Student(1L, "Ahmed", "Ben Ali", "fatima@email.com", 21, "Informatique");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(studentRepository.existsByEmailAndIdNot("fatima@email.com", 1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> studentService.updateStudent(1L, updated));

        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateStudent_shouldThrowException_whenStudentNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> studentService.updateStudent(99L, student1));
    }

    // ===================== DELETE =====================

    @Test
    void deleteStudent_shouldDeleteSuccessfully() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        doNothing().when(studentRepository).delete(student1);

        assertDoesNotThrow(() -> studentService.deleteStudent(1L));
        verify(studentRepository, times(1)).delete(student1);
    }

    @Test
    void deleteStudent_shouldThrowException_whenNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> studentService.deleteStudent(99L));

        verify(studentRepository, never()).delete(any());
    }

    // ===================== SEARCH =====================

    @Test
    void searchStudents_shouldReturnMatchingStudents() {
        when(studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            "ahmed", "ahmed")).thenReturn(Arrays.asList(student1));

        List<Student> result = studentService.searchStudents("ahmed");

        assertEquals(1, result.size());
        assertEquals("Ahmed", result.get(0).getFirstName());
    }

    @Test
    void searchStudents_shouldReturnEmpty_whenNoMatch() {
        when(studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            "xyz", "xyz")).thenReturn(Arrays.asList());

        List<Student> result = studentService.searchStudents("xyz");

        assertTrue(result.isEmpty());
    }
}
