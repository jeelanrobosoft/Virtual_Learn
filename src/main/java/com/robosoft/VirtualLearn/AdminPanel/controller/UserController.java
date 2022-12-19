package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.Category;
import com.robosoft.VirtualLearn.AdminPanel.entity.SubCategory;
import com.robosoft.VirtualLearn.AdminPanel.request.CourseChapterRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.EnrollmentRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.FilterRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.VideoPauseRequest;
import com.robosoft.VirtualLearn.AdminPanel.response.*;
import com.robosoft.VirtualLearn.AdminPanel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
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


    @GetMapping("/subCategories")
    public ResponseEntity<?> getSubcategory(@RequestParam Integer categoryId) {
        List<SubCategory> subCategories = userService.getSubCategories(categoryId);

        if ((subCategories) != null)
            return ResponseEntity.of(Optional.of(subCategories));
        else
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No SubCategories added Yet"))).status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/subCategoriesWP")
    public ResponseEntity<?> getSubcategoryWithoutPagination(@RequestParam Integer categoryId) {
        List<SubCategory> subCategories = userService.getSubCategoriesWithoutPagination(categoryId);

        if ((subCategories) == null && subCategories.isEmpty() )
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No SubCategories added Yet"))).status(HttpStatus.NOT_FOUND).build();
        else
            return ResponseEntity.of(Optional.of(subCategories));

    }

    @GetMapping("/allSubCategoriesWP")
    public ResponseEntity<?> getAllSubcategoryWithoutPagination() {
        List<SubCategory> subCategories = userService.getAllSubCategoriesWithoutPagination();

        if ((subCategories) != null)
            return ResponseEntity.of(Optional.of(subCategories));
        else
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "No SubCategories added Yet"))).status(HttpStatus.NOT_FOUND).build();
    }


    @GetMapping("/courseOverView")
    public ResponseEntity<?> getCourseOverview(@RequestParam Integer courseId) {
        try {
            OverviewResponse overviewResponse = userService.getOverviewOfCourse(courseId);
            if (overviewResponse != null) {
                return ResponseEntity.of(Optional.of(overviewResponse));
            }
            return new ResponseEntity<>(Collections.singletonMap("message", "Overview For the Course is not Available"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/basicCourses")
    public ResponseEntity<?> getBeginnerCourses(@RequestParam Integer categoryId) {
        try {
            List<CourseResponse> courseResponses = userService.getBasicCourses(categoryId);

            if (courseResponses.isEmpty()) {
                return new ResponseEntity<>(Collections.singletonMap("message", "Currently No Courses Available in this Category"), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(courseResponses));

        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/advanceCourses")
    public ResponseEntity<?> getAdvancedCourses(@RequestParam Integer categoryId) {
        try {
            List<CourseResponse> courseResponses = userService.getAdvanceCourses(categoryId);

            if (courseResponses.isEmpty()) {
                return new ResponseEntity<>(Collections.singletonMap("message", "Currently No Courses Available in this Category"), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(courseResponses));

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/allCoursesOfCategory")
    public ResponseEntity<?> getAllCourses(@RequestParam Integer categoryId) {
        try {
            List<AllCoursesResponse> allCourseResponses = userService.getAllCoursesOf(categoryId);

            if (allCourseResponses.isEmpty()) {
                return new ResponseEntity<>(Collections.singletonMap("message", "Currently No Courses Available in this Category"), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(allCourseResponses));

        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/allCoursesOfSubCategory")
    public ResponseEntity<?> getAllCoursesOfSubcategory(@RequestParam Integer subCategoryId) {
        try {
            List<AllCoursesResponse> allCourseResponses = userService.getAllCoursesOfSub(subCategoryId);

            if (allCourseResponses.isEmpty()) {
                return new ResponseEntity<>(Collections.singletonMap("message", "Currently No Courses Available in this SubCategory"), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.of(Optional.of(allCourseResponses));

        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }




    @GetMapping("/checkMyCourses")
    public ResponseEntity<?> checkMyCourses()
    {
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", userService.checkMyCourses())));
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

    @GetMapping("/courseChapterResponse")
    public ResponseEntity<?> getCourseChapterResponse(@RequestParam Integer courseId) {
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
    public ResponseEntity<?> getLastPlayed(@RequestParam Integer courseId) {
        Continue c = userService.getLastPlayed(courseId);
        if (c != null) {
            return ResponseEntity.of(Optional.of(c));
        }
        return new ResponseEntity<>(Collections.singletonMap("message", "null"), HttpStatus.NOT_FOUND);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String searchKey) {
        try {
            List<AllCoursesResponse> allCoursesResponses = userService.searchCourses(searchKey);

            if (allCoursesResponses.isEmpty())
                return new ResponseEntity<>(Collections.singletonMap("message", "No Matching Course"), HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(allCoursesResponses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/searchOfCategory")
    public ResponseEntity<?> searchOfCategory(@RequestParam Integer categoryId ,@RequestParam String searchKey) {
        try {
            List<AllCoursesResponse> allCoursesResponses = userService.searchCoursesOfCategory(categoryId,searchKey);

            if (allCoursesResponses.isEmpty())
                return new ResponseEntity<>(Collections.singletonMap("message", "No Matching Course"), HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(allCoursesResponses, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.singletonMap("message", "Invalid Input"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/applyFilter")
    public ResponseEntity<?> applyFilter(@RequestBody FilterRequest filterRequest) {
        List<AllCoursesResponse> allCoursesResponses = userService.searchFilter(filterRequest);
        if (allCoursesResponses == null || allCoursesResponses.isEmpty()) {
//            return new ResponseEntity<>(Collections.singletonMap("message", "No Matching Course"), HttpStatus.OK);
            return null;
        }
        return new ResponseEntity<>(allCoursesResponses, HttpStatus.OK);
    }

    @GetMapping("/searchByKeyword")
    public ResponseEntity<?> searchByKeyword(@RequestParam String keyword)
    {
        List<AllCoursesResponse> allCoursesResponses = userService.searchByKeyword(keyword);
        if (allCoursesResponses == null || allCoursesResponses.isEmpty()) {
            return new ResponseEntity<>(Collections.singletonMap("message", "No Matching Course"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allCoursesResponses, HttpStatus.OK);
    }

    @PutMapping("/keywords")
    public ResponseEntity<?> updateSearchCount(@RequestBody CourseChapterRequest courseChapterRequest){
        userService.topSearches(courseChapterRequest);
        return new ResponseEntity<>(Collections.singletonMap("message", "Updated"), HttpStatus.OK);
    }

    @GetMapping("/topSearches")
    public ResponseEntity<?> getTopSearches()
    {
        List<KeywordSearchResponse> keywordsList = userService.searchKeywords();
        if(keywordsList.size() == 0)
        {
            return new ResponseEntity<>(Collections.singletonMap("message", "No Top Searches"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(keywordsList, HttpStatus.OK);
    }

    @GetMapping("/home/course")
    public ResponseEntity<?> homeTopBarData() {
        List<HomeResponseTopHeader> coursesList =  userService.HomePageTopBar();
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

    //pagination
    @GetMapping("/home/course/pagination")
    public ResponseEntity<?> homeTopBarDataPagination(@RequestParam Integer pageLimit) {
        List<HomeResponseTopHeader> coursesList =  userService.HomePageTopBarPagination(pageLimit);
        if (coursesList == null) {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "courses are not available")));
        }
        return ResponseEntity.of(Optional.of(coursesList));
    }

    @GetMapping("/home/course/all/pagination")
    public ResponseEntity<?> homeAllCoursesPagination(@RequestParam Integer pageLimit) {
        List<HomeAllCourse> allCourses = userService.getAllCoursesPagination(pageLimit);
        if (allCourses.size() == 0) {
            return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "courses are not available")));
        }
        return ResponseEntity.of(Optional.of(allCourses));
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> userEnrollment(@RequestBody EnrollmentRequest enrollmentRequest) {
        String enrolResponse = userService.enrollment(enrollmentRequest);
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", enrolResponse)));
    }

    @PutMapping("/pauseTime")
    public ResponseEntity<?> updateLessonCompletionStatus(@RequestBody VideoPauseRequest videoPauseRequest) throws IOException, ParseException {
        userService.updateVideoPauseTime(videoPauseRequest);
        return ResponseEntity.of(Optional.of(Collections.singletonMap("message", "Updated SuccessFully")));
    }
}


