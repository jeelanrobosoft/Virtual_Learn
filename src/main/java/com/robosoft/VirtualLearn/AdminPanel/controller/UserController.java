package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.Category;
import com.robosoft.VirtualLearn.AdminPanel.entity.Course;
import com.robosoft.VirtualLearn.AdminPanel.entity.SubCategory;
import com.robosoft.VirtualLearn.AdminPanel.request.*;
import com.robosoft.VirtualLearn.AdminPanel.response.*;
import com.robosoft.VirtualLearn.AdminPanel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<Category> categories = userService.getCategories();

        if ((categories) != null)
            return ResponseEntity.of(Optional.of(categories));
        else {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No Categories added Yet"))).status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/categoriesWP")
    public ResponseEntity<?> getCategoriesWithoutPagination() {
        List<Category> categories = userService.getCategoriesWithoutPagination();

        if ((categories) != null)
            return ResponseEntity.of(Optional.of(categories));
        else {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No Categories added Yet"))).status(HttpStatus.NOT_FOUND).build();
        }
    }


    @GetMapping("/subCategories/{categoryId}")
    public ResponseEntity<?> getSubcategory(@PathVariable Integer categoryId) {
        List<SubCategory> subCategories = userService.getSubCategories(categoryId);

        if ((subCategories) != null)
            return ResponseEntity.of(Optional.of(subCategories));
        else
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No SubCategories added Yet"))).status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/subCategoriesWP")
    public ResponseEntity<?> getSubcategoryWithoutPagination(@RequestBody Category category) {
        List<SubCategory> subCategories = userService.getSubCategoriesWithoutPagination(category);

        if ((subCategories) != null)
            return ResponseEntity.of(Optional.of(subCategories));
        else
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No SubCategories added Yet"))).status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/allSubCategoriesWP")
    public ResponseEntity<?> getAllSubcategoryWithoutPagination() {
        List<SubCategory> subCategories = userService.getAllSubCategoriesWithoutPagination();

        if ((subCategories) != null)
            return ResponseEntity.of(Optional.of(subCategories));
        else
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No SubCategories added Yet"))).status(HttpStatus.NOT_FOUND).build();
    }


    @GetMapping("/courseOverView/{courseId}")
    public ResponseEntity<?> getCourseOverview(@PathVariable Integer courseId) {
        try {
            OverviewResponse overviewResponse = userService.getOverviewOfCourse(courseId);
            if (overviewResponse != null) {
                return ResponseEntity.of(Optional.of(overviewResponse));
            }
            return new ResponseEntity(Collections.singletonMap("message", "Overview For the Course is not Available"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity(Collections.singletonMap("message", "Invalid Input"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/basicCourses")
    public ResponseEntity<?> getBeginnerCourses(@RequestBody Category category) {
        try {
            List<CourseResponse> courseResponses = userService.getBasicCourses(category.getCategoryId());

            if (courseResponses.isEmpty()) {
                return new ResponseEntity(Collections.singletonMap("message", "Currently No Courses Available in this Category"), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(courseResponses));

        } catch (Exception e) {
            return new ResponseEntity(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/advanceCourses")
    public ResponseEntity<?> getAdvancedCourses(@RequestBody Category category) {
        try {
            List<CourseResponse> courseResponses = userService.getAdvanceCourses(category.getCategoryId());

            if (courseResponses.isEmpty()) {
                return new ResponseEntity(Collections.singletonMap("message", "Currently No Courses Available in this Category"), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(courseResponses));

        } catch (Exception e) {
            return new ResponseEntity(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/allCoursesOfCategory")
    public ResponseEntity<?> getAllCourses(@RequestBody Category category) {
        try {
            List<AllCoursesResponse> allCourseResponses = userService.getAllCoursesOf(category.getCategoryId());

            if (allCourseResponses.isEmpty()) {
                return new ResponseEntity(Collections.singletonMap("message", "Currently No Courses Available in this Category"), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(allCourseResponses));

        } catch (Exception e) {
            return new ResponseEntity(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/allCoursesOfCategory/{categoryId}")
    public ResponseEntity<?> getAllCoursesOfSubcategory(@PathVariable Integer subCategoryId) {
        try {
            List<AllCoursesResponse> allCourseResponses = userService.getAllCoursesOfSub(subCategoryId);

            if (allCourseResponses.isEmpty()) {
                return new ResponseEntity(Collections.singletonMap("message", "Currently No Courses Available in this SubCategory"), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(allCourseResponses));

        } catch (Exception e) {
            return new ResponseEntity(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/privacyPolicy")
    public ResponseEntity<?> getPrivacyPolicy() {
        try {
            String privacyPolicy = userService.getPolicy();
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", privacyPolicy)));
        } catch (Exception e) {
            return new ResponseEntity(Collections.singletonMap("message", "Privacy Policy Not Found"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/termsAndConditions")
    public ResponseEntity<?> getTermsAndConditions() {
        try {
            String termsAndConditions = userService.getTermsAndConditions();
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", termsAndConditions)));
        } catch (Exception e) {
            return new ResponseEntity(Collections.singletonMap("message", "Terms and Conditions Not Found"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/ongoingCourses")
    public ResponseEntity<?> getOngoingCourses() {
        try {
            List<OngoingResponse> ongoingResponses = userService.getOngoingCourses();
            if (ongoingResponses.isEmpty()) {
                return new ResponseEntity<>(Collections.singletonMap("message", "No Ongoing Courses or The Course You Enrolled has No Chapters yet."), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(ongoingResponses));
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid Input ", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/completedCourses")
    public ResponseEntity<?> getCompletedCourses() {
        List<CompletedResponse> completedResponse = userService.getCourseCompletedResponse();
        if (completedResponse.isEmpty())
            return new ResponseEntity<>(Collections.singletonMap("message", "No Completed Courses"), HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(completedResponse));

    }

    @GetMapping("/courseChapterResponse/{courseId}")
    public ResponseEntity<?> getCourseChapterResponse(@PathVariable Integer courseId) {
        try {
            CourseChapterResponse courseChapterResponse = userService.getCourseChapterResponse(courseId);
            if (courseChapterResponse != null) {
                return ResponseEntity.of(Optional.of(courseChapterResponse));
            }
            return new ResponseEntity<>(Collections.singletonMap("message", "There are No Chapters Available at the Course yet"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/continue")
    public ResponseEntity<?> getLastPlayed(@RequestBody CourseChapterRequest courseChapterRequest) {
        Continue c = userService.getLastPlayed(courseChapterRequest.getCourseId());

        if (c != null) {
            return ResponseEntity.of(Optional.of(c));
        }
        return new ResponseEntity<>(Collections.singletonMap("message", "null"), HttpStatus.NOT_FOUND);
    }

    @GetMapping("/search/{searchKey}")
    public ResponseEntity<?> search(@PathVariable String searchKey) {
        try {
            List<AllCoursesResponse> allCoursesResponses = userService.searchCourses(searchKey);

            if (allCoursesResponses.isEmpty())
                return new ResponseEntity<>(Collections.singletonMap("message", "No Matching Course"), HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(allCoursesResponses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("applyFilter")
    public ResponseEntity<?> applyFilter(@RequestBody FilterRequest filterRequest) {
        List<AllCoursesResponse> allCoursesResponses = userService.searchFilter(filterRequest);
        if (allCoursesResponses == null || allCoursesResponses.isEmpty()) {
            return new ResponseEntity<>(Collections.singletonMap("message", "No Matching Course"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allCoursesResponses, HttpStatus.OK);
    }

    @GetMapping("/home/course")
    public ResponseEntity<?> homeTopBarData() {
        List<HomeResponseTopHeader> coursesList = new ArrayList<>();
        coursesList = userService.HomePageTopBar();
        if (coursesList == null) {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "courses are not available")));
        }
        return ResponseEntity.of(Optional.of(coursesList));
    }

    @GetMapping("/home/course/all")
    public ResponseEntity<?> homeAllCourses() {
        List<HomeAllCourse> allCourses = userService.getAllCourses();
        if (allCourses.size() == 0) {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "courses are not available")));
        }
        return ResponseEntity.of(Optional.of(allCourses));
    }

    @GetMapping("/home/course/popular")
    public ResponseEntity<?> homePopularCourses() {
        List<HomeAllCourse> popularCourses = userService.getPopularCourses();
        if (popularCourses.size() == 0) {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "courses are not available")));
        }
        return ResponseEntity.of(Optional.of(popularCourses));
    }

    @GetMapping("/home/course/newest")
    public ResponseEntity<?> homeNewestCourses() {
        List<HomeAllCourse> newestCourses = userService.getNewCourses();
        if (newestCourses.size() == 0) {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "courses are not available")));
        }
        return ResponseEntity.of(Optional.of(newestCourses));
    }

    @GetMapping("/home/course/category")
    public ResponseEntity<?> homeGetPopularCoursesOfCategory() {
        List<TopCourseResponse> coursesOfCategory = userService.popularCoursesInCategory();
        if (coursesOfCategory == null) {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "courses are not available")));
        }
        return ResponseEntity.of(Optional.of(coursesOfCategory));
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> userEnrollment(@RequestBody EnrollmentRequest enrollmentRequest) {
        String enrolResponse = userService.enrollment(enrollmentRequest);
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", enrolResponse)));
    }

    @PutMapping("/pauseTime")
    public ResponseEntity<?> updateLessonCompletionStatus(@RequestBody VideoPauseRequest videoPauseRequest) {
        userService.updateVideoPauseTime(videoPauseRequest);
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Updated SuccessFully")));
    }
}


