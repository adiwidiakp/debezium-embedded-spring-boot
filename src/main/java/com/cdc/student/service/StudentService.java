package com.cdc.student.service;

import com.cdc.student.model.Student;
import com.cdc.student.repository.StudentRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.debezium.data.Envelope.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StudentService {

	@Autowired
	private StudentRepository studentRepository;

	
	public void maintainReadModel(Map<String, Object> studentData, Operation operation) {
		final ObjectMapper mapper = new ObjectMapper();
		final Student student = mapper.convertValue(studentData, Student.class);
		        log.info("StudentService.maintainReadModel : {}", operation);

		if (Operation.DELETE.name().equals(operation.name())) {
			studentRepository.deleteById(student.getId());
		} else {
			studentRepository.save(student);
		}
	}
}
