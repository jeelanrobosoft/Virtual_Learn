package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.ModuleTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.service.ModuleTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class ModuleTestController {
    @Autowired
    ModuleTestService testService;

    @GetMapping("/moduleTest")
    public ResponseEntity<?> moduleTest(@RequestParam Integer testId) {
//        String status = testService.checkForCompletedStatus(testId);
//        if(status != null)
//            return new ResponseEntity<>(Collections.singletonMap("message",status),HttpStatus.NOT_ACCEPTABLE);
        ModuleTest moduleTest = testService.moduleTestQuestions(testId);
        if (moduleTest == null)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(moduleTest));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitUserAnswers(@RequestBody UserAnswers userAnswers) {
        return new ResponseEntity<>( testService.userAnswers(userAnswers),HttpStatus.OK);
    }

    @GetMapping("/resultHeader")
    public ResponseEntity<?> getResultHeader(@RequestParam Integer testId) {
        return new ResponseEntity<>(testService.getResultHeader(testId),HttpStatus.OK);
    }

    @GetMapping("/resultAnswers")
    public ResponseEntity<?> getResultAnswers(@RequestParam Integer testId) {
        return new ResponseEntity<>(testService.getResultAnswers(testId),HttpStatus.OK);
    }
}
