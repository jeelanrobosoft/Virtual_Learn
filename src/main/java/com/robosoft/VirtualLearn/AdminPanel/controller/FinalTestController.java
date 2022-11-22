package com.robosoft.VirtualLearn.AdminPanel.controller;
import com.robosoft.VirtualLearn.AdminPanel.entity.FinalTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.request.CertificateRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.FinalTestRequest;
import com.robosoft.VirtualLearn.AdminPanel.service.FinalTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
public class FinalTestController
{
    @Autowired

    FinalTestService finalTestService;
    @GetMapping("FinalTest")
    public ResponseEntity<?> getFinalTest(@RequestBody FinalTestRequest request)
    {
        FinalTest moduleTest = finalTestService.finalTestService(request);
        if(moduleTest == null)
            return new ResponseEntity<>("Invalid Test Id", HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(moduleTest));
    }
    @PostMapping("/FinalSubmit")
    public Map submitUserAnswers(@RequestBody UserAnswers userAnswers) throws IOException, ParseException {
        float testPercentage = finalTestService.userAnswers(userAnswers);
        finalTestService.certificate(userAnswers.getTestId());
        return Collections.singletonMap("Test Percentage",testPercentage);
    }
    @GetMapping("/Result")
    public Map getFinalTestResult(@RequestBody FinalTestRequest request) throws IOException
    {
        return Collections.singletonMap("Approval Rate",finalTestService.getFinalTestResult(request));
    }
    @GetMapping("/viewCertificate")
    public Map getCertificate(@RequestBody CertificateRequest certificateRequest) throws IOException, ParseException
    {
        return Collections.singletonMap("Test Percentage", finalTestService.viewCertificate(certificateRequest));
    }

}
