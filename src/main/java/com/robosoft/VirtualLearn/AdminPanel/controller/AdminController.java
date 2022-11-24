package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.*;
import com.robosoft.VirtualLearn.AdminPanel.request.*;
import com.robosoft.VirtualLearn.AdminPanel.response.JwtResponse;
import com.robosoft.VirtualLearn.AdminPanel.service.AdminService;
import com.robosoft.VirtualLearn.AdminPanel.service.MyUserDetailsService;
import com.robosoft.VirtualLearn.AdminPanel.utility.JwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtility jwtUtility;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @PostMapping("/register")
    public Map register(@RequestBody AdminRegistration adminRegistration) {
        return Collections.singletonMap("message", adminService.addAdminDetails(adminRegistration));
    }

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
        return new JwtResponse(token);
    }

    @PostMapping("/category")
    public ResponseEntity<?> addCategory(@ModelAttribute CategoryRequest category) throws IOException {
        int change = adminService.addCategory(category);
        if (change > 0)
            return ResponseEntity.of(Optional.of("Category " + category.getCategoryName() + " has been Added SuccessFully"));
        else
            return new ResponseEntity<>("Category Type Already Exists", HttpStatus.ALREADY_REPORTED);
    }

    @PostMapping("/subCategory")
    public ResponseEntity<?> addSubCategory(@RequestBody SubCategory subcategory) {
        int change = adminService.addSubCategory(subcategory);

        if (change > 0)
            return ResponseEntity.of(Optional.of("SubCategory " + subcategory.getSubCategoryName() + " has been Added SuccessFully"));
        else
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
    }

    @PostMapping("/chapter")
    public ResponseEntity<?> addChapter(@RequestBody Chapter chapter) {
        int change = adminService.addChapter(chapter);

        if (change > 0)
            return ResponseEntity.of(Optional.of("Chapter " + chapter.getChapterName() + " has been Added SuccessFully"));
        else
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
    }

    @PostMapping("/overview")
    public ResponseEntity<?> addOverview(@RequestBody Overview overview) {
        if (adminService.addOverView(overview) != 0)
            return ResponseEntity.of(Optional.of("OverView for the Course" + overview.getCourseId() + " has been Added SuccessFully"));
        else
            return new ResponseEntity<>("Overview For the Course " + overview.getCourseId() + "has already is Already Present", HttpStatus.ALREADY_REPORTED);
    }

    @PostMapping("/policy")
    public ResponseEntity<?> addPolicy(@RequestBody Policy policy) {
        if (adminService.addPolicy(policy) != 0) {
            return new ResponseEntity<>("Privacy Policy and Terms and Condition Updated", HttpStatus.OK);
        }
        return new ResponseEntity<>("You Didn't changed the Privacy Policy and Terms and Conditions or Failed to Update the Privacy Policy and Terms and Condition", HttpStatus.ALREADY_REPORTED);
    }

    @PostMapping("/course")
    public ResponseEntity<String> saveCourse(@ModelAttribute CourseRequest courseRequest) throws IOException {
        String courseResponse = adminService.addCourse(courseRequest);
        if (courseResponse == null) {
            return new ResponseEntity<String>("course addition is unsuccessful", HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<String>(courseResponse, HttpStatus.OK);
    }

    @PostMapping("/keyword")
    public ResponseEntity<?> addCourseKeyword(@RequestBody  CourseKeywordRequest courseKeywordRequest){
       String keyword= adminService.addCourseKeywords(courseKeywordRequest);
       if(keyword == null)
       {
           return new ResponseEntity<String>("course keyword addition is unsuccessful", HttpStatus.NOT_MODIFIED);
       }
        return new ResponseEntity<String>(keyword, HttpStatus.OK);
    }
    @PostMapping("/lesson")
    public ResponseEntity<String> saveLesson(@ModelAttribute LessonRequest lessonRequest) throws IOException, ParseException {
        String lessonResponse = adminService.addLesson(lessonRequest);
        if (lessonResponse == null) {
            return new ResponseEntity<String>("lesson addition is unsuccessful", HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>(lessonResponse, HttpStatus.OK);
    }

    @PostMapping("/test")
    public ResponseEntity<String> saveTest(@RequestBody TestRequest testRequest) {
        String testResponse = adminService.addTest(testRequest);
        if (testResponse == null) {
            return new ResponseEntity<String>("test addition is unsuccessful", HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>(testResponse, HttpStatus.OK);
    }

    @PostMapping("/question")
    public ResponseEntity<String> saveQuestion(@RequestBody Question question) {
        String questionResponse = adminService.addQuestion(question);
        if (questionResponse == null) {
            return new ResponseEntity<String>("question addition is unsuccessful", HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>(questionResponse, HttpStatus.OK);
    }

    @PostMapping("/instructor")
    public ResponseEntity<String> saveInstructor(@ModelAttribute InstructorRequest instructor) throws IOException {
        String instructorResponse = adminService.addInstructor(instructor);
        if (instructorResponse == null) {
            return new ResponseEntity<String>("instructor addition is unsuccessful", HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>(instructorResponse, HttpStatus.OK);
    }
}
