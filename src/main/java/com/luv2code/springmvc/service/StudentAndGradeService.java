package com.luv2code.springmvc.service;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradeDao;
import com.luv2code.springmvc.repository.MathGradeDao;
import com.luv2code.springmvc.repository.ScienceGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentAndGradeService {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    @Qualifier("mathGrades")
    private MathGrade mathGrade;

    @Autowired
    private MathGradeDao mathGradeDao;

    @Autowired
    @Qualifier("scienceGrades")
    private ScienceGrade scienceGrade;

    @Autowired
    private ScienceGradeDao scienceGradeDao;

    @Autowired
    @Qualifier("historyGrades")
    private HistoryGrade historyGrade;

    @Autowired
    private HistoryGradeDao historyGradeDao;

    @Autowired
    private StudentGrades studentGrades;

    public void createStudent(String firstname, String lastname, String emailAddress) {
        CollegeStudent student = new CollegeStudent(firstname, lastname, emailAddress);
        student.setId(0);
        studentDao.save(student);
    }

    public boolean checkIfStudentIsNull(int id) {
        Optional<CollegeStudent> student = studentDao.findById(id);
        if (student.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    public void deleteStudent(int id) {
        if (checkIfStudentIsNull(id)) {
            studentDao.deleteById(id);
            mathGradeDao.deleteByStudentId(id);
            historyGradeDao.deleteByStudentId(id);
            scienceGradeDao.deleteByStudentId(id);
        }
    }

    public Iterable<CollegeStudent> getGradebook() {
        Iterable<CollegeStudent> collegeStudents = studentDao.findAll();
        return collegeStudents;
    }

    public boolean createGrade(double grade, int studentId, String gradeType) {
        if(!checkIfStudentIsNull(studentId)) {
            return false;
        }
        if (grade >= 0 && grade <= 100) {
            if (gradeType.equals("math")) {
                mathGrade.setId(0);
                mathGrade.setGrade(grade);
                mathGrade.setStudentId(studentId);
                mathGradeDao.save(mathGrade);
                return true;
            } else if (gradeType.equals("science")) {
                scienceGrade.setId(0);
                scienceGrade.setGrade(grade);
                scienceGrade.setStudentId(studentId);
                scienceGradeDao.save(scienceGrade);
                return true;
            } else if (gradeType.equals("history")) {
                historyGrade.setId(0);
                historyGrade.setGrade(grade);
                historyGrade.setStudentId(studentId);
                historyGradeDao.save(historyGrade);
                return true;

            }
        }
        return false;
    }

    public int deleteGrade(int id, String gradeType) {
        int studentId = 0;
        if (gradeType.equals("math")) {
            Optional<MathGrade> grade =  mathGradeDao.findById(id);
            if (!grade.isPresent()) {
                return studentId;
            }
            studentId = grade.get().getStudentId();
            mathGradeDao.deleteById(id);
        } else if (gradeType.equals("science")) {
            Optional<ScienceGrade> grade = scienceGradeDao.findById(id);
            if (!grade.isPresent()) {
                return studentId;
            }
            studentId = grade.get().getStudentId();
            scienceGradeDao.deleteById(id);
        } else if (gradeType.equals("history")) {
            Optional<HistoryGrade> grade = historyGradeDao.findById(id);
            if (!grade.isPresent()) {
                return studentId;
            }
            studentId = grade.get().getStudentId();
            historyGradeDao.deleteById(id);
        }
        return studentId;
    }

    public GradebookCollegeStudent studentInformation(int id) {
        if (!checkIfStudentIsNull(id)) {
            return null;
        }
        Optional<CollegeStudent> student = studentDao.findById(id);
        Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(id);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(id);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(id);

        List<Grade> mathGradeList = new ArrayList<>();
        List<Grade> scienceGradeList = new ArrayList<>();
        List<Grade> historyGradList = new ArrayList<>();

        mathGrades.forEach(mathGradeList::add);
        scienceGrades.forEach(scienceGradeList::add);
        historyGrades.forEach(historyGradList::add);

        studentGrades.setHistoryGradeResults(historyGradList);
        studentGrades.setMathGradeResults(mathGradeList);
        studentGrades.setScienceGradeResults(scienceGradeList);

        GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(
                student.get().getId(),
                student.get().getFirstname(),
                student.get().getLastname(),
                student.get().getEmailAddress(),
                studentGrades
        );

        return gradebookCollegeStudent;
    }

    public void initializeStudentInformation(int id, Model m) {
        GradebookCollegeStudent studentEntity = this.studentInformation(id);
        m.addAttribute("student", studentEntity);
        if (studentEntity.getStudentGrades().getMathGradeResults().size() > 0) {
            m.addAttribute("mathAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getMathGradeResults()
            ));
        } else {
            m.addAttribute("mathAverage", "N/A");
        }

        if (studentEntity.getStudentGrades().getScienceGradeResults().size() > 0) {
            m.addAttribute("scienceAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getScienceGradeResults()
            ));
        } else {
            m.addAttribute("scienceAverage", "N/A");
        }

        if (studentEntity.getStudentGrades().getHistoryGradeResults().size() > 0) {
            m.addAttribute("historyAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getHistoryGradeResults()
            ));
        } else {
            m.addAttribute("historyAverage", "N/A");
        }

    }
}
