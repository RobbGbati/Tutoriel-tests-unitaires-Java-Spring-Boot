package com.luv2code.springmvc.controller;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GradebookController {

	@Autowired
	private Gradebook gradebook;

	@Autowired
	private StudentAndGradeService studentService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getStudents(Model m) {
		Iterable<CollegeStudent> collegeStudents = studentService.getGradebook();
		m.addAttribute("students", collegeStudents);
		return "index";
	}


	@GetMapping("/studentInformation/{id}")
		public String studentInformation(@PathVariable int id, Model m) {
		if(!studentService.checkIfStudentIsNull(id)) {
			return "error";
		}

		studentService.initializeStudentInformation(id, m);

		return "studentInformation";
		}


	@PostMapping("/")
	public String createStudent(@ModelAttribute("student") CollegeStudent student, Model m) {
		studentService.createStudent(student.getFirstname(), student.getLastname(), student.getEmailAddress());
		Iterable<CollegeStudent> collegeStudents = studentService.getGradebook();
		m.addAttribute("students", collegeStudents);
		return "index";
	}

	@GetMapping("/delete/student/{id}")
	public String deleteStudent(@PathVariable int id, Model m) {

		if (!studentService.checkIfStudentIsNull(id)) {
			return "error";
		}
		studentService.deleteStudent(id);
		Iterable<CollegeStudent> collegeStudents = studentService.getGradebook();
		m.addAttribute("students", collegeStudents);
		return  "index";
	}

	@PostMapping("/grades")
	public String createGrade(@RequestParam("grade") double grade, @RequestParam String gradeType, @RequestParam int studentId,
							  Model m) {
		if (!studentService.checkIfStudentIsNull(studentId)) {
			return "error";
		}

		boolean success = studentService.createGrade(grade, studentId, gradeType);
		if (!success) {
			return "error";
		}

		studentService.initializeStudentInformation(studentId, m);

		return "studentInformation";
	}

	@GetMapping("/grades/{id}/{gradeType}")
	public String deleteGrade(@PathVariable(name = "id") int gradeId, @PathVariable String gradeType, Model m) {
		int studentId = studentService.deleteGrade(gradeId, gradeType);
		if (studentId == 0) {
			return "error";
		}
		studentService.initializeStudentInformation(studentId, m);
		return "studentInformation";
	}

}
