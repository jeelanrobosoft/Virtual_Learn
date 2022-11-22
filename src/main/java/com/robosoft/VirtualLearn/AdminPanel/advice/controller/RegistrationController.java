package com.robosoft.VirtualLearn.AdminPanel.advice.controller;


import com.robosoft.VirtualLearn.AdminPanel.entity.UserRegistration;
import com.robosoft.VirtualLearn.AdminPanel.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
public class RegistrationController
{
    @Autowired
    RegistrationService registrationService;
    @PostMapping("/Register")
    public ResponseEntity<Map> registration(@RequestBody UserRegistration registration)
    {
        String status = registrationService.addDetails(registration);
        if(status == null)
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message","User Created")));
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message",status)));
    }
}
