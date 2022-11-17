package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.Course;
import com.robosoft.VirtualLearn.AdminPanel.entity.Category;
import com.robosoft.VirtualLearn.AdminPanel.entity.Notification;
import com.robosoft.VirtualLearn.AdminPanel.entity.SubCategory;
import com.robosoft.VirtualLearn.AdminPanel.request.*;
import com.robosoft.VirtualLearn.AdminPanel.response.*;
import com.robosoft.VirtualLearn.AdminPanel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/Categories")
    public ResponseEntity<?> getCategories()
    {
        List<Category> categories = userService.getCategories();

        if((categories) != null)
            return ResponseEntity.of(Optional.of(categories));
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/SubCategories")
    public ResponseEntity<?> getSubcategory(@RequestBody Category category)
    {
        List<SubCategory> subCategories = userService.getSubCategories(category);

        if((subCategories) != null)
            return ResponseEntity.of(Optional.of(subCategories));
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/CourseOverView")
    public ResponseEntity<?> getCourseOverview(@RequestBody Course course) {
        try {
            OverviewResponse overviewResponse = userService.getOverviewOfCourse(course.getCourseId());
            if (overviewResponse != null) {
                return ResponseEntity.of(Optional.of(overviewResponse));
            }
           return new ResponseEntity("Overview For the Course is not Available", HttpStatus.NOT_FOUND);
        }catch (Exception e)
        {
            return new ResponseEntity("Invalid Input", HttpStatus.NOT_FOUND);

        }
    }

    @GetMapping("/BasicCourses")
    public ResponseEntity<?> getBeginnerCourses(@RequestBody Category category)
    {
        try {
            List<CourseResponse> courseResponses = userService.getBasicCourses(category.getCategoryId());

            if(courseResponses.isEmpty())
            {
                return new ResponseEntity("Currently No Courses Avaialable in this Category",HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(courseResponses));

        }catch (Exception e)
        {
            return new ResponseEntity("Invalid Input",HttpStatus.BAD_REQUEST);
        }
    }  

    @GetMapping("/AdvanceCourses")
    public ResponseEntity<?> getAdvancedCourses(@RequestBody Category category)
    {
        try {
            List<CourseResponse> courseResponses = userService.getAdvanceCourses(category.getCategoryId());

            if(courseResponses.isEmpty())
            {
                return new ResponseEntity("Currently No Courses Avaialable in this Category",HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(courseResponses));

        }catch (Exception e)
        {
            return new ResponseEntity("Invalid Input",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/AllCoursesOfCategory")
    public ResponseEntity<?> getAllCourses(@RequestBody Category category)
    {
        try {
            List<AllCoursesResponse> allCourseResponses = userService.getAllCoursesOf(category.getCategoryId());

            if(allCourseResponses.isEmpty())
            {
                return new ResponseEntity("Currently No Courses Avaialable in this Category",HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(allCourseResponses));

        }catch (Exception e)
        {
            return new ResponseEntity("Invalid Input",HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/privacyPolicy")
    public ResponseEntity<?> getPrivacyPolicy() {
        try {
            String privacyPolicy = userService.getPolicy();
                return ResponseEntity.of(Optional.of(privacyPolicy));
        }catch (Exception e)
        {
            return new ResponseEntity("Privacy Policy Not Found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/TermsAndConditions")
    public ResponseEntity<?> getTermsAndConditions() {
        try {
            String termsAndConditions = userService.getTermsAndConditions();
                return ResponseEntity.of(Optional.of(termsAndConditions));
        }catch (Exception e)
        {
            return new ResponseEntity("Terms and Conditions Not Found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/OngoingCourses")
    public ResponseEntity<?> getOngoingCourses()
    {
        try {
            List<OngoingResponse> ongoingResponses = userService.getOngoingCourses();
            if(ongoingResponses.isEmpty()){
                return new ResponseEntity<>("No Ongoing Courses or The Course You Enrolled has No Chapters yet.",HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(ongoingResponses));
        }catch (Exception e) {
            return new ResponseEntity<>("Invalid Input ", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/CompletedCourses")
    public ResponseEntity<?> getCompletedCourses()
    {
        List<CompletedResponse> completedResponse = userService.getCourseCompletedResponse();
        if(completedResponse.isEmpty())
            return new ResponseEntity<>("No Completed Courses",HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(completedResponse));

    }

    @GetMapping("/CourseChapterResponse")
    public ResponseEntity<?> getCourseChapterResponse(@RequestBody CourseChapterRequest courseChapterRequest)
    {
        try {
            CourseChapterResponse  courseChapterResponse =userService.getCourseChapterResponse(courseChapterRequest.getCourseId());
            if(courseChapterResponse != null)
            {
                return ResponseEntity.of(Optional.of(courseChapterRequest));
            }
            return new ResponseEntity<>("There are No Chapters Available at the Course yet",HttpStatus.NOT_FOUND);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>("Invalid Input",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/LessonResponse")
    public ResponseEntity<?> getLessonResponse(@RequestBody LessonResponseRequest lessonResponseRequest){
        List<LessonResponse> lessonResponses= userService.getLessonResponses(lessonResponseRequest.getChapterId());
        if(lessonResponses.isEmpty())
        {
            return new ResponseEntity<>("Currently No Lessons Available in the Course",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.of(Optional.of(lessonResponses));
    }

    @GetMapping("/Continue")
    public ResponseEntity<?> getLastPlayed(@RequestBody CourseChapterRequest courseChapterRequest)
    {
        Continue c = userService.getLastPlayed(courseChapterRequest.getCourseId());

        if(c != null) {
            return ResponseEntity.of(Optional.of(c));
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //chk
    @GetMapping("/home/course")
    public ResponseEntity<?> homeTopBarData()
    {
        List<HomeResponseTopHeader> coursesList= userService.HomePageTopBar();
        if(coursesList.size() ==0)
        {
            return new ResponseEntity<String>("courses are not available", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(coursesList,HttpStatus.OK);
    }


    @GetMapping("/home/course/all")
    public ResponseEntity<?> homeAllCourses()
    {
        List<HomeAllCourse> allCourses= userService.getAllCourses();
        if(allCourses.size() ==0)
        {
            return new ResponseEntity<String>("courses are not available", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allCourses,HttpStatus.OK);
    }


    @GetMapping("/home/course/popular")
    public ResponseEntity<?> homePopularCourses()
    {
        List<HomeAllCourse> popularCourses= userService.getPopularCourses();
        if(popularCourses.size() ==0)
        {
            return new ResponseEntity<String>("courses are not available", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(popularCourses,HttpStatus.OK);
    }

    @GetMapping("/home/course/newest")
    public ResponseEntity<?> homeNewestCourses() {
        List<HomeAllCourse> newestCourses = userService.getNewCourses();
        if (newestCourses.size() == 0) {
            return new ResponseEntity<String>("courses are not available", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(newestCourses, HttpStatus.OK);
    }

    //*********************10-11-20222*****************************************
    @GetMapping("/home/course/category")
    public ResponseEntity<?> homeGetPopularCoursesOfCategory() {
        Map<Integer,List<PopularCourseInEachCategory>> coursesOfCategory = userService.popularCoursesInCategory();
        if (coursesOfCategory == null) {
            return new ResponseEntity<String>("courses are not available", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(coursesOfCategory, HttpStatus.OK);
    }


    //***********************10-11-2022***************************************
    @PostMapping("/enroll")
    public ResponseEntity<String> userEnrollment(@RequestBody EnrollmentRequest enrollmentRequest)
    {
        String enrolResponse = userService.enrollment(enrollmentRequest);
        return new ResponseEntity<>(enrolResponse, HttpStatus.OK);
    }

    @PutMapping("/pauseTime")
    public ResponseEntity<String> updateLessonCompletionStatus(@RequestBody VideoPauseRequest videoPauseRequest)
    {
        userService.updateVideoPauseTime(videoPauseRequest);
        return new ResponseEntity<>("Updated SuccessFully",HttpStatus.OK);
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> notifications()
    {
        Map<Integer, List<Notification>> notifications = userService.pullNotification();
        if(notifications.size() == 0)
        {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(notifications,HttpStatus.OK);
    }
}


