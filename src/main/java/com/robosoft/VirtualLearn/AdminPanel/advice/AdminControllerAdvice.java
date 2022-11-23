package com.robosoft.VirtualLearn.AdminPanel.advice;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.EmptyStackException;
import java.util.InputMismatchException;

@ControllerAdvice
public class AdminControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(InputMismatchException.class)
    public ResponseEntity<String> handleInputMismatchException(InputMismatchException inputMismatchException) {
        return new ResponseEntity<String>("Please Provide the Valid Input", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>("Please Change the Http Method Type", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyStackException.class)
    public ResponseEntity<String> handleEmptyStackException(EmptyStackException e) {
        return new ResponseEntity<>("Invalid or Empty Input", HttpStatus.BAD_REQUEST);
    }
}