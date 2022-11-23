package com.robosoft.VirtualLearn.AdminPanel.controller;


import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/user")
public class TwoFactorServiceController {
    @Autowired
    SmsService service;

    @PutMapping("/continue")
    public Map sendCodeInSMS(@RequestBody MobileAuth mobileAuth) {
        int status = service.checkMobileNumber(mobileAuth);
        if (status == 1)
            return Collections.singletonMap("message", "User already exists");
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

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody MobileAuth auth) {
        int status = service.checkMobileNumber(auth);
        if (status == 0)
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Invalid Mobile Number")));
        String twoFaCode = String.valueOf(new Random().nextInt(8999) + 1000);
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "OTP Valid For " + service.sendOtp(auth, twoFaCode) + " Minutes")));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody MobileAuth auth) {
        int status = service.checkMobileNumber(auth);
        if (status == 0)
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Input Field is incorrect")));
        service.resetPassword(auth);
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Password Changed Successfully")));
    }
}
