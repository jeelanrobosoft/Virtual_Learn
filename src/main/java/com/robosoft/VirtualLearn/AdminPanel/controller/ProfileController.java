package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarResponse;
import com.robosoft.VirtualLearn.AdminPanel.entity.ChangePassword;
import com.robosoft.VirtualLearn.AdminPanel.entity.SaveProfile;
import com.robosoft.VirtualLearn.AdminPanel.service.MenuService;
import com.robosoft.VirtualLearn.AdminPanel.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {
    @Autowired
    MenuService menuService;
    @Autowired
    ProfileService profileService;

    @GetMapping("/myProfile")
    public ResponseEntity<?> getMyProfile() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        SideBarResponse response = menuService.getUserDetails(userName);
        if (response == null)
            return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(menuService.getMyProfile(response, userName)));
    }

    @PutMapping("/save")
    public ResponseEntity<?> saveMyProfile(@ModelAttribute SaveProfile saveProfile) throws IOException, ParseException {
        if(saveProfile == null)
            return new ResponseEntity<>(Collections.singletonMap("Error","All fields are empty"),HttpStatus.NOT_MODIFIED);
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
       String message = profileService.saveMyProfile(saveProfile, userName);
       if(message == null)
           return new ResponseEntity<>(Collections.singletonMap("message", "Successfully updated profile"),HttpStatus.OK);
       return new ResponseEntity<>(Collections.singletonMap("message", message),HttpStatus.NOT_ACCEPTABLE);

    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePassword password) {
        String status = profileService.changePassword(password);
        if (status.equals("Reset Password Failed"))
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Reset Password Failed")));
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Password Changed Successfully")));

    }
}
