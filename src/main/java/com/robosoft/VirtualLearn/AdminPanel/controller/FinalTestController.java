package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.FinalTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.request.CertificateRequest;
import com.robosoft.VirtualLearn.AdminPanel.response.SubmitResponse;
import com.robosoft.VirtualLearn.AdminPanel.service.FinalTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class FinalTestController {
    @Autowired
    FinalTestService finalTestService;

    private Integer testId = null;


    @GetMapping("/finalTest")
    public ResponseEntity<?> getFinalTest(@RequestParam Integer testId) {

//        String status = finalTestService.checkForCompletedStatus(testId);
//        if(status != null)
//            return new ResponseEntity<>(Collections.singletonMap("message",status),HttpStatus.NOT_ACCEPTABLE);
        FinalTest moduleTest = finalTestService.finalTestService(testId);
        if (moduleTest == null)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        this.testId = testId;

        return ResponseEntity.of(Optional.of(moduleTest));
    }

    @PostMapping("/finalSubmit")
    public ResponseEntity<?> submitUserAnswers(@RequestBody UserAnswers userAnswers) throws IOException, ParseException {
        if(this.testId != testId)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        SubmitResponse testPercentage = finalTestService.userAnswers(userAnswers);
        finalTestService.certificate(userAnswers.getTestId());
        return new ResponseEntity<>(testPercentage,HttpStatus.OK);
    }

    @GetMapping("/result")
    public ResponseEntity<?> getFinalTestResult(@RequestParam Integer testId) throws IOException {
        if(this.testId != testId)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(finalTestService.getFinalTestResult(testId),HttpStatus.OK);
    }

    @GetMapping("/viewCertificate")
    public Map getCertificate(@RequestBody CertificateRequest certificateRequest) throws IOException, ParseException {
        return Collections.singletonMap("message", finalTestService.viewCertificate(certificateRequest.getTestId()));
    }

    @GetMapping("/pdf")
    public Map getPdf(@RequestParam Integer courseId) throws IOException {
        return Collections.singletonMap("certificate", finalTestService.getPdfUrl(courseId));
    }

    @Scheduled(fixedRate = 10000)
    public void clearTestId(){
        this.testId = null;
    }
}
