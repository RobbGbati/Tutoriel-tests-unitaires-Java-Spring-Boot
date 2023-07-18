package com.luv2code.springmvc;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradeDao;
import com.luv2code.springmvc.repository.MathGradeDao;
import com.luv2code.springmvc.repository.ScienceGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application-test.properties")
@SpringBootTest
public class StudentAndGradeServiceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradeDao mathGradeDao;

    @Autowired
    private ScienceGradeDao scienceGradeDao;

    @Autowired
    private HistoryGradeDao historyGradeDao;

    @Value("${sql.script.create.student}")
    private String insertStudentQry;

    @Value("${sql.script.create.math.grade}")
    private String insertMathQry;

    @Value("${sql.script.create.science.grade}")
    private String insertScienceQry;

    @Value("${sql.script.create.history.grade}")
    private String insertHistoryQry;

    @Value("${sql.script.delete.student}")
    private String deleteStudentQry;

    @Value("${sql.script.delete.science.grade}")
    private String deleteScienceQry;

    @Value("${sql.script.delete.math.grade}")
    private String deleteMathQry;

    @Value("${sql.script.delete.history.grade}")
    private String deleteHistoryQry;

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(insertStudentQry);
        jdbc.execute(insertHistoryQry);
        jdbc.execute(insertMathQry);
        jdbc.execute(insertScienceQry);
    }

    @Test
    public void createStudentService() {
        studentService.createStudent("Chad", "Doroty", "chad.doroty@luv2code.com");

        CollegeStudent student = studentDao.findByEmailAddress("chad.doroty@luv2code.com");

        assertEquals("chad.doroty@luv2code.com", student.getEmailAddress(), "find by email");
    }

    @Test
    public void isStudentNullCheck() {
        assertTrue(studentService.checkIfStudentIsNull(1));
        assertFalse(studentService.checkIfStudentIsNull(0));
    }

    @Test
    public void deleteStudentService() {
        Optional<CollegeStudent> deletedCollegeStudent = studentDao.findById(1);
        Optional<MathGrade> deletedMathGrade = mathGradeDao.findById(1);
        Optional<HistoryGrade> deletedHistoryGrade = historyGradeDao.findById(1);
        Optional<ScienceGrade> deletedScienceGrade = scienceGradeDao.findById(1);

        assertTrue(deletedCollegeStudent.isPresent(), "Return True");
        assertTrue(deletedHistoryGrade.isPresent());
        assertTrue(deletedMathGrade.isPresent());
        assertTrue(deletedScienceGrade.isPresent());

        studentService.deleteStudent(1);

        deletedCollegeStudent = studentDao.findById(1);
        deletedHistoryGrade = historyGradeDao.findById(1);
        deletedMathGrade = mathGradeDao.findById(1);
        deletedScienceGrade = scienceGradeDao.findById(1);

        assertFalse(deletedCollegeStudent.isPresent(), "Return False");
        assertFalse(deletedHistoryGrade.isPresent());
        assertFalse(deletedMathGrade.isPresent());
        assertFalse(deletedScienceGrade.isPresent());
    }

    @Sql("/insertData.sql")
    @Test
    public void getGradebookService() {
        Iterable<CollegeStudent> iterableCollegeStudents = studentService.getGradebook();

        List<CollegeStudent> collegeStudents = new ArrayList<>();

        for (CollegeStudent collegeStudent : iterableCollegeStudents) {
            collegeStudents.add(collegeStudent);
        }

        assertEquals(6, collegeStudents.size());
    }

    @Test
    public void createGradeService() {
        // create the grade
        assertTrue(studentService.createGrade(80.50, 1, "math"));
        assertTrue(studentService.createGrade(80.50, 1, "science"));
        assertTrue(studentService.createGrade(80.50, 1, "history"));
        // get all grades with studentsId
        Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);

        // verify there is grades
        assertTrue(((Collection<MathGrade>) mathGrades).size() == 2, "student has math grades");
        assertTrue(((Collection<ScienceGrade>) scienceGrades).size() == 2, "student has science grades");
        assertTrue(((Collection<HistoryGrade>) historyGrades).size() == 2, "student has history grades");
    }

    @Test
    public void createStudentGradeServiceReturnFalse() {
        assertFalse(studentService.createGrade(103, 1, "math"));
        assertFalse(studentService.createGrade(-103, 1, "math"));
        assertFalse(studentService.createGrade(10.60, 12, "math"));
        assertFalse(studentService.createGrade(50.97, 1, "foot"));
    }

    @Test
    public void deleteGradeService() {
        assertEquals(1, studentService.deleteGrade(1, "math"), "Returns student id after delete");
        assertEquals(1, studentService.deleteGrade(1, "science"), "Returns student id after delete");
        assertEquals(1, studentService.deleteGrade(1, "history"), "Returns student id after delete");
    }

    @Test
    public void deleteGradeServiceReturnIdOfZero() {
        assertEquals(0, studentService.deleteGrade(0, "science"), "no student should have 0 id");

        assertEquals(0, studentService.deleteGrade(1, "litterratyre"), "no student shoulds have a litterature class");
    }

    @Test
    public void getStudentInformation() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);

        assertNotNull(gradebookCollegeStudent);
        assertEquals(1, gradebookCollegeStudent.getId());
        assertEquals("Eric", gradebookCollegeStudent.getFirstname());
        assertEquals("Boby", gradebookCollegeStudent.getLastname());
        assertEquals("eric.boby@luv2code.com", gradebookCollegeStudent.getEmailAddress());
        assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size() == 1);
    }

    @Test
    public void studentInformationServiceReturnNull() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);

        assertNull(gradebookCollegeStudent);
    }

    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(deleteStudentQry);
        jdbc.execute(deleteHistoryQry);
        jdbc.execute(deleteMathQry);
        jdbc.execute(deleteScienceQry);
    }
}
