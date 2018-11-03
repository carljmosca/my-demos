/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carljmosca.spring.controller;

import com.carljmosca.spring.data.Student;
import com.carljmosca.spring.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author moscac
 */
@Controller
@RequestMapping("/rs")
public class StudentController {

    @Autowired
    StudentRepository studentRepository;

    @RequestMapping(value = "/student", method = RequestMethod.GET)
    public ResponseEntity<PagedResources<Student>> getStudents(Pageable pageable,
    PagedResourcesAssembler assembler) {
        
        Page<Student> students = studentRepository.findAll(pageable);
        
        return new ResponseEntity<>(assembler.toResource(students), HttpStatus.OK);
        
    }
}
