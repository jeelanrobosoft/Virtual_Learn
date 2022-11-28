package com.robosoft.VirtualLearn.AdminPanel.exceptions;


import com.twilio.exception.ApiException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class MyControllerAdvice {

//    @ExceptionHandler(EmptyResultDataAccessException.class)
//    public ResponseEntity<Map> handleEmptyResult(EmptyResultDataAccessException exception) {
//        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Input field is incorrect")));
//    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<Map> handleEmptyResult(ParseException exception) {
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Send Date in dd/mm/yyyy format")));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map> handleApiException(ApiException exception) {
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Please Enter Valid Phone Number")));
    }

}
