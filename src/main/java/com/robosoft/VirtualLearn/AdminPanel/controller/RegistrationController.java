package com.robosoft.VirtualLearn.AdminPanel.controller;


import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserRegistration;
import com.robosoft.VirtualLearn.AdminPanel.service.RegistrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/newUser")
public class RegistrationController {

    @Autowired
    RegistrationServiceImpl service;
    @Autowired
    RegistrationServiceImpl registrationService;

    @PutMapping("/continue")
    public Map sendCodeInSMS(@RequestBody MobileAuth mobileAuth) {
        int status = service.checkMobileNumber(mobileAuth);
        if (status == 1)
            return Collections.singletonMap("message", "User already exists");
        service.deletePreviousOtp(mobileAuth.getMobileNumber());
        String twoFaCode = String.valueOf(new Random().nextInt(8999) + 1000);
        return Collections.singletonMap("message", "OTP Valid For " + service.sendOtp(mobileAuth, twoFaCode) + " Minutes");
    }

    @PutMapping("/resend")
    public Map ResendCodeInSMS(@RequestBody MobileAuth mobileAuth) {
        service.deletePreviousOtp(mobileAuth.getMobileNumber());
        String twoFaCode = String.valueOf(new Random().nextInt(8999) + 1000);
        return Collections.singletonMap("message", "OTP Valid For " + service.sendOtp(mobileAuth, twoFaCode) + " Minutes");
    }

    @PostMapping("/verify")
    public Map verifyOtp(@RequestBody MobileAuth otp) {
        return Collections.singletonMap("message", service.verifyOtp(otp));
    }



    @PostMapping("/register")
    public ResponseEntity<Map> registration(@RequestBody UserRegistration registration) {
        String status = registrationService.addDetails(registration);
        if (status == null)
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "User Created")));
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", status)));
    }


}
