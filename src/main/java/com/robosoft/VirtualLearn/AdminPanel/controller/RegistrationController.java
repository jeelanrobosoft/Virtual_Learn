package com.robosoft.VirtualLearn.AdminPanel.controller;


import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserRegistration;
import com.robosoft.VirtualLearn.AdminPanel.service.RegistrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/newUser")
@CrossOrigin(origins = "http://localhost:3000")
public class RegistrationController {

    @Autowired
    RegistrationServiceImpl service;
    @Autowired
    RegistrationServiceImpl registrationService;

    private String mobileNumber = null;

    @PutMapping("/continue")
    public ResponseEntity<?> sendCodeInSMS(@RequestBody MobileAuth mobileAuth) {
        int status = service.checkMobileNumber(mobileAuth);
        if (status == 1)
            return new ResponseEntity<>(Collections.singletonMap("message", "User already exists"),HttpStatus.NOT_ACCEPTABLE);
        service.deletePreviousOtp(mobileAuth.getMobileNumber());
        mobileNumber = mobileAuth.getMobileNumber();
        String twoFaCode = String.valueOf(new Random().nextInt(8999) + 1000);
        return new ResponseEntity<>(Collections.singletonMap("message", "OTP Valid For " + service.sendOtp(mobileAuth, twoFaCode) + " Minutes"),HttpStatus.OK);
    }

    @PutMapping("/resend")
    public ResponseEntity<?> ResendCodeInSMS(@RequestBody MobileAuth mobileAuth) {
        service.deletePreviousOtp(mobileAuth.getMobileNumber());
        String twoFaCode = String.valueOf(new Random().nextInt(8999) + 1000);
        return new ResponseEntity<>(Collections.singletonMap("message", "OTP Valid For " + service.sendOtp(mobileAuth, twoFaCode) + " Minutes"),HttpStatus.OK);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody MobileAuth otp) {
        return new ResponseEntity<>(Collections.singletonMap("message", service.verifyOtp(otp)),HttpStatus.OK);
    }



    @PostMapping("/register")
    public ResponseEntity<Map> registration(@RequestBody UserRegistration registration) {
        if((registration.getMobileNumber().equals(mobileNumber)) != true)
            return new ResponseEntity<>(Collections.singletonMap("message", "Incorrect Mobile Number"), HttpStatus.NOT_ACCEPTABLE);
        Integer status = service.checkForVerificationStatus(registration.getMobileNumber());
        if(status > 0)
            return new ResponseEntity<>(Collections.singletonMap("message", "Mobile Number not verified"),HttpStatus.NOT_ACCEPTABLE);
        String addDetails = registrationService.addDetails(registration);
        if (addDetails == null)
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "User Created")));
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", addDetails)));
    }

    @Scheduled(fixedRate = 3600000)
    public void eventScheduler(){
        mobileNumber = null;
    }

}
