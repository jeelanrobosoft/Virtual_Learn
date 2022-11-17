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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ChapterModuleTestController {


    @Autowired
    ModuleTestService testService;

    @GetMapping("/ModuleTest")
    public ResponseEntity<?> moduleTest(@RequestBody ModuleTestRequest request){
        ModuleTest moduleTest = testService.moduleTestQuestions(request);
        if(moduleTest == null)
            return new ResponseEntity<>("Invalid Test Id", HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(moduleTest));
    }

    @PostMapping("/Submit")
    public Map submitUserAnswers(@RequestBody UserAnswers userAnswers){
        return Collections.singletonMap("Test Percentage",testService.userAnswers(userAnswers));
    }

    @GetMapping("Result_Header")
    public ResultHeaderRequest getResultHeader(@RequestBody ModuleTestRequest testRequest){
        return testService.getResultHeader(testRequest);
    }

    @GetMapping("Result_Answers")
    public List<ResultAnswerRequest> getResultAnswers(@RequestBody ModuleTest request){
            return testService.getResultAnswers(request);
    }


}
