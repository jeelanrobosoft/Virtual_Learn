package com.robosoft.VirtualLearn.AdminPanel.service;

import com.robosoft.VirtualLearn.AdminPanel.entity.*;
import com.robosoft.VirtualLearn.AdminPanel.request.CourseChapterRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.EnrollmentRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.FilterRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.VideoPauseRequest;
import com.robosoft.VirtualLearn.AdminPanel.response.*;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.DOWNLOAD_URL;
import static com.robosoft.VirtualLearn.AdminPanel.entity.PushNotification.sendPushNotification;

import static com.robosoft.VirtualLearn.AdminPanel.entity.PushNotification.sendPushNotification;

@Service
public class UserService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    int pages = 2;
    int lowerLimit = 0;
    int upperLimit = pages;
    int topHeaderLowerLimit = 0;

    int topHeaderUpperLimit;
    int allCourseLowerLimit = 0;
    int allCourseUpperLimit;

    @Autowired
    FinalTestService finalTestService;

    public List<Category> getCategories() {
        List<Category> categories = jdbcTemplate.query("SELECT * FROM category limit ?,?", (rs, rowNum) ->
                new Category(rs.getInt("categoryId"), rs.getString("categoryName"), rs.getString("categoryPhoto")), lowerLimit, upperLimit);
        lowerLimit = lowerLimit + pages;

        if (categories.size() == 0) {
            lowerLimit = 0;
            return jdbcTemplate.query("SELECT * FROM category limit ?,?", (rs, rowNum) ->
                    new Category(rs.getInt("categoryId"), rs.getString("categoryName"), rs.getString("categoryPhoto")), lowerLimit, upperLimit);
        }
        return categories;
    }

    public List<Category> getCategoriesWithoutPagination() {
        List<Integer> categoryIds = jdbcTemplate.queryForList("SELECT categoryId FROM category", Integer.class);
        List<Category> categories = new ArrayList<>();
        for (Integer categoryId : categoryIds) {
            try {

                int categoryCount = jdbcTemplate.queryForObject("SELECT count(categoryId) FROM course WHERE categoryId = ? AND publishStatus = true", Integer.class, categoryId);
                if (categoryCount != 0)
                    categories.add(jdbcTemplate.queryForObject("SELECT * FROM category WHERE categoryId = ?", new BeanPropertyRowMapper<>(Category.class), categoryId));
            } catch (Exception ignored) {

            }
        }
        return categories;
    }


    public List<SubCategory> getSubCategories(Integer categoryId) {
        List<SubCategory> subCategories = jdbcTemplate.query("SELECT * FROM subCategory WHERE categoryId = ? limit ?,?", (rs, rowNum) ->
                new SubCategory(rs.getInt("categoryId"), rs.getInt("subCategoryId"), rs.getString("subCategoryName")), categoryId, lowerLimit, upperLimit);
        lowerLimit = lowerLimit + pages;
        if (subCategories.size() == 0) {
            lowerLimit = 0;
            return jdbcTemplate.query("SELECT * FROM subCategory WHERE categoryId = ? limit ?,?", (rs, rowNum) ->
                    new SubCategory(rs.getInt("categoryId"), rs.getInt("subCategoryId"), rs.getString("subCategoryName")), categoryId, lowerLimit, upperLimit);
        }
        return subCategories;
    }

    public List<SubCategory> getSubCategoriesWithoutPagination(Integer categoryId) {
        List<Integer> subCategoryIds = jdbcTemplate.queryForList("SELECT subCategoryId FROM subCategory WHERE categoryId = ? ", Integer.class, categoryId);
        List<SubCategory> subCategories = new ArrayList<>();
        for (Integer subCategoryId : subCategoryIds) {
            try {

                int subCategoryCount = jdbcTemplate.queryForObject("SELECT COUNT(subCategoryId) FROM course WHERE subCategoryId = ? AND publishStatus = true", Integer.class, subCategoryId);
                if (subCategoryCount != 0)
                    subCategories.add(jdbcTemplate.queryForObject("SELECT * FROM subCategory WHERE subCategoryId = ?", new BeanPropertyRowMapper<>(SubCategory.class), subCategoryId));
            } catch (Exception ignored) {

            }
        }
        return subCategories;
    }

    public List<SubCategory> getAllSubCategoriesWithoutPagination() {
        List<Integer> subCategoryIds = jdbcTemplate.queryForList("SELECT subCategoryId FROM subCategory ", Integer.class);
        List<SubCategory> subCategories = new ArrayList<>();
        for (Integer subCategoryId : subCategoryIds) {
            try {

                int subCategoryCount = jdbcTemplate.queryForObject("SELECT COUNT(subCategoryId) FROM course WHERE subCategoryId = ? AND publishStatus = true", Integer.class, subCategoryId);
                if (subCategoryCount != 0)
                    subCategories.add(jdbcTemplate.queryForObject("SELECT * FROM subCategory WHERE subCategoryId = ?", new BeanPropertyRowMapper<>(SubCategory.class), subCategoryId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return subCategories;
    }

    public Counts getCount(Integer courseId) {
        Counts counts = new Counts();
        List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId FROM chapter WHERE courseId = ? ORDER BY chapterId", Integer.class, courseId);
        counts.setChapterCount(chapterIds.size());
        int lessonCount = 0;
        int tesCount = 0;
        for (int chapterId : chapterIds) {
            try {
                lessonCount += jdbcTemplate.queryForObject("SELECT COUNT(lessonId) FROM lesson WHERE chapterId = ?", Integer.class, chapterId);
            } catch (Exception e) {
                lessonCount += 0;
            }
            try {
                tesCount += jdbcTemplate.queryForObject("SELECT COUNT(testId) FROM test WHERE chapterId = ?", Integer.class, chapterId);
            } catch (Exception e) {
                tesCount += 0;
            }
        }
        counts.setLessonCount(lessonCount);
        counts.setTestCount(tesCount);
        return counts;
    }

    public Counts getCountEnrolled(Integer courseId, String userName) {
        Counts counts = new Counts();
        List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId FROM chapterProgress WHERE courseId = ? AND userName = ? ORDER BY chapterId", Integer.class, courseId, userName);
        counts.setChapterCount(chapterIds.size());
        int lessonCount = 0;
        int tesCount = 0;
        for (int chapterId : chapterIds) {
            try {
                lessonCount += jdbcTemplate.queryForObject("SELECT COUNT(lessonId) FROM lessonProgress WHERE chapterId = ? AND userName = ?", Integer.class, chapterId, userName);
            } catch (Exception e) {
                lessonCount += 0;
            }
            try {
                tesCount += jdbcTemplate.queryForObject("SELECT COUNT(testId) FROM test WHERE chapterId = ?", Integer.class, chapterId);
            } catch (Exception e) {
                tesCount += 0;
            }
        }
        counts.setLessonCount(lessonCount);
        counts.setTestCount(tesCount);
        return counts;
    }


    public OverviewResponse getOverviewOfCourse(int courseId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {

            try {
                jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE userName = ? AND courseId = ? AND deleteStatus = false", new BeanPropertyRowMapper<>(Enrollment.class), userName, courseId);
                OverviewResponse overviewResponse = jdbcTemplate.queryForObject("SELECT overView.courseId,courseName,coursePhoto,categoryName,courseTagLine,overView.description,courseDuration,fullName as instructorName,designation,url,profilePhoto,admin.description AS instructorDescription FROM overView INNER JOIN admin ON overView.instructorId = admin.emailId INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(OverviewResponse.class), courseId);
                List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId FROM chapter WHERE courseId = ? ORDER BY chapterId", Integer.class, courseId);
                int lessonId = 0;
                for (int chapterId : chapterIds) {
                    try {
                        lessonId = jdbcTemplate.queryForObject("SELECT min(lessonId) FROM lesson WHERE chapterId = ?", Integer.class, chapterId);
                        break;
                    } catch (Exception ignored) {
                    }
                }
                Counts count = this.getCountEnrolled(courseId,userName);
                assert overviewResponse != null;
                overviewResponse.setChapterCount(count.getChapterCount());
                overviewResponse.setLessonCount(count.getLessonCount());
                overviewResponse.setTestCount(count.getTestCount());
                String learningOutcome = jdbcTemplate.queryForObject("SELECT learningOutCome FROM overView WHERE courseId = ?", String.class, courseId);
                String requirement = jdbcTemplate.queryForObject("SELECT requirements FROM overView WHERE courseId = ?", String.class, courseId);
                if (learningOutcome != null && requirement != null) {
                    overviewResponse.setLearningOutCome(Arrays.asList(learningOutcome.split("\n|\\. |\\.")));
                    overviewResponse.setRequirements(Arrays.asList(requirement.split("\n|\\. |\\.")));
                }
                try {
                    overviewResponse.setPreviewVideo(jdbcTemplate.queryForObject("SELECT videoLink FROM lesson WHERE lessonId = ?", String.class, lessonId));
                    overviewResponse.setPreviewVideoName(jdbcTemplate.queryForObject("SELECT lessonName FROM lesson WHERE lessonId = ?", String.class, lessonId));
                    overviewResponse.setPreviewVideoDuration(jdbcTemplate.queryForObject("SELECT lessonDuration FROM lesson WHERE lessonId = ?", String.class, lessonId));
                } catch (Exception ignored) {

                }
                overviewResponse.setEnrolled(true);
                return overviewResponse;
            } catch (Exception e) {

                OverviewResponse overviewResponse = jdbcTemplate.queryForObject("SELECT overView.courseId,courseName,coursePhoto,categoryName,courseTagLine,overView.description,courseDuration,admin.fullName as instructorName,admin.designation,admin.url,admin.profilePhoto,admin.description AS instructorDescription FROM overView INNER JOIN admin ON overView.instructorId = admin.emailId  INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(OverviewResponse.class), courseId);
                int lessonId = 0;
                List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId FROM chapter WHERE courseId = ? ORDER BY chapterId", Integer.class, courseId);
                for (Integer chapterId : chapterIds) {
                    try {
                        lessonId = jdbcTemplate.queryForObject("SELECT min(lessonId) FROM lesson WHERE chapterId = ?", Integer.class, chapterId);
                        break;
                    } catch (Exception ignored) {
                    }
                }
                Counts count = this.getCount(courseId);
                assert overviewResponse != null;
                overviewResponse.setChapterCount(count.getChapterCount());
                overviewResponse.setLessonCount(count.getLessonCount());
                overviewResponse.setTestCount(count.getTestCount());
                String learningOutcome = jdbcTemplate.queryForObject("SELECT learningOutCome FROM overView WHERE courseId = ?", String.class, courseId);
                String requirement = jdbcTemplate.queryForObject("SELECT requirements FROM overView WHERE courseId = ?", String.class, courseId);
                if (learningOutcome != null && requirement != null) {
                    overviewResponse.setLearningOutCome(Arrays.asList(learningOutcome.split("\n|\\. |\\.")));
                    overviewResponse.setRequirements(Arrays.asList(requirement.split("\n|\\. |\\.")));
                }
                try {
                    overviewResponse.setPreviewVideo(jdbcTemplate.queryForObject("SELECT videoLink FROM lesson WHERE lessonId = ?", String.class, lessonId));
                    overviewResponse.setPreviewVideoName(jdbcTemplate.queryForObject("SELECT lessonName FROM lesson WHERE lessonId = ?", String.class, lessonId));
                    overviewResponse.setPreviewVideoDuration(jdbcTemplate.queryForObject("SELECT lessonDuration FROM lesson WHERE lessonId = ?", String.class, lessonId));
                } catch (Exception ignored) {

                }

                overviewResponse.setEnrolled(false);
                return overviewResponse;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public List<CourseResponse> getBasicCourses(int categoryId) {

        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE categoryId = ? AND publishStatus = true", Integer.class, categoryId);
        List<CourseResponse> courseResponses = new ArrayList<>();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        for (Integer courseId : courseIds) {
            System.out.println(courseId);
            try {
                jdbcTemplate.queryForObject("SELECT courseId FROM enrollment WHERE userName = ? AND courseId = ?", new BeanPropertyRowMapper<>(Enrollment.class), userName, courseId);
            } catch (Exception e) {
                try {
                    CourseResponse courseResponse = jdbcTemplate.queryForObject("SELECT course.courseId,coursePhoto,courseName,previewVideo,courseDuration FROM overView INNER JOIN course ON course.courseId = overView.courseId WHERE overView.courseId = ? AND difficultyLevel = 'Beginner'", new BeanPropertyRowMapper<>(CourseResponse.class), courseId);
                    Counts count = getCount(courseId);
                    if (courseResponse != null) {
                        courseResponse.setChapterCount(count.getChapterCount());
                    }
                    courseResponses.add(courseResponse);
                } catch (Exception ignored) {
                }
            }
        }
        return courseResponses;
    }

    public List<CourseResponse> getAdvanceCourses(int categoryId) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE  categoryId = ? AND publishStatus = true", Integer.class, categoryId);
        List<CourseResponse> courseResponses = new ArrayList<>();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        for (Integer courseId : courseIds) {
            try {
                jdbcTemplate.queryForObject("SELECT * FROM enrollment WHERE userName = ? AND courseId = ?", new BeanPropertyRowMapper<>(Enrollment.class), userName, courseId);
                System.out.println(courseId);
            } catch (Exception exception) {
                try {
                    CourseResponse courseResponse = jdbcTemplate.queryForObject("SELECT course.courseId,coursePhoto,courseName,previewVideo,courseDuration FROM overView INNER JOIN course ON course.courseId = overView.courseId WHERE course.courseId = ? AND difficultyLevel = 'Advanced'", new BeanPropertyRowMapper<>(CourseResponse.class), courseId);
                    Counts count = getCount(courseId);
                    if (courseResponse != null) {
                        courseResponse.setChapterCount(count.getChapterCount());
                    }
                    courseResponses.add(courseResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return courseResponses;
    }

    public List<AllCoursesResponse> getAllCoursesOf(int categoryId) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE category.categoryId = " + categoryId + " ", Integer.class);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        for (Integer courseId : courseIds) {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT course.courseId,coursePhoto,courseName,category.categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
            if (allCoursesResponse != null) {
                allCoursesResponse.setChapterCount(getCount(courseId).getChapterCount());
            }
            allCoursesResponses.add(allCoursesResponse);
        }
        return allCoursesResponses;
    }

    public List<AllCoursesResponse> getAllCoursesOfSub(int subCategoryId) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE subCategoryId = ? AND publishStatus = true", Integer.class, subCategoryId);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        for (Integer courseId : courseIds) {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT course.courseId,coursePhoto,courseName,category.categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
            if (allCoursesResponse != null) {
                allCoursesResponse.setChapterCount(getCount(courseId).getChapterCount());
            }
            allCoursesResponses.add(allCoursesResponse);
        }
        return allCoursesResponses;
    }

    public String getPolicy() {
        return jdbcTemplate.queryForObject("SELECT privacyPolicy FROM policy WHERE policyId=(SELECT max(policyId) FROM policy)", String.class);
    }

    public String getTermsAndConditions() {
        return jdbcTemplate.queryForObject("SELECT termsAndConditions FROM policy WHERE policyId=(SELECT max(policyId) FROM policy)", String.class);
    }

    public List<OngoingResponse> getOngoingCourses() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<OngoingResponse> ongoingResponses = new ArrayList<>();
        List<Integer> courseId = jdbcTemplate.queryForList("SELECT courseId FROM enrollment WHERE  userName = ? AND deleteStatus = false", Integer.class, userName);

        for (Integer i : courseId) {
            Integer completedChapter = jdbcTemplate.queryForObject("SELECT count(chapterId) FROM chapterProgress WHERE courseId = ? AND userName = ? AND chapterCompletedStatus = true", Integer.class, i, userName);
            Integer totalChapter = jdbcTemplate.queryForObject("SELECT count(chapterId) FROM chapterProgress WHERE courseId = ? AND userName = ?", Integer.class, i, userName);
            if (completedChapter != null && totalChapter != null) {
                if (completedChapter < totalChapter) {
                    OngoingResponse ongoingResponse = jdbcTemplate.queryForObject("SELECT courseId,courseName,coursePhoto FROM course WHERE courseId = ?", new BeanPropertyRowMapper<>(OngoingResponse.class), i);
                    if (ongoingResponse != null) {
                        ongoingResponse.setCompletedChapter(completedChapter);
                        ongoingResponse.setTotalChapter(totalChapter);
                    }
                    ongoingResponses.add(ongoingResponse);
                }
            }
        }
        return ongoingResponses;
    }

    public List<CompletedResponse> getCourseCompletedResponse() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return jdbcTemplate.query("SELECT course.courseId,courseName,coursePercentage,coursePhoto FROM course INNER JOIN courseProgress ON course.courseId = courseProgress.courseId AND courseProgress.userName = ? AND courseProgress.courseCompletedStatus = true", new BeanPropertyRowMapper<>(CompletedResponse.class), userName);
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean checkMyCourses() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            jdbcTemplate.queryForObject("SELECT DISTINCT userName FROM enrollment WHERE userName = ? AND deleteStatus = false ", String.class, userName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public CourseChapterResponse getCourseChapterResponse(Integer courseId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            jdbcTemplate.queryForObject("SELECT courseId FROM enrollment WHERE userName = ? AND courseId = ? AND deleteStatus = false", new BeanPropertyRowMapper<>(Enrollment.class), userName, courseId);
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId from chapter WHERE courseId = ?", Integer.class, courseId);
            CourseChapterResponse courseChapterResponse = jdbcTemplate.queryForObject("SELECT course.courseId,courseName,categoryName,courseDuration,courseCompletedStatus FROM overView INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId INNER JOIN courseProgress on course.courseId = courseProgress.courseId AND userName = ?", new BeanPropertyRowMapper<>(CourseChapterResponse.class), courseId, userName);
            Counts counts = this.getCountEnrolled(courseId, userName);
            if (courseChapterResponse != null) {
                courseChapterResponse.setChapterCount(counts.getChapterCount());
            }
            assert courseChapterResponse != null;
            courseChapterResponse.setLessonCount(counts.getLessonCount());
            courseChapterResponse.setTestCount(counts.getTestCount());
            courseChapterResponse.setEnrolled(true);
            String courseDuration = courseChapterResponse.getCourseDuration();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date1 = timeFormat.parse(courseDuration);
            long sum = date1.getTime();
            List<ChapterResponse> chapterResponses = new ArrayList<>();
            for (Integer i : chapterIds) {
                try {
                    chapterResponses.add(getChapterResponse(userName, i));
                    String testDuration = jdbcTemplate.queryForObject("SELECT testDuration FROM test WHERE chapterId = ?", String.class, i);
                    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date2 = timeFormat.parse(testDuration);
                    sum += date2.getTime();
                } catch (Exception e) {
                    Logger logger = LoggerFactory.getLogger(UserService.class);
                    logger.info("Un resolved exception");
                }
            }
            String totalDuration = timeFormat.format(new Date(sum));
            courseChapterResponse.setTotalDuration(totalDuration);
            courseChapterResponse.setChapterResponses(chapterResponses);
            if (courseChapterResponse.getCourseCompletedStatus()) {
                try {
                    courseChapterResponse.setCoursePercentage(jdbcTemplate.queryForObject("SELECT coursePercentage FROM courseProgress WHERE userName = ? AND courseId = ?", Float.class, userName, courseId));
                    courseChapterResponse.setJoinedDate(jdbcTemplate.queryForObject("SELECT joinDate FROM enrollment WHERE userName = ? AND courseId = ?", String.class, userName, courseId));
                    courseChapterResponse.setCompletedDate(jdbcTemplate.queryForObject("SELECT completedDate FROM enrollment WHERE userName = ? AND courseId = ?", String.class, userName, courseId));
                    courseChapterResponse.setCertificateUrl(jdbcTemplate.queryForObject("SELECT certificateUrl FROM certificate WHERE userName = ? AND courseId = ?", String.class, userName, courseId));
                } catch (Exception ignored) {

                }
            }
            return courseChapterResponse;
        } catch (Exception e) {
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId from chapter WHERE courseId = ? order by chapterNumber", Integer.class, courseId);
            CourseChapterResponse courseChapterResponse = jdbcTemplate.queryForObject("SELECT course.courseId, courseName,categoryName,courseDuration FROM overView INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(CourseChapterResponse.class), courseId);
            Counts counts = this.getCount(courseId);
            if (courseChapterResponse != null) {
                courseChapterResponse.setChapterCount(counts.getChapterCount());
            }
            if (courseChapterResponse != null) {
                courseChapterResponse.setLessonCount(counts.getLessonCount());
            }
            if (courseChapterResponse != null) {
                courseChapterResponse.setTestCount(counts.getTestCount());
            }
            List<ChapterResponse> chapterResponses = new ArrayList<>();
            if (courseChapterResponse != null) {
                courseChapterResponse.setEnrolled(false);
            }
            for (Integer i : chapterIds) {
                chapterResponses.add(getChapterResponse(userName, i));
            }
            if (courseChapterResponse != null) {
                courseChapterResponse.setChapterResponses(chapterResponses);
            }
            return courseChapterResponse;
        }
    }

    public ChapterResponse getChapterResponse(String userName, Integer chapterId) {
        try {
            ChapterResponse chapterResponse = jdbcTemplate.queryForObject("SELECT chapter.chapterId,chapterNumber,chapterName,chapterCompletedStatus,chapterStatus FROM chapter INNER JOIN chapterProgress ON chapter.chapterId = chapterProgress.chapterId WHERE chapter.chapterId = ? AND chapterProgress.userName = ?", new BeanPropertyRowMapper<>(ChapterResponse.class), chapterId, userName);
            List<LessonResponse> lessonResponses = getLessonResponses(chapterId);
            try {
                Integer testId = jdbcTemplate.queryForObject("SELECT testId FROM test WHERE chapterId = ?", Integer.class, chapterId);
                Integer questionCount = jdbcTemplate.queryForObject("SELECT count(questionId) FROM question WHERE testId = ?", Integer.class, testId);
                ChapterResponse chapterResponse1 = jdbcTemplate.queryForObject("SELECT test.testId,testName,testDuration,chapterTestPercentage FROM test INNER JOIN chapterProgress ON test.testId = chapterProgress.testId AND chapterProgress.userName = ? AND chapterProgress.chapterId = ?", new BeanPropertyRowMapper<>(ChapterResponse.class), userName, chapterId);
                if (chapterResponse1 != null && chapterResponse != null) {
                    chapterResponse.setTestId(chapterResponse1.getTestId());
                    chapterResponse.setTestName(chapterResponse1.getTestName());
                    chapterResponse.setTestDuration(chapterResponse1.getTestDuration());
                    chapterResponse.setChapterTestPercentage(chapterResponse1.getChapterTestPercentage());
                    chapterResponse.setQuestionCount(questionCount);
                }
            } catch (Exception ignored) {
            }
            if (chapterResponse != null) {
                chapterResponse.setLessonResponses(lessonResponses);
            }
            return chapterResponse;

        } catch (Exception e) {
            ChapterResponse chapterResponse = jdbcTemplate.queryForObject("SELECT chapter.chapterId,chapterNumber,chapterName FROM chapter WHERE chapterId = ?", new BeanPropertyRowMapper<>(ChapterResponse.class), chapterId);
            List<LessonResponse> lessonResponses = getLessonResponses(chapterId);
            try {
                Integer testId = jdbcTemplate.queryForObject("SELECT testId FROM test WHERE chapterId = ?", Integer.class, chapterId);
                Integer questionCount = jdbcTemplate.queryForObject("SELECT count(questionId) FROM question WHERE testId = ?", Integer.class, testId);
                ChapterResponse chapterResponse1 = jdbcTemplate.queryForObject("SELECT test.testId,testName,testDuration FROM test test.chapterId = ?", new BeanPropertyRowMapper<>(ChapterResponse.class), chapterId);
                if (chapterResponse1 != null && chapterResponse != null) {
                    chapterResponse.setTestId(chapterResponse1.getTestId());
                    chapterResponse.setTestName(chapterResponse1.getTestName());
                    chapterResponse.setTestDuration(chapterResponse1.getTestDuration());
                    chapterResponse.setQuestionCount(questionCount);
                    chapterResponse.setChapterStatus(false);
                }
            } catch (Exception exception) {
                Logger logger = LoggerFactory.getLogger(UserService.class);
                logger.info("Un resolved exception");
            }
            if (chapterResponse != null) {
                chapterResponse.setLessonResponses(lessonResponses);
            }
            return chapterResponse;
        }
    }

    public List<LessonResponse> getLessonResponses(Integer chapterId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Integer> lessonIds = jdbcTemplate.queryForList("SELECT lessonId from lesson WHERE chapterId = ? order by lessonNumber", Integer.class, chapterId);
            List<LessonResponse> lessonResponses = new ArrayList<>();
            for (Integer lessonId : lessonIds) {
                try {
                    Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM chapter INNER JOIN lesson on lesson.chapterId = chapter.chapterId AND lessonId = ?", Integer.class, lessonId);

                    if (Objects.equals(lessonId, jdbcTemplate.queryForObject("SELECT min(lessonId) from lesson WHERE chapterId = (SELECT min(chapterId) FROM chapter WHERE courseId = ?)", Integer.class, courseId))) {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration,videoLink,lessonCompletedStatus,lessonStatus FROM lesson INNER JOIN lessonProgress on lesson.lessonId = lessonProgress.lessonId AND lessonProgress.userName = ? AND lesson.lessonId = ? order by lessonNumber", new BeanPropertyRowMapper<>(LessonResponse.class), userName, lessonId);
                        if (lessonResponse != null) {
                            lessonResponse.setLessonStatus(true);
                        }
                        lessonResponses.add(lessonResponse);
                    } else {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration,videoLink,lessonCompletedStatus,lessonStatus FROM lesson INNER JOIN lessonProgress on lesson.lessonId = lessonProgress.lessonId AND lessonProgress.userName = ? AND lesson.lessonId = ? order by lessonNumber", new BeanPropertyRowMapper<>(LessonResponse.class), userName, lessonId);
                        lessonResponses.add(lessonResponse);
                    }
                } catch (Exception e) {
                    Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM chapter INNER JOIN lesson on lesson.chapterId = chapter.chapterId AND lessonId = ?", Integer.class, lessonId);
                    if (Objects.equals(lessonId, jdbcTemplate.queryForObject("SELECT min(lessonId) from lesson WHERE chapterId = (SELECT min(chapterId) FROM chapter WHERE courseId = ?)", Integer.class, courseId))) {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration,videoLink FROM lesson WHERE lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), lessonId);
                        if (lessonResponse != null) {
                            lessonResponse.setLessonStatus(true);
                        }
                        lessonResponses.add(lessonResponse);
                    } else {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration FROM lesson WHERE lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), lessonId);
                        lessonResponses.add(lessonResponse);
                    }
                }
            }
            return lessonResponses;
        } catch (Exception e) {
            return null;
        }
    }

    public Continue getLastPlayed(Integer courseId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? AND userName = ? AND deleteStatus = false", new BeanPropertyRowMapper<>(Enrollment.class), courseId, userName);
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId FROM chapterProgress WHERE courseId = ? AND userName = ? AND chapterCompletedStatus = false ", Integer.class, courseId, userName);
            Integer lessonId = 0;
            for (int chapterId : chapterIds) {
                lessonId = jdbcTemplate.queryForObject("SELECT lesson.lessonId FROM lesson INNER JOIN lessonProgress ON lesson.lessonId = lessonProgress.lessonId WHERE lessonProgress.updatedTime = (SELECT max(updatedTime) FROM lessonProgress where lessonProgress.userName = ? and chapterId = ?) AND lessonProgress.pauseTime < lesson.lessonDuration  AND lessonProgress.pauseTime > '00:00:00'AND lessonProgress.userName = ? AND lesson.chapterId = ?", Integer.class, userName, chapterId, userName, chapterId);
                break;
            }
            return jdbcTemplate.queryForObject("SELECT chapter.chapterId,lessonName,chapterNumber,lessonNumber,lesson.lessonId,pauseTime,videoLink FROM lesson INNER JOIN lessonProgress ON lesson.lessonId = lessonProgress.lessonId INNER JOIN chapter ON lesson.chapterId = chapter.chapterId AND lesson.lessonId = ? AND lessonProgress.userName = ?", new BeanPropertyRowMapper<>(Continue.class), lessonId, userName);
        } catch (Exception e) {
            return null;
        }
    }

    public List<AllCoursesResponse> searchCourses(String search) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE publishStatus = true AND courseName LIKE '%" + search + "%'", Integer.class);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        if (search.length() > 1) {
            for (Integer courseId : courseIds) {
                AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
                Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
                if (chapterCount != null && allCoursesResponse != null) {
                    allCoursesResponse.setChapterCount(chapterCount);
                    allCoursesResponses.add(allCoursesResponse);
                }
            }
        }
        return allCoursesResponses;
    }

    public List<AllCoursesResponse> searchCoursesOfCategory(Integer categoryId, String search) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE categoryId = ? AND publishStatus = true AND courseName LIKE '%" + search + "%'", Integer.class, categoryId);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        if (search.length() > 1) {
            for (Integer courseId : courseIds) {
                AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
                Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
                if (chapterCount != null && allCoursesResponse != null) {
                    allCoursesResponse.setChapterCount(chapterCount);
                    allCoursesResponses.add(allCoursesResponse);
                }
            }
        }
        return allCoursesResponses;
    }

    public List<AllCoursesResponse> searchFilter(FilterRequest filterRequest) {
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        try {
            if (filterRequest.getCategoryId() == null || filterRequest.getCategoryId().isEmpty()) {
                for (int i = 0; i < filterRequest.getChapterStartCount().size(); i++) {
                    List<AllCoursesResponse> allCoursesResponses1 = jdbcTemplate.query("SELECT course.courseId,courseName,coursePhoto,count(chapter.courseId) AS chapterCount,categoryName FROM chapter INNER JOIN course ON course.courseId = chapter.courseId INNER JOIN category ON course.categoryId = category.categoryId AND course.publishStatus = true GROUP BY chapter.courseId HAVING count(chapter.courseId) >= ? AND count(chapter.courseId) <= ? ", new BeanPropertyRowMapper<>(AllCoursesResponse.class), filterRequest.getChapterStartCount().get(i), filterRequest.getChapterEndCount().get(i));
                    allCoursesResponses.addAll(allCoursesResponses1);
                }
            } else if (filterRequest.getChapterStartCount() == null || filterRequest.getChapterStartCount().isEmpty()) {
                List<AllCoursesResponse> allCoursesResponses1 = new ArrayList<>();
                for (int i = 0; i < filterRequest.getCategoryId().size(); i++) {
                    List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE categoryId = ? AND publishStatus = true", Integer.class, filterRequest.getCategoryId().get(i));
                    for (Integer courseId : courseIds) {
                        AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT course.courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON course.categoryId = category.categoryId AND course.courseId = ? group by course.courseId", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
                        Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
                        if (chapterCount != null && allCoursesResponse != null) {
                            allCoursesResponse.setChapterCount(chapterCount);
                            allCoursesResponses1.add(allCoursesResponse);
                        }
                    }
                }
                allCoursesResponses.addAll(allCoursesResponses1);
            } else {
                int categoryListSize = filterRequest.getCategoryId().size() - 1;
                int durationListSize = filterRequest.getChapterStartCount().size() - 1;
                while (categoryListSize >= 0 && durationListSize >= 0) {
                    List<AllCoursesResponse> allCoursesResponses1 = jdbcTemplate.query("SELECT course.courseId,courseName,coursePhoto,count(chapter.courseId) AS chapterCount,categoryName FROM chapter INNER JOIN course ON course.courseId = chapter.courseId INNER JOIN category ON course.categoryId = category.categoryId AND category.categoryId = ? AND course.publishStatus = true GROUP BY chapter.courseId HAVING count(chapter.courseId) > ? AND count(chapter.courseId) < ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), filterRequest.getCategoryId().get(categoryListSize), filterRequest.getChapterStartCount().get(durationListSize), filterRequest.getChapterEndCount().get(durationListSize));
                    allCoursesResponses.addAll(allCoursesResponses1);
                    categoryListSize--;
                    durationListSize--;
                }
            }
            return allCoursesResponses;
        } catch (Exception e) {
            return null;
        }
    }

    public List<AllCoursesResponse> searchByKeyword(String keyword) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId from courseKeywords WHERE keyword = ?", Integer.class, keyword);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        for (Integer courseId : courseIds) {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ? AND publishStatus = true", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
            Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
            if (chapterCount != null && allCoursesResponse != null) {
                allCoursesResponse.setChapterCount(chapterCount);
                allCoursesResponses.add(allCoursesResponse);
            }
        }
        return allCoursesResponses;
    }

    public void topSearches(CourseChapterRequest courseChapterRequest) {
        Integer searchCount = jdbcTemplate.queryForObject("SELECT searchCount FROM courseKeywords WHERE courseId=?", Integer.class, courseChapterRequest.getCourseId());
        if (searchCount != null)
            jdbcTemplate.update("UPDATE courseKeywords set searchCount=? WHERE courseId=?", searchCount + 1, courseChapterRequest.getCourseId());
    }

    public List<KeywordSearchResponse> searchKeywords() {
        List<KeywordSearchResponse> keyWords = new ArrayList<>();
        List<CourseKeywords> courseList = jdbcTemplate.query("select * from courseKeywords order by searchCount", new BeanPropertyRowMapper<>(CourseKeywords.class));
        if (courseList.size() <= 10) {
            for (CourseKeywords c : courseList) {
                if (c.getSearchCount() > 3) {
                    KeywordSearchResponse keywordSearchResponse = new KeywordSearchResponse();
                    System.out.println(c.getKeyword());
                    keywordSearchResponse.setKeyWord(c.getKeyword());
                    keyWords.add(keywordSearchResponse);
                }
            }
        } else {
            int size = courseList.size() / 2;
            for (int i = size; i < courseList.size(); i++) {
                KeywordSearchResponse response = new KeywordSearchResponse();
                response.setKeyWord(courseList.get(i).getKeyword());
                keyWords.add(response);
            }
        }
        return keyWords;

    }

    public List<HomeResponseTopHeader> HomePageTopBar()   // front end should send username when ever they call home api as a response
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<HomeResponseTopHeader> homeTopBar = new ArrayList<>();
        User user = jdbcTemplate.queryForObject("SELECT occupation FROM user WHERE userName = ?", (rs, rowNum) -> new User(rs.getString("occupation")), userName);
        if (user != null) {
            if (user.getOccupation().equals("other")) {
//                List<HomeResponseTopHeader> list = jdbcTemplate.query("SELECT coursePhoto, courseName FROM course", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class));

                List<HomeResponseTopHeader> homeResponseTopHeaders = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course WHERE publishStatus = true", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class));
                System.out.println(homeResponseTopHeaders);
                for(HomeResponseTopHeader homeResponseTopHeader:homeResponseTopHeaders)
                {
                    try
                    {

                        String enrolledUser = jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? and userName = ?", new BeanPropertyRowMapper<>(String.class), homeResponseTopHeader.getCourseId(), userName);

                    }
                    catch (Exception e)
                    {
                        System.out.println("catch");
                        HomeResponseTopHeader homeResponseTopHeader1 = jdbcTemplate.queryForObject("SELECT courseId,coursePhoto, courseName FROM course WHERE courseId = ? and publishStatus = true",new BeanPropertyRowMapper<>(HomeResponseTopHeader.class), homeResponseTopHeader.getCourseId());
                        homeTopBar.add(homeResponseTopHeader1);
                    }
                }

            } else {
                try {
                    Integer subcategoryId = jdbcTemplate.queryForObject("SELECT subCategoryId FROM subCategory WHERE subCategoryName=?", Integer.class, user.getOccupation());
                    List<HomeResponseTopHeader> course = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course WHERE subCategoryId=? and publishStatus = true", (rs, rowNum) -> new HomeResponseTopHeader(rs.getInt("courseId"), rs.getString("courseName"), rs.getString("coursePhoto")), subcategoryId);
                    for(HomeResponseTopHeader homeResponseTopHeader:course)
                    {
                        try
                        {

                            String enrolledUser = jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? and userName = ?", new BeanPropertyRowMapper<>(String.class), homeResponseTopHeader.getCourseId(), userName);

            }
                        catch (Exception e)
                        {
                            HomeResponseTopHeader homeResponseTopHeader1 = jdbcTemplate.queryForObject("SELECT courseId,coursePhoto, courseName FROM course WHERE courseId = ? and publishStatus = true",new BeanPropertyRowMapper<>(HomeResponseTopHeader.class), homeResponseTopHeader.getCourseId());
                            homeTopBar.add(homeResponseTopHeader1);
                        }
                    }
                } catch (NullPointerException e) {
                    Integer categoryId = jdbcTemplate.queryForObject("SELECT categoryId from subCategory WHERE subcategoryName = ?", Integer.class, user.getOccupation());
                    List<HomeResponseTopHeader> course = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course WHERE categoryId=? and publishStatus = true", (rs, rowNum) -> new HomeResponseTopHeader(rs.getInt("courseId"), rs.getString("courseName"), rs.getString("coursePhoto")), categoryId);
                    for(HomeResponseTopHeader homeResponseTopHeader:course)
                    {
                        try
                        {
                            String enrolledUser = jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? and userName = ?", new BeanPropertyRowMapper<>(String.class), homeResponseTopHeader.getCourseId(), userName);
                        }
                        catch (Exception exception)
                        {
                            HomeResponseTopHeader homeResponseTopHeader1 = jdbcTemplate.queryForObject("SELECT courseId,coursePhoto, courseName FROM course WHERE courseId = ? and publishStatus = true",new BeanPropertyRowMapper<>(HomeResponseTopHeader.class), homeResponseTopHeader.getCourseId());
                            homeTopBar.add(homeResponseTopHeader1);
                        }
                    }
                }
            }
        }
        return homeTopBar;
    }


    public List<HomeResponseTopHeader> HomePageTopBarPagination(Integer topHeaderPages)   // front end should send username when ever they call home api as a response
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<HomeResponseTopHeader> homeTopBar = new ArrayList<>();
        User user = jdbcTemplate.queryForObject("SELECT occupation FROM user WHERE userName = ?", (rs, rowNum) -> new User(rs.getString("occupation")), userName);
        if (user != null) {
            if (user.getOccupation().equals("other")) {
//                List<HomeResponseTopHeader> list = jdbcTemplate.query("SELECT coursePhoto, courseName FROM course", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class));
                List<HomeResponseTopHeader> homeResponseTopHeaders = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course WHERE publishStatus = true and userName not in (SELECT * FROM enrollment WHERE courseId = ? and username = ?)", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class));
                System.out.println(homeResponseTopHeaders);
                for(HomeResponseTopHeader homeResponseTopHeader:homeResponseTopHeaders)
                {
                    try
                    {

                        String enrolledUser = jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? and userName = ?", new BeanPropertyRowMapper<>(String.class), homeResponseTopHeader.getCourseId(), userName);

                    }
                    catch (Exception e)
                    {
                        System.out.println("catch");
                        HomeResponseTopHeader homeResponseTopHeader1 = jdbcTemplate.queryForObject("SELECT courseId,coursePhoto, courseName FROM course WHERE courseId = ? and publishStatus = true",new BeanPropertyRowMapper<>(HomeResponseTopHeader.class), homeResponseTopHeader.getCourseId());
                        homeTopBar.add(homeResponseTopHeader1);
                    }
                }
            } else {
                try {
                    Integer subcategoryId = jdbcTemplate.queryForObject("SELECT subCategoryId FROM subCategory WHERE subCategoryName=?", Integer.class, user.getOccupation());
                    List<HomeResponseTopHeader> course = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course WHERE subCategoryId=? and publishStatus = true", (rs, rowNum) -> new HomeResponseTopHeader(rs.getInt("courseId"), rs.getString("courseName"), rs.getString("coursePhoto")), subcategoryId);
                    for(HomeResponseTopHeader homeResponseTopHeader:course)
                    {
                        try
                        {
                            String enrolledUser = jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? and userName = ?", new BeanPropertyRowMapper<>(String.class), homeResponseTopHeader.getCourseId(), userName);

                        }
                        catch (Exception e)
                        {
                            HomeResponseTopHeader homeResponseTopHeader1 = jdbcTemplate.queryForObject("SELECT courseId,coursePhoto, courseName FROM course WHERE courseId = ? and publishStatus = true",new BeanPropertyRowMapper<>(HomeResponseTopHeader.class), homeResponseTopHeader.getCourseId());
                            homeTopBar.add(homeResponseTopHeader1);
                        }
                    }
                } catch (NullPointerException e) {
                    Integer categoryId = jdbcTemplate.queryForObject("SELECT categoryId from subCategory WHERE subcategoryName = ?", Integer.class, user.getOccupation());
                    List<HomeResponseTopHeader> course = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course WHERE categoryId=? and publishStatus = true", (rs, rowNum) -> new HomeResponseTopHeader(rs.getInt("courseId"), rs.getString("courseName"), rs.getString("coursePhoto")), categoryId);
                    for(HomeResponseTopHeader homeResponseTopHeader:course)
                    {
                        try
                        {
                            String enrolledUser = jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? and userName = ?", new BeanPropertyRowMapper<>(String.class), homeResponseTopHeader.getCourseId(), userName);
                        }
                        catch (Exception exception)
                        {
                            HomeResponseTopHeader homeResponseTopHeader1 = jdbcTemplate.queryForObject("SELECT courseId,coursePhoto, courseName FROM course WHERE courseId = ? and publishStatus = true",new BeanPropertyRowMapper<>(HomeResponseTopHeader.class), homeResponseTopHeader.getCourseId());
                            homeTopBar.add(homeResponseTopHeader1);
                        }
                    }
                }
            }
        }
        return homeTopBar;
    }

    public Integer getChapterCount(Integer courseId)
    {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM chapter WHERE courseId = ?",Integer.class, courseId);
    }

    public String getCategoryName(Integer categoryId) {
        return jdbcTemplate.queryForObject("SELECT categoryName FROM category WHERE categoryId = ?", String.class, categoryId);
    }

    public List<HomeAllCourse> getAllCourses() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Boolean publishStatus = false;
        List<HomeAllCourse> homeAllCourseList = new ArrayList<>();
        List<Course> courses = jdbcTemplate.query("SELECT * FROM course", new BeanPropertyRowMapper<>(Course.class));

        for(Course c: courses)
        {
            publishStatus = jdbcTemplate.queryForObject("SELECT publishStatus FROM course WHERE courseId = ?", Boolean.class,c.getCourseId());
            if(publishStatus == true)
            {
                String enrolledUser = jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? and userName = ?", String.class, c.getCourseId(), userName);
                if(enrolledUser == null)
                {
                    HomeAllCourse homeAllCourse = new HomeAllCourse();
                    String categoryName =getCategoryName(c.getCategoryId());
                    Integer chapterCount = getChapterCount(c.getCourseId());
                    homeAllCourse.setCourseId(c.getCourseId());
                    homeAllCourse.setCoursePhoto(c.getCoursePhoto());
                    homeAllCourse.setCourseName(c.getCourseName());
                    homeAllCourse.setCategoryId(c.getCategoryId());
                    homeAllCourse.setCategoryName(categoryName);
                    homeAllCourse.setChapterCount(chapterCount);
                    homeAllCourseList.add(homeAllCourse);
                }
            }
        }
        return homeAllCourseList;
    }

    public List<HomeAllCourse> getAllCoursesPagination(Integer allCoursePageLimit) {
        allCourseUpperLimit = allCoursePageLimit;
        List<HomeAllCourse> homeAllCourses = jdbcTemplate.query("SELECT overView.courseId, coursePhoto, courseName,course.categoryId,categoryName,chapterCount FROM course,overView, category WHERE course.courseId = overView.courseId and categoryName=(SELECT categoryName FROM category ct WHERE ct.categoryId=course.categoryId) limit ?,?", new BeanPropertyRowMapper<>(HomeAllCourse.class), allCourseLowerLimit, allCourseUpperLimit);
        allCourseLowerLimit = allCourseLowerLimit + allCoursePageLimit;
        if (homeAllCourses.size() == 0) {
            allCourseLowerLimit = 0;
            return jdbcTemplate.query("SELECT overView.courseId, coursePhoto, courseName,course.categoryId,categoryName,chapterCount FROM course,overView, category WHERE course.courseId = overView.courseId and categoryName=(SELECT categoryName FROM category ct WHERE ct.categoryId=course.categoryId) limit ?,?", (rs, rowNum) -> new HomeAllCourse(rs.getInt("courseId"), rs.getString("coursePhoto"), rs.getString("courseName"), rs.getInt("categoryId"), rs.getString("categoryName"), rs.getInt("chapterCount")), allCourseLowerLimit, allCourseUpperLimit);
        }
        return homeAllCourses;
    }


    public List<HomeAllCourse> getPopularCourses() {
        List<HomeAllCourse> popularCourseList = new ArrayList<>();
        List<Enrollment> allEnrolledCourses = jdbcTemplate.query("SELECT distinct courseId FROM enrollment", (rs, rowNum) -> new Enrollment(rs.getInt("courseId")));
        for (Enrollment allEnrolledCourse : allEnrolledCourses) {
            Integer enrolmentCount = jdbcTemplate.queryForObject("SELECT count(courseId) FROM enrollment WHERE courseId= ?", Integer.class, allEnrolledCourse.getCourseId());

            if (enrolmentCount > 2) {

                Course course = jdbcTemplate.queryForObject("SELECT * FROM course WHERE courseId = ?", new BeanPropertyRowMapper<>(Course.class), allEnrolledCourse.getCourseId());
                String categoryName = getCategoryName(course.getCategoryId());
                HomeAllCourse homeAllCourse = new HomeAllCourse();
                Integer chapterCount = getChapterCount(course.getCourseId());
                homeAllCourse.setCourseId(course.getCourseId());
                homeAllCourse.setCoursePhoto(course.getCoursePhoto());
                homeAllCourse.setCourseName(course.getCourseName());
                homeAllCourse.setCategoryId(course.getCategoryId());
                homeAllCourse.setCategoryName(categoryName);
                homeAllCourse.setChapterCount(chapterCount);
                popularCourseList.add(homeAllCourse);
            }
        }
        Collections.sort(popularCourseList);
        return popularCourseList;
    }


    public List<HomeAllCourse> getNewCourses() {
        List<HomeAllCourse> newCourseList = new ArrayList<>();
        List<HomeAllCourse> allNewCourses =  getAllCourses();
        Boolean publishStatus = false;
        Collections.sort(allNewCourses);
        int size = allNewCourses.size() - 1;
        int newCourseLimit = size / 2;
        for (int i = size; i >= newCourseLimit; i--) {

            // HomeAllCourse homeAllCourse = jdbcTemplate.queryForObject("SELECT overView.courseId, coursePhoto, courseName,course.categoryId,categoryName,chapterCount FROM course,overView, category WHERE course.courseId=? and course.courseId = overView.courseId and categoryName=(SELECT categoryName FROM category ct WHERE ct.categoryId=course.categoryId)", new BeanPropertyRowMapper<>(HomeAllCourse.class), allNewCourses.get(i).getCourseId());
            publishStatus = jdbcTemplate.queryForObject("SELECT publishStatus FROM course WHERE courseId = ?", Boolean.class, allNewCourses.get(i).getCourseId());
            if (publishStatus == true) {
                newCourseList.add(allNewCourses.get(i));
            }

        }
        return newCourseList;
    }

    public List<TopCourseResponse> popularCoursesInCategory() {
        List<TopCourseResponse> topCoursesList = new ArrayList<>();
        List<Category> categoriesList = jdbcTemplate.query("SELECT * FROM category", (rs, rowNum) -> new Category(rs.getInt("categoryId"), rs.getString("categoryName"), rs.getString("categoryPhoto")));
        if (categoriesList.size() == 0) {
            return null;
        }

        for (Category category : categoriesList) {

            Integer enrollmentCount = jdbcTemplate.queryForObject("SELECT count(c.courseId) FROM enrollment e, course c , category ct WHERE  ct.categoryId = ? and ct.categoryId = c.categoryId and c.courseId = e.courseId", Integer.class, category.getCategoryId());
            if (enrollmentCount != null) {
                if (enrollmentCount >= 0) {
                    try {
                        TopCourseResponse topCourseResponse = new TopCourseResponse();
                        String categoryName = jdbcTemplate.queryForObject("SELECT categoryName FROM category WHERE categoryId=?", String.class, category.getCategoryId());
                        List<Course> courses = jdbcTemplate.query("SELECT * FROM course WHERE categoryId = ?", new BeanPropertyRowMapper<>(Course.class), category.getCategoryId());
                        List<PopularCourseInEachCategory> popularCourseInEachCategoryList = new ArrayList<>();
                        for (Course c : courses) {
                            PopularCourseInEachCategory popularCourseInEachCategory = new PopularCourseInEachCategory();
                            popularCourseInEachCategory.setCourseName(c.getCourseName());
                            popularCourseInEachCategory.setCourseId(c.getCourseId());
                            popularCourseInEachCategory.setCoursePhoto(c.getCoursePhoto());
                            Integer chapterCount = getChapterCount(c.getCourseId());
                            popularCourseInEachCategory.setChapterCount(chapterCount);
                            popularCourseInEachCategory.setCourseDuration(c.getCourseDuration());
                            popularCourseInEachCategory.setPreviewVideo(c.getPreviewVideo());
                            popularCourseInEachCategoryList.add(popularCourseInEachCategory);

                        }
                        System.out.println("before " + popularCourseInEachCategoryList);
                        if (popularCourseInEachCategoryList.size() != 0) {
                            System.out.println("after " + popularCourseInEachCategoryList);
                            topCourseResponse.setCategoryId(category.getCategoryId());
                            topCourseResponse.setCategoryName(categoryName);
                            topCourseResponse.setPopularCourseInEachCategoryList(popularCourseInEachCategoryList);
                        }
                        if (topCourseResponse.getCategoryId() != null && topCourseResponse.getCategoryName() != null && topCourseResponse.getPopularCourseInEachCategoryList() != null && topCourseResponse.getPopularCourseInEachCategoryList().size() != 0) {
                            topCoursesList.add(topCourseResponse);
                        }
                    } catch (EmptyResultDataAccessException exp) {
                        return null;
                    }
                }
            }
        }

        return topCoursesList;
    }

    public String enrollment(EnrollmentRequest enrollmentRequest) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer testIdOfChapter;
        try {
            jdbcTemplate.queryForObject("SELECT * FROM enrollment WHERE userName= ? and courseId = ?", (rs, rowNum) -> new Enrollment(rs.getString("userName"), rs.getInt("courseId"), rs.getDate("joinDate"), rs.getInt("courseScore")), userName, enrollmentRequest.getCourseId());
            return "Already enrolled";
        } catch (EmptyResultDataAccessException e) {
            jdbcTemplate.update("INSERT INTO enrollment(userName,courseId,joinDate) values(?,?,?)", userName, enrollmentRequest.getCourseId(), LocalDate.now());
            jdbcTemplate.update("INSERT INTO courseProgress(userName,courseId) values(?,?)", userName, enrollmentRequest.getCourseId());
            List<Chapter> chaptersOfCourse = jdbcTemplate.query("SELECT * FROM chapter WHERE courseId = ?", (rs, rowNum) -> new Chapter(rs.getInt("chapterId"), rs.getInt("courseId"), rs.getInt("chapterNumber"), rs.getString("chapterName"), rs.getString("chapterDuration")), enrollmentRequest.getCourseId());

            for (Chapter chapter : chaptersOfCourse) {
                List<Lesson> lessonsOfChapter;
                try {
                    testIdOfChapter = jdbcTemplate.queryForObject("SELECT testId FROM test WHERE chapterId = ?", Integer.class, chapter.getChapterId());
                    jdbcTemplate.update("INSERT INTO chapterProgress(userName,courseId,chapterId,testId) values(?,?,?,?)", userName, enrollmentRequest.getCourseId(), chapter.getChapterId(), testIdOfChapter);
                    lessonsOfChapter = jdbcTemplate.query("SELECT * FROM lesson WHERE chapterId = ?", (rs, rowNum) -> new Lesson(rs.getInt("lessonId"), rs.getInt("lessonNumber"), rs.getInt("chapterId"), rs.getString("lessonName"), rs.getString("lessonDuration"), rs.getString("videoLink")), chapter.getChapterId());
                } catch (Exception exception) {
                    jdbcTemplate.update("INSERT INTO chapterProgress(userName,courseId,chapterId) values(?,?,?)", userName, enrollmentRequest.getCourseId(), chapter.getChapterId());
                    lessonsOfChapter = jdbcTemplate.query("SELECT * FROM lesson WHERE chapterId = ?", (rs, rowNum) -> new Lesson(rs.getInt("lessonId"), rs.getInt("lessonNumber"), rs.getInt("chapterId"), rs.getString("lessonName"), rs.getString("lessonDuration"), rs.getString("videoLink")), chapter.getChapterId());
                }
                for (Lesson lesson : lessonsOfChapter) {
                    jdbcTemplate.update("INSERT INTO lessonProgress(userName,chapterId,lessonId) values(?,?,?)", userName, chapter.getChapterId(), lesson.getLessonId());
                }
            }
        }
        String courseName = jdbcTemplate.queryForObject("SELECT courseName FROM course WHERE courseId = ?", String.class, enrollmentRequest.getCourseId());
        String coursePhoto = jdbcTemplate.queryForObject("SELECT coursePhoto FROM course WHERE courseId=?", String.class, enrollmentRequest.getCourseId());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formatDateTime = now.format(format);
        jdbcTemplate.update("INSERT INTO notification(userName,description,notificationUrl,timeStamp) values(?,?,?,?)", userName, "Joined a new course - " + courseName, coursePhoto, formatDateTime);
        String description = "Joined a new course - " + courseName;
        String fcmToken = jdbcTemplate.queryForObject("select fcmToken from user where userName='" + userName + "'", String.class);
        sendPushNotification(fcmToken, description, "Hey " + userName);
        return "Enrolled successfully";
    }

    public void updateVideoPauseTime(VideoPauseRequest videoPauseRequest) throws IOException, ParseException {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean chapterCompletedStatus= jdbcTemplate.queryForObject("SELECT chapterCompletedStatus FROM chapterProgress WHERE chapterId = ? and userName=?",Boolean.class,videoPauseRequest.getChapterId(), userName);
             jdbcTemplate.update("UPDATE chapterProgress SET chapterStatus=? WHERE chapterId=? and userName=? and courseId=?", true, videoPauseRequest.getChapterId(), userName, videoPauseRequest.getCourseId());
             jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus = ? WHERE lessonID=? and username=?", true, videoPauseRequest.getLessonId(), userName);
             java.sql.Time pauseTime = videoPauseRequest.getPauseTime();
             String videoPauseTime = pauseTime.toString();
             LocalDateTime now = LocalDateTime.now();
             DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
             String formatDateTime = now.format(format);
             List<Lesson> lessonList = jdbcTemplate.query("SELECT * FROM lesson WHERE chapterId=?", new BeanPropertyRowMapper<>(Lesson.class), videoPauseRequest.getChapterId());
             // List<Lesson> sortedList = lessonList.stream().sorted().collect(Collectors.toList());
             Collections.sort(lessonList);
             //System.out.println("******"+pauseTime+"llll"+videoPauseRequest.getLessonId());
             System.out.println("lessons list : "+lessonList);
             List<Chapter> chaptersList = jdbcTemplate.query("SELECT * FROM chapter WHERE courseId=?", new BeanPropertyRowMapper<>(Chapter.class), videoPauseRequest.getCourseId());
             //List<Chapter> sortedChapterList = chaptersList.stream().sorted().collect(Collectors.toList());
             Collections.sort(chaptersList);
             //System.out.println("lesson list"+lessonList);
             for (int i = 0; i < lessonList.size(); i++) {
                 if (lessonList.get(i).getLessonId().compareTo(videoPauseRequest.getLessonId()) == 0) {
                     jdbcTemplate.update("UPDATE lessonProgress SET pauseTime=? WHERE lessonId=? and userName=? and chapterId=?", videoPauseTime, videoPauseRequest.getLessonId(), userName, videoPauseRequest.getChapterId());
                     // jdbcTemplate.update("UPDATE lessonProgress SET updatedTime = ? WHERE lessonId=? and userName=? and chapterId=?", formatDateTime,videoPauseRequest.getLessonId(), userName, videoPauseRequest.getChapterId());
                     String lessonDuration = jdbcTemplate.queryForObject("SELECT lessonDuration FROM lesson WHERE lessonId=?", String.class, videoPauseRequest.getLessonId());
                     SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss"); // 12 hour format
                     java.util.Date d1 = (java.util.Date) format1.parse(lessonDuration);
                     java.sql.Time ppstime = new java.sql.Time(d1.getTime());
                     if (lessonDuration != null) {
                         if (pauseTime.compareTo(ppstime) == 0 || pauseTime.after(ppstime)) {
                             System.out.println(ppstime + "  " + pauseTime);
                             jdbcTemplate.update("UPDATE lessonProgress SET lessonCompletedStatus=? WHERE lessonId = ? and userName=? and chapterId=?", true, videoPauseRequest.getLessonId(), userName, videoPauseRequest.getChapterId());

                             try {

                                 jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus = ? WHERE lessonId=?", true, lessonList.get(i + 1).getLessonId());
                             } catch (Exception e) {
                                 try {

                                     Integer testId = jdbcTemplate.queryForObject("SELECT testId FROM test WHERE chapterId=?", Integer.class, videoPauseRequest.getChapterId());
                                 } catch (Exception exception) {

                                     jdbcTemplate.update("UPDATE chapterProgress SET chapterStatus=? WHERE chapterId=?", false, videoPauseRequest.getChapterId());
                                     jdbcTemplate.update("UPDATE chapterProgress SET chapterCompletedStatus=? WHERE chapterId=?", true, videoPauseRequest.getChapterId());

                                 }

                                 boolean chapterCompleted = jdbcTemplate.queryForObject("SELECT chapterCompletedStatus FROM chapterProgress WHERE chapterId=? and userName=?", Boolean.class, videoPauseRequest.getChapterId(), userName);
                                  if (chapterCompleted == true) {

                                     for (int j = 0; j < chaptersList.size(); j++) {
                                          if (chaptersList.get(j).getChapterId().compareTo(videoPauseRequest.getChapterId()) == 0) {
                                             try {

                                                 List<Lesson> lessonsList = jdbcTemplate.query("SELECT *  FROM lesson WHERE chapterId=?", new BeanPropertyRowMapper<>(Lesson.class), chaptersList.get(j + 1).getChapterId());
                                                 Collections.sort(lessonsList);

                                                 //List<Lesson> sortedLessonsList = lessonsList.stream().sorted().collect(Collectors.toList());

                                                 jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus = ? WHERE lessonId=?", true, lessonsList.get(0).getLessonId());
                                                 jdbcTemplate.update("Update chapterProgress set chapterStatus=? where chapterId=?", true, chaptersList.get(j + 1).getChapterId());
                                             } catch (Exception ex) {

                                                 break;
                                             }
                                         }
                                     }
                                     boolean completedStatus = true;
                                     for (Chapter ch : chaptersList) {
                                         completedStatus = jdbcTemplate.queryForObject("SELECT chapterCompletedStatus FROM chapterProgress WHERE chapterId=? and username = ?", Boolean.class, ch.getChapterId(), userName);
                                         if (completedStatus == false) {
                                             completedStatus = false;
                                             break;
                                         } else {
                                             jdbcTemplate.update("UPDATE chapterProgress SET chapterStatus=? WHERE chapterId=? and userName = ?", false, ch.getChapterId(),userName);
                                         }
                                     }
                                     if (completedStatus == true) {
                                         jdbcTemplate.update("UPDATE courseProgress set courseCompletedStatus= ? WHERE courseId=? and userName = ?", true, videoPauseRequest.getCourseId(), userName);
                                         finalTestService.certificateWithoutTest(videoPauseRequest.getCourseId());

                                     }
                                 }
                             }
                         } else {
                             //  jdbcTemplate.update("UPDATE chapterProgress SET chapterStatus=? WHERE chapterId=? and userName=? and courseId=?", true, videoPauseRequest.getChapterId(), userName, videoPauseRequest.getCourseId());
                             // jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus = ? WHERE lessonID=? and username=?", true, videoPauseRequest.getLessonId(), userName);
                         }
                     }
                 }
             }
    }
    }
}