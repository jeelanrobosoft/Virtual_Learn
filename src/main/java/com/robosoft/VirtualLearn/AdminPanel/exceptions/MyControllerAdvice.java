package com.robosoft.VirtualLearn.AdminPanel.exceptions;


import com.twilio.exception.ApiException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class MyControllerAdvice {

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Map> handleEmptyResult(EmptyResultDataAccessException exception) {
        return new ResponseEntity<>(Collections.singletonMap("message", "Input field is incorrect"), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<Map> handleEmptyResult(ParseException exception) {
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Send Date in yyyy-MM-dd format")));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map> handleApiException(ApiException exception) {
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Please Enter Valid Phone Number")));
    }

}
