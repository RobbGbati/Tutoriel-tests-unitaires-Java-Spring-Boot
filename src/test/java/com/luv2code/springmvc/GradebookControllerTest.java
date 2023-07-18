package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.repository.MathGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource("/application-test.properties")
@SpringBootTest
@AutoConfigureMockMvc
public class GradebookControllerTest {

    private static MockHttpServletRequest request;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private StudentAndGradeService studentCreateServiceMock;

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradeDao mathGradeDao;

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

    @BeforeAll
    public static void setup() {
        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Chad");
        request.setParameter("lastname", "Boby");
        request.setParameter("emailAddress", "chad.boby@luv2code.com");
    }

    @BeforeEach
    public void beforeEach() {
        jdbc.execute(insertStudentQry);
        jdbc.execute(insertHistoryQry);
        jdbc.execute(insertMathQry);
        jdbc.execute(insertScienceQry);
    }

    @Test
    public void getStudentsHttpRequest() throws Exception {
        CollegeStudent studentOne = new GradebookCollegeStudent(
                "Eric", "Boby", "eric.boby@luv2code.com");

        CollegeStudent studentTwo = new GradebookCollegeStudent(
                "Malik", "Tesla", "malik.tesla@luv2code.com");

        List<CollegeStudent> collegeStudentList = new ArrayList<>(Arrays.asList(studentOne, studentTwo));

        when(studentCreateServiceMock.getGradebook()).thenReturn(collegeStudentList);

        assertIterableEquals(collegeStudentList, studentCreateServiceMock.getGradebook());

        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "index");
    }

    @Test
    public void createStudentHttpRequest() throws Exception {

        CollegeStudent studentOne = new CollegeStudent("Eric", "Boby", "eric.boby@luv2code.com");

        List<CollegeStudent> collegeStudentList = new ArrayList<>(Arrays.asList(studentOne));

        when(studentCreateServiceMock.getGradebook()).thenReturn(collegeStudentList);

        MvcResult mvcResult = this.mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstname", request.getParameter("firstname"))
                .param("lastname", request.getParameter("lastname"))
                .param("emailAddress", request.getParameter("emailAddress")))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "index");

        CollegeStudent verifyStudent = studentDao.findByEmailAddress("chad.boby@luv2code.com");

        assertNotNull(verifyStudent, "Student should be found");
    }

    @Test
    public void deleteStudentHttpRequest() throws Exception {
        assertTrue(studentDao.findById(1).isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/delete/student/{id}", 1))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "index");
        assertFalse(studentDao.findById(1).isPresent());
    }

    @Test
    public void deleteStudentHttpRequestErrorPage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                get("/delete/student/{id}", 0))
                        .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");

    }

    @Test
    public void studentInformationHttpStudentDoesNotExistsRequest() throws Exception {
        assertFalse(studentDao.findById(0).isPresent());
        MvcResult mvcResult = mockMvc.perform(get("/studentInformation/{id}", 0))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test
    public void createValidGradeHttpRequest() throws Exception {
        assertTrue(studentDao.findById(1).isPresent());
        GradebookCollegeStudent student = studentService.studentInformation(1);

        assertEquals(1, student.getStudentGrades().getMathGradeResults().size());

        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "math")
                .param("studentId", "1"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "studentInformation");

        student  = studentService.studentInformation(1);

        assertEquals(2, student.getStudentGrades().getMathGradeResults().size());
    }

    @Test
    public void createValideGradeHttpRequestStudentDoesNotExistEmptyResponse() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform((post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.01")
                .param("gradeType", "history")
                .param("studentId", "0"))
                ).andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");

    }

    @Test
    public void createNonValidGradeHttpRequestGradeTypeDoesNotExistEmptyResp() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform((post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.01")
                .param("gradeType", "sport")
                .param("studentId", "1"))
        ).andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test
    public void deleteValidGradeHttpRequest() throws Exception {
        Optional<MathGrade> mathGrade = mathGradeDao.findById(1);

        assertTrue(mathGrade.isPresent());

        MvcResult mvcResult = this.mockMvc.perform(get("/grades/{id}/{gradeType}", 1, "math"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "studentInformation");

        mathGrade = mathGradeDao.findById(1);

        assertFalse(mathGrade.isPresent());
    }

    @Test
    public void deleteAValidGradeHttpRequestStudentIdDoesNotExistEmptyResponse() throws Exception {
        Optional<MathGrade> mathGrade = mathGradeDao.findById(2);

        assertFalse(mathGrade.isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/grades/{id}/{gradeType}", 2, "math"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test
    public void deleteANonValidGradehttpRequest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/grades/{id}/{gradeType}", 1, "sport"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }


    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(deleteStudentQry);
        jdbc.execute(deleteHistoryQry);
        jdbc.execute(deleteMathQry);
        jdbc.execute(deleteScienceQry);
    }
}
