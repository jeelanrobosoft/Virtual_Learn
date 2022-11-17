package com.robosoft.VirtualLearn.AdminPanel.controller;


import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarResponse;
import com.robosoft.VirtualLearn.AdminPanel.entity.ChangePassword;
import com.robosoft.VirtualLearn.AdminPanel.entity.SaveProfile;
import com.robosoft.VirtualLearn.AdminPanel.service.MenuService;
import com.robosoft.VirtualLearn.AdminPanel.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

@RestController
public class ProfileController {

    @Autowired
    MenuService menuService;

    @Autowired
    ProfileService profileService;

    @GetMapping("/MyProfile")
    public ResponseEntity<?> getMyProfile(@RequestBody SideBarRequest request){
        SideBarResponse response = menuService.getUserDetails(request);
        if (response == null)
            return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(menuService.getMyProfile(response , request)));
    }

    @PostMapping("/Save")
    public void saveMyProfile(@RequestBody SaveProfile saveProfile) throws IOException, ParseException {
        profileService.saveMyProfile(saveProfile);

    }

    @PostMapping("/ChangePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePassword password){
        String status = profileService.changePassword(password);
        if(status.equals("Reset Password Failed"))
            return new ResponseEntity<>("Reset Password Failed",HttpStatus.NOT_ACCEPTABLE);
        return new ResponseEntity<>("Password Changed Successfully", HttpStatus.OK);

    }
}
