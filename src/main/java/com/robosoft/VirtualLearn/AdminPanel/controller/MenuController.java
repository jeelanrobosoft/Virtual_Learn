package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.dto.NotificationResponse;
import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarResponse;
import com.robosoft.VirtualLearn.AdminPanel.service.MenuService;
import com.robosoft.VirtualLearn.AdminPanel.utility.JwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin("http://localhost:3000")
public class MenuController {
    @Autowired
    MenuService menuService;

    @Autowired
    private JwtUtility jwtUtility;


    @GetMapping("/menu")
    public ResponseEntity<?> getSideBar() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        SideBarResponse response = menuService.getUserDetails(userName);
        if (response == null)
            return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(response));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotification() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<NotificationResponse> responses = menuService.getNotification(userName);
        if (responses.isEmpty())
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No notifications")));
        return ResponseEntity.of(Optional.of(responses));
    }

    @PutMapping("/readNotification")
    public ResponseEntity<Map> readNotification(@RequestBody NotificationResponse notificationResponse) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", menuService.readNotification(userName, notificationResponse.getNotificationId()))));
    }
}
