package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.ModuleTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.response.SubmitResponse;
import com.robosoft.VirtualLearn.AdminPanel.service.ModuleTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
public class ModuleTestController {
    @Autowired
    ModuleTestService testService;

    private Integer testId = null;

    @GetMapping("/moduleTest")
    public ResponseEntity<?> moduleTest(@RequestParam Integer testId) {
       //String status = testService.checkForCompletedStatus(testId);
//        if(status != null)
//            return new ResponseEntity<>(Collections.singletonMap("message",status),HttpStatus.NOT_ACCEPTABLE);
        ModuleTest moduleTest = testService.moduleTestQuestions(testId);
        if (moduleTest == null)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        this.testId = testId;
        return ResponseEntity.of(Optional.of(moduleTest));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitUserAnswers(@RequestBody UserAnswers userAnswers) throws SQLException {
        if(userAnswers.getTestId() != this.testId)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        SubmitResponse submitResponse = testService.userAnswers(userAnswers);
        if(submitResponse == null)
            return new ResponseEntity<>(Collections.singletonMap("message","Enter Correct question id's"),HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(submitResponse ,HttpStatus.OK);
    }

    @GetMapping("/resultHeader")
    public ResponseEntity<?> getResultHeader(@RequestParam Integer testId) {
        if(this.testId != testId)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(testService.getResultHeader(testId),HttpStatus.OK);
    }

    @GetMapping("/resultAnswers")
    public ResponseEntity<?> getResultAnswers(@RequestParam Integer testId) {
        if(this.testId != testId)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(testService.getResultAnswers(testId),HttpStatus.OK);
    }


    @Scheduled(fixedRate = 3600000)
    public void clearTestId(){
        this.testId = null;
    }
}
