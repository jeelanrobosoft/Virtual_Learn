package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.request.JwtRequest;
import com.robosoft.VirtualLearn.AdminPanel.response.JwtResponse;
import com.robosoft.VirtualLearn.AdminPanel.service.MyUserDetailsService;
import com.robosoft.VirtualLearn.AdminPanel.service.RegistrationServiceImpl;
import com.robosoft.VirtualLearn.AdminPanel.utility.JwtUtility;
import io.jsonwebtoken.impl.DefaultClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class LoginController {
    @Autowired
    private JwtUtility jwtUtility;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    @Autowired
    private RegistrationServiceImpl service;

    private String mobileNumber = null;

    Logger logger = LoggerFactory.getLogger(LoginController.class);

    @PutMapping("/login")
    public JwtResponse login(@RequestBody JwtRequest jwtRequest) throws Exception {
        try {
            authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(jwtRequest.getUserName(), jwtRequest.getPassword()));
        } catch (DisabledException e) {
            throw new Exception("User Disabled");
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Credentials");
        }
        final UserDetails userDetails = myUserDetailsService.loadUserByUsername(jwtRequest.getUserName());
        final String token = jwtUtility.generateToken(userDetails);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities()));
        return new JwtResponse(token);
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) throws Exception {
        DefaultClaims claims = (io.jsonwebtoken.impl.DefaultClaims) request.getAttribute("claims");
        Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);
        if(expectedMap == null)
            return new ResponseEntity<>(Collections.singletonMap("Error" , "Token Not Expired"), HttpStatus.NOT_ACCEPTABLE);
        String token = jwtUtility.doGenerateRefreshToken(expectedMap, expectedMap.get("sub").toString());
        return new ResponseEntity<>(new JwtResponse(token),HttpStatus.OK);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody MobileAuth auth) {
        int status = service.checkMobileNumber(auth);
        if (status == 0)
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Mobile Number not registered")));
        service.deletePreviousOtp(auth.getMobileNumber());
        mobileNumber = auth.getMobileNumber();
        String twoFaCode = String.valueOf(new Random().nextInt(8999) + 1000);
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "OTP Valid For " + service.sendOtp(auth, twoFaCode) + " Minutes")));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody MobileAuth auth) {
        int status = service.checkMobileNumber(auth);
        if (status == 0)
            return new ResponseEntity<>(Collections.singletonMap("message", "Input Field is incorrect"),HttpStatus.NOT_ACCEPTABLE);
        if((auth.getMobileNumber().equals(mobileNumber)) != true)
            return new ResponseEntity<>(Collections.singletonMap("message", "Incorrect Mobile Number"),HttpStatus.NOT_ACCEPTABLE);
        status = service.checkForVerificationStatus(auth.getMobileNumber());
        if(status > 0)
            return new ResponseEntity<>(Collections.singletonMap("message", "Mobile Number not verified"),HttpStatus.NOT_ACCEPTABLE);
        service.resetPassword(auth);
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Password Changed Successfully")));
    }


    public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        try{
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            expectedMap.put(entry.getKey(), entry.getValue());
        }
        return expectedMap;
        } catch (NullPointerException e){
            return null;
        }
    }


    // Event Scheduler which makes mobileNumber reference null
    @Scheduled(fixedRate = 60000)
    public void eventScheduler(){
        mobileNumber = null;
    }




}
