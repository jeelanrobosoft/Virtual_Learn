package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.dto.ModuleTestRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultAnswerRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultHeaderRequest;
import com.robosoft.VirtualLearn.AdminPanel.entity.ModuleTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.service.ModuleTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ChapterModuleTestController {
    @Autowired
    ModuleTestService testService;

    @GetMapping("/moduleTest")
    public ResponseEntity<?> moduleTest(@RequestParam Integer testId) {
        ModuleTest moduleTest = testService.moduleTestQuestions(testId);
        if (moduleTest == null)
            return new ResponseEntity<>("Invalid Test Id", HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(moduleTest));
    }

    @PostMapping("/submit")
    public Map submitUserAnswers(@RequestBody UserAnswers userAnswers) {
        return Collections.singletonMap("Test Percentage", testService.userAnswers(userAnswers));
    }

    @GetMapping("resultHeader")
    public ResultHeaderRequest getResultHeader(@RequestParam Integer testId) {
        return testService.getResultHeader(testId);
    }

    @GetMapping("resultAnswers")
    public List<ResultAnswerRequest> getResultAnswers(@RequestParam Integer testId) {
        return testService.getResultAnswers(testId);
    }
}
