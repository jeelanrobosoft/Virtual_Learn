package com.robosoft.VirtualLearn.AdminPanel.service;

import com.robosoft.VirtualLearn.AdminPanel.entity.*;
import com.robosoft.VirtualLearn.AdminPanel.request.EnrollmentRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.FilterRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.VideoPauseRequest;
import com.robosoft.VirtualLearn.AdminPanel.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    int pages = 2;
    int lowerLimit = 0;
    int upperLimit = pages;
    int topHeaderLowerLimit = 0;

    int topHeaderUpperLimit;

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
        return jdbcTemplate.query("SELECT * FROM category", new BeanPropertyRowMapper<>(Category.class));
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
        return jdbcTemplate.query("SELECT * FROM subCategory WHERE categoryId = ?", new BeanPropertyRowMapper<>(SubCategory.class), categoryId);
    }

    public List<SubCategory> getAllSubCategoriesWithoutPagination() {
        return jdbcTemplate.query("SELECT subCategoryId,subCategoryName FROM subCategory", new BeanPropertyRowMapper<>(SubCategory.class));
    }


    public OverviewResponse getOverviewOfCourse(int courseId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {

            try {
                jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE userName = ? AND courseId = ?", new BeanPropertyRowMapper<>(Enrollment.class), userName, courseId);
                OverviewResponse overviewResponse = jdbcTemplate.queryForObject("SELECT overView.courseId,courseName,coursePhoto,categoryName,chapterCount,lessonCount,courseTagLine,previewVideo,overView.description,testCount,courseMaterialId,courseDuration,instructorName,url,profilePhoto,instructor.description AS instructorDescription FROM overView INNER JOIN instructor ON overView.instructorId = instructor.instructorId  INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(OverviewResponse.class), courseId);
                String learningOutcome = jdbcTemplate.queryForObject("SELECT learningOutCome FROM overView WHERE courseId = ?",String.class,courseId);
                String requirement = jdbcTemplate.queryForObject("SELECT requirements FROM overView WHERE courseId = ?",String.class,courseId);
                if (overviewResponse != null) {
                    if(learningOutcome != null && requirement != null) {
                        overviewResponse.setLearningOutCome(Arrays.asList(learningOutcome.split("\n")));
                        overviewResponse.setRequirements(Arrays.asList(requirement.split("\n")));
                    }
                    overviewResponse.setEnrolled(true);
                }
                return overviewResponse;
            } catch (Exception e) {
                OverviewResponse overviewResponse = jdbcTemplate.queryForObject("SELECT overView.courseId,courseName,coursePhoto,categoryName,chapterCount,lessonCount,courseTagLine,previewVideo,overView.description,testCount,courseMaterialId,courseDuration,learningOutCome,requirements,instructorName,url,profilePhoto,instructor.description AS instructorDescription FROM overView INNER JOIN instructor ON overView.instructorId = instructor.instructorId  INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(OverviewResponse.class), courseId);
                if (overviewResponse != null) {
                    overviewResponse.setEnrolled(false);
                }
                return overviewResponse;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public List<CourseResponse> getBasicCourses(int categoryId) {
        return jdbcTemplate.query("SELECT course.courseId,coursePhoto,courseName,previewVideo,chapterCount,courseDuration FROM overView INNER JOIN course ON course.courseId = overView.courseId WHERE categoryId = " + categoryId + " AND difficultyLevel = 'Beginner'", new BeanPropertyRowMapper<>(CourseResponse.class));
    }

    public List<CourseResponse> getAdvanceCourses(int categoryId) {
        return jdbcTemplate.query("SELECT course.courseId,coursePhoto,courseName,previewVideo,chapterCount,courseDuration FROM overView INNER JOIN course ON course.courseId = overView.courseId WHERE categoryId = " + categoryId + " AND difficultyLevel = 'Advanced'", new BeanPropertyRowMapper<>(CourseResponse.class));
    }

    public List<AllCoursesResponse> getAllCoursesOf(int categoryId) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE category.categoryId = " + categoryId + " ", Integer.class);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        for (Integer courseId : courseIds) {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT course.courseId,coursePhoto,courseName,category.categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
            try {
                Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
                if (allCoursesResponse != null) {
                    allCoursesResponse.setChapterCount(chapterCount);
                }
            } catch (Exception e) {
                if (allCoursesResponse != null) {
                    allCoursesResponse.setChapterCount(0);
                }
            }
            allCoursesResponses.add(allCoursesResponse);
        }
        return allCoursesResponses;
    }
    public List<AllCoursesResponse> getAllCoursesOfSub(int subCategoryId) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE subCategoryId = " + subCategoryId + " ", Integer.class);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        for (Integer courseId : courseIds) {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT course.courseId,coursePhoto,courseName,category.categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
            try {
                Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
                if (allCoursesResponse != null) {
                    allCoursesResponse.setChapterCount(chapterCount);
                }
            } catch (Exception e) {
                if (allCoursesResponse != null) {
                    allCoursesResponse.setChapterCount(0);
                }
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
        List<Integer> courseId = jdbcTemplate.queryForList("SELECT courseId FROM enrollment WHERE  userName = ?", Integer.class, userName);

            for (Integer i : courseId) {
                Integer completedChapter = jdbcTemplate.queryForObject("SELECT count(chapterId) FROM chapterProgress WHERE courseId = ? AND userName = ? AND chapterCompletedStatus = true", Integer.class, i, userName);
                Integer totalChapter = jdbcTemplate.queryForObject("SELECT count(chapterId) FROM chapterProgress WHERE courseId = ? AND userName = ?", Integer.class, i, userName);
                if(completedChapter != null && totalChapter != null) {
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

    public Boolean checkMyCourses()
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE userName = ?",String.class,userName);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }


    public CourseChapterResponse getCourseChapterResponse(Integer courseId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId from chapter WHERE courseId = ?", Integer.class, courseId);
            CourseChapterResponse courseChapterResponse = jdbcTemplate.queryForObject("SELECT course.courseId,courseName,categoryName,chapterCount,lessonCount,testCount,courseDuration,courseCompletedStatus FROM overView INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId INNER JOIN courseProgress on course.courseId = courseProgress.courseId AND userName = ?", new BeanPropertyRowMapper<>(CourseChapterResponse.class), courseId, userName);
            if (courseChapterResponse != null)
            {
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
                courseChapterResponse.setCoursePercentage(jdbcTemplate.queryForObject("SELECT coursePercentage FROM courseProgress WHERE userName = ? AND courseId = ?", Float.class, userName, courseId));
                courseChapterResponse.setJoinedDate(jdbcTemplate.queryForObject("SELECT joinDate FROM enrollment WHERE userName = ? AND courseId = ?", String.class, userName, courseId));
                courseChapterResponse.setCompletedDate(jdbcTemplate.queryForObject("SELECT completedDate FROM enrollment WHERE userName = ? AND courseId = ?", String.class, userName, courseId));
                courseChapterResponse.setCertificateUrl(jdbcTemplate.queryForObject("SELECT certificateUrl FROM certificate WHERE userName = ? AND courseId = ?", String.class, userName, courseId));
            }
        }
            return courseChapterResponse;
        } catch (Exception e) {
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId from chapter WHERE courseId = ? order by chapterNumber", Integer.class, courseId);
            CourseChapterResponse courseChapterResponse = jdbcTemplate.queryForObject("SELECT course.courseId, courseName,categoryName,chapterCount,lessonCount,testCount,courseDuration FROM overView INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(CourseChapterResponse.class), courseId);
            if(courseChapterResponse != null) {
                List<ChapterResponse> chapterResponses = new ArrayList<>();
                courseChapterResponse.setEnrolled(false);
                for (Integer i : chapterIds) {
                    chapterResponses.add(getChapterResponse(userName, i));
                }
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
                if(chapterResponse1 != null && chapterResponse != null) {
                    chapterResponse.setTestId(chapterResponse1.getTestId());
                    chapterResponse.setTestName(chapterResponse1.getTestName());
                    chapterResponse.setTestDuration(chapterResponse1.getTestDuration());
                    chapterResponse.setChapterTestPercentage(chapterResponse1.getChapterTestPercentage());
                    chapterResponse.setQuestionCount(questionCount);
                }
            } catch (Exception e) {
                System.out.println(e);
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
                if(chapterResponse1 != null && chapterResponse != null) {
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
                    Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM chapter INNER JOIN lesson on lesson.chapterId = chapter.chapterId AND lessonId = ?",Integer.class,lessonId);
                    if(Objects.equals(lessonId, jdbcTemplate.queryForObject("SELECT min(lessonId) from lesson WHERE chapterId = (SELECT min(chapterId) FROM chapter WHERE courseId = ?)", Integer.class, courseId))) {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration,videoLink,lessonCompletedStatus,lessonStatus FROM lesson INNER JOIN lessonProgress on lesson.lessonId = lessonProgress.lessonId AND lessonProgress.userName = ? AND lesson.lessonId = ? order by lessonNumber", new BeanPropertyRowMapper<>(LessonResponse.class), userName, lessonId);
                        if (lessonResponse != null) {
                            lessonResponse.setLessonStatus(true);
                        }
                        lessonResponses.add(lessonResponse);
                    }
                    else {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration,videoLink,lessonCompletedStatus,lessonStatus FROM lesson INNER JOIN lessonProgress on lesson.lessonId = lessonProgress.lessonId AND lessonProgress.userName = ? AND lesson.lessonId = ? order by lessonNumber", new BeanPropertyRowMapper<>(LessonResponse.class), userName, lessonId);
                        lessonResponses.add(lessonResponse);
                    }
                } catch (Exception e) {
                    Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM chapter INNER JOIN lesson on lesson.chapterId = chapter.chapterId AND lessonId = ?",Integer.class,lessonId);
                    if(Objects.equals(lessonId, jdbcTemplate.queryForObject("SELECT min(lessonId) from lesson WHERE chapterId = (SELECT min(chapterId) FROM chapter WHERE courseId = ?)", Integer.class, courseId)))
                    {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration,videoLink FROM lesson WHERE lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), lessonId);
                        if (lessonResponse != null) {
                            lessonResponse.setLessonStatus(true);
                        }
                        lessonResponses.add(lessonResponse);
                    }
                    else {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration FROM lesson WHERE lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), lessonId);
                        lessonResponses.add(lessonResponse);
                    }
                }
            }
            return lessonResponses;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Continue getLastPlayed(Integer courseId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? AND userName = ?", new BeanPropertyRowMapper<>(Enrollment.class), courseId, userName);
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId FROM chapter WHERE courseId = ?", Integer.class, courseId);
            Integer lessonId = 0;
            for (int chapterId : chapterIds) {
                lessonId = jdbcTemplate.queryForObject("SELECT lesson.lessonId FROM lesson INNER JOIN lessonProgress ON lesson.lessonId = lessonProgress.lessonId WHERE lessonProgress.updatedTime = (SELECT max(updatedTime) FROM lessonProgress) AND lessonProgress.userName = ? AND lesson.chapterId = ?", Integer.class, userName, chapterId);                break;
            }
            return jdbcTemplate.queryForObject("SELECT chapter.chapterId,lessonName,chapterNumber,lessonNumber,lesson.lessonId,pauseTime,videoLink FROM lesson INNER JOIN lessonProgress ON lesson.lessonId = lessonProgress.lessonId INNER JOIN chapter ON lesson.chapterId = chapter.chapterId AND lesson.lessonId = ? AND lessonProgress.userName = ?", new BeanPropertyRowMapper<>(Continue.class), lessonId, userName);
        } catch (Exception e) {
            return null;
        }
    }

    public List<AllCoursesResponse> searchCourses(String search) {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE courseName LIKE '" + search + "%'", Integer.class);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        for (Integer courseId : courseIds) {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
            Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
            if (chapterCount != null && allCoursesResponse != null) {
                allCoursesResponse.setChapterCount(chapterCount);
                allCoursesResponses.add(allCoursesResponse);
            }
        }
        return allCoursesResponses;
    }

    public List<AllCoursesResponse> searchFilter(FilterRequest filterRequest) {
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        try {
            if (filterRequest.getCategoryId() == null || filterRequest.getCategoryId().isEmpty()) {
                for (int i = 0; i < filterRequest.getChapterStartCount().size(); i++) {
                    List<AllCoursesResponse> allCoursesResponses1 = jdbcTemplate.query("SELECT course.courseId,courseName,coursePhoto,count(chapter.courseId) AS chapterCount,categoryName FROM chapter INNER JOIN course ON course.courseId = chapter.courseId INNER JOIN category ON course.categoryId = category.categoryId GROUP BY chapter.courseId HAVING count(chapter.courseId) >= ? AND count(chapter.courseId) <= ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), filterRequest.getChapterStartCount().get(i), filterRequest.getChapterEndCount().get(i));
                    allCoursesResponses.addAll(allCoursesResponses1);
                }
            } else if (filterRequest.getChapterStartCount() == null || filterRequest.getChapterStartCount().isEmpty()) {
                List<AllCoursesResponse> allCoursesResponses1 = new ArrayList<>();
                for (int i = 0; i < filterRequest.getCategoryId().size(); i++) {
                    List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE categoryId = ?", Integer.class, filterRequest.getCategoryId().get(i));
                    for (Integer courseId : courseIds) {
                        AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT course.courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON course.categoryId = category.categoryId AND course.courseId = ? group by course.courseId", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
                        Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
                        if(chapterCount != null && allCoursesResponse != null) {
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
                    List<AllCoursesResponse> allCoursesResponses1 = jdbcTemplate.query("SELECT course.courseId,courseName,coursePhoto,count(chapter.courseId) AS chapterCount,categoryName FROM chapter INNER JOIN course ON course.courseId = chapter.courseId INNER JOIN category ON course.categoryId = category.categoryId AND category.categoryId = ? GROUP BY chapter.courseId HAVING count(chapter.courseId) > ? AND count(chapter.courseId) < ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), filterRequest.getCategoryId().get(categoryListSize), filterRequest.getChapterStartCount().get(durationListSize), filterRequest.getChapterEndCount().get(durationListSize));
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

    public List<AllCoursesResponse> searchByKeyword(String keyword)
    {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId from courseKeywords WHERE keyword = ?", Integer.class,keyword);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        for (Integer courseId : courseIds) {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
            Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
            if (chapterCount != null && allCoursesResponse != null) {
                allCoursesResponse.setChapterCount(chapterCount);
                allCoursesResponses.add(allCoursesResponse);
            }
        }
        return allCoursesResponses;
    }

    public void topSearches(Integer courseId)
    {
            Integer searchCount = jdbcTemplate.queryForObject("SELECT searchCount FROM courseKeywords WHERE courseId=?", Integer.class, courseId);
            if(searchCount != null)
                jdbcTemplate.update("UPDATE courseKeywords set searchCount=? WHERE courseId=?", searchCount + 1, courseId);
    }

    public List<KeywordSearchResponse> searchKeywords()
    {
        List<KeywordSearchResponse> keyWords = new ArrayList<>();
        List<CourseKeywords> courseList = jdbcTemplate.query("select * from courseKeywords order by searchCount", new BeanPropertyRowMapper<>(CourseKeywords.class));
        if(courseList.size()<=10)
        {
            for(CourseKeywords c:courseList)
            {
                if(c.getSearchCount()>3)
                {
                    KeywordSearchResponse keywordSearchResponse = new KeywordSearchResponse();
                    System.out.println(c.getKeyword());
                    keywordSearchResponse.setKeyWord(c.getKeyword());
                    keyWords.add(keywordSearchResponse);
                }
            }
        }
        else
        {
            int size = courseList.size()/2;
            for(int i=size;i<courseList.size();i++)
            {
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
        User user = jdbcTemplate.queryForObject("SELECT occupation FROM user WHERE userName = ?", (rs, rowNum) -> new User(rs.getInt("occupation")), userName);
        if(user != null) {
            if (user.getOccupation() == 0) {
//                List<HomeResponseTopHeader> list = jdbcTemplate.query("SELECT coursePhoto, courseName FROM course", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class));
                return jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class));
            } else {
                try {
                    List<HomeResponseTopHeader> course = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course WHERE subCategoryId= ?", (rs, rowNum) -> new HomeResponseTopHeader(rs.getInt("courseId"),rs.getString("courseName"), rs.getString("coursePhoto")), user.getOccupation());
                    if (course.size() != 0) {
                        return course;
                    }
                } catch (NullPointerException e) {
                    Integer categoryId = jdbcTemplate.queryForObject("SELECT categoryId from subCategory WHERE subcategoryId = ?", Integer.class, user.getOccupation());
                    return jdbcTemplate.query("SELECT * FROM course WHERE categoryId = ?", (rs, rowNum) -> new HomeResponseTopHeader(rs.getInt("courseId"),rs.getString("courseName"), rs.getString("coursePhoto")), categoryId);

                }
            }
        }
        return null;
    }



    public List<HomeResponseTopHeader> HomePageTopBarPagination(Integer topHeaderPages)   // front end should send username when ever they call home api as a response
    {
        topHeaderUpperLimit=topHeaderPages;
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = jdbcTemplate.queryForObject("SELECT occupation FROM user WHERE userName = ?", (rs, rowNum) -> new User(rs.getInt("occupation")), userName);
        if(user != null) {
            if (user.getOccupation() == 0) {
                List<HomeResponseTopHeader> list = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course limit ?,?", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class),topHeaderLowerLimit,topHeaderUpperLimit);
                topHeaderLowerLimit = topHeaderLowerLimit+topHeaderPages;


                if(list.size() == 0)
                {
                    topHeaderLowerLimit =0;
                    return jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course limit ?,?", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class),topHeaderLowerLimit,topHeaderUpperLimit);

                }
                return list;
                // return jdbcTemplate.query("SELECT coursePhoto, courseName FROM course limit ?,?", new BeanPropertyRowMapper<>(HomeResponseTopHeader.class),,topHeaderLowerLimit,topHeaderUpperLimit);
            } else {
                try {
                    List<HomeResponseTopHeader> course = jdbcTemplate.query("SELECT courseId,coursePhoto, courseName FROM course WHERE subCategoryId= ? limit ?,?", (rs, rowNum) -> new HomeResponseTopHeader(rs.getInt("courseId"),rs.getString("courseName"), rs.getString("coursePhoto")), user.getOccupation(),topHeaderLowerLimit,topHeaderUpperLimit);
                    topHeaderLowerLimit = topHeaderLowerLimit+topHeaderPages;
                    if (course.size() != 0) {
                        return course;
                    }
                } catch (NullPointerException e) {
                    topHeaderLowerLimit=0;
                    Integer categoryId = jdbcTemplate.queryForObject("SELECT categoryId from subCategory WHERE subcategoryId = ?", Integer.class, user.getOccupation());
                    return jdbcTemplate.query("SELECT * FROM course WHERE categoryId = ? limit ?,?", (rs, rowNum) -> new HomeResponseTopHeader(rs.getInt("courseId"),rs.getString("courseName"), rs.getString("coursePhoto")), categoryId,topHeaderLowerLimit, topHeaderUpperLimit);

                }
            }
        }
        return null;
    }


    public List<HomeAllCourse> getAllCourses() {
        return jdbcTemplate.query("SELECT overView.courseId, coursePhoto, courseName,categoryId, chapterCount FROM course,overView WHERE course.courseId = overView.courseId", (rs, rowNum) -> new HomeAllCourse(rs.getInt("courseId"), rs.getString("coursePhoto"),rs.getString("courseName"),  rs.getInt("categoryId"), rs.getInt("chapterCount")));
    }

    public List<HomeAllCourse> getAllCoursesPagination(Integer allCoursePageLimit) {
        topHeaderLowerLimit=0;
        List<HomeAllCourse> homeAllCourses= jdbcTemplate.query("SELECT overView.courseId, coursePhoto, courseName,categoryId, chapterCount FROM course,overView WHERE course.courseId = overView.courseId limit ?,?", (rs, rowNum) -> new HomeAllCourse(rs.getInt("courseId"), rs.getString("coursePhoto"),rs.getString("courseName"),  rs.getInt("categoryId"), rs.getInt("chapterCount")), topHeaderLowerLimit, topHeaderUpperLimit);
        topHeaderLowerLimit = topHeaderLowerLimit+allCoursePageLimit;
        if(homeAllCourses.size() == 0)
        {
            topHeaderLowerLimit =0;
            return  jdbcTemplate.query("SELECT overView.courseId, coursePhoto, courseName,categoryId, chapterCount FROM course,overView WHERE course.courseId = overView.courseId limit ?,?", (rs, rowNum) -> new HomeAllCourse(rs.getInt("courseId"), rs.getString("coursePhoto"),rs.getString("courseName"),  rs.getInt("categoryId"), rs.getInt("chapterCount")), topHeaderLowerLimit, topHeaderUpperLimit);
        }
      return  homeAllCourses;
    }

    public List<HomeAllCourse> getPopularCourses() {
        List<HomeAllCourse> popularCourseList = new ArrayList<>();
        List<Enrollment> allEnrolledCourses = jdbcTemplate.query("SELECT distinct courseId FROM enrollment", (rs, rowNum) -> new Enrollment(rs.getInt("courseId")));
        for (Enrollment allEnrolledCourse : allEnrolledCourses) {
            Integer enrolmentCount = jdbcTemplate.queryForObject("SELECT count(courseId) FROM enrollment WHERE courseId= ?", Integer.class, allEnrolledCourse.getCourseId());
            if (enrolmentCount != null) {
                if (enrolmentCount > 2) {
                    HomeAllCourse homeAllCourse = jdbcTemplate.queryForObject("SELECT c.courseId,c.coursePhoto, c.courseName,c.categoryId, o.chapterCount FROM course c,overView o WHERE c.courseId=? and c.courseId = o.courseId", (rs, rowNum) -> new HomeAllCourse(rs.getInt("courseId"),rs.getString("coursePhoto"), rs.getString("courseName"), rs.getInt("categoryId"), rs.getInt("chapterCount")), allEnrolledCourse.getCourseId());
                    popularCourseList.add(homeAllCourse);
                }
            }
        }
        return popularCourseList;
    }

    public List<HomeAllCourse> getNewCourses() {
        List<HomeAllCourse> newCourseList = new ArrayList<>();
        List<HomeAllCourse> allNewCourses = jdbcTemplate.query("SELECT course.courseId, coursePhoto, courseName,categoryId, chapterCount FROM course,overView WHERE course.courseId = overView.courseId", (rs, rowNum) -> new HomeAllCourse(rs.getInt("courseId"), rs.getString("coursePhoto"), rs.getString("courseName"), rs.getInt("categoryId"), rs.getInt("chapterCount")));
        int size = allNewCourses.size() - 1;
        int newCourseLimit = size / 2;
        for (int i = size; i >= newCourseLimit; i--) {
            HomeAllCourse homeAllCourse = jdbcTemplate.queryForObject("SELECT c.courseId,c.coursePhoto, c.courseName,c.categoryId, o.chapterCount FROM course c,overView o WHERE c.courseId=? and c.courseId = o.courseId", new BeanPropertyRowMapper<>(HomeAllCourse.class), allNewCourses.get(i).getCourseId());
            newCourseList.add(homeAllCourse);
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
            TopCourseResponse topCourseResponse = new TopCourseResponse();
            Integer enrollmentCount = jdbcTemplate.queryForObject("SELECT count(c.courseId) FROM enrollment e, course c , category ct WHERE  ct.categoryId = ? and ct.categoryId = c.categoryId and c.courseId = e.courseId", Integer.class, category.getCategoryId());
            if(enrollmentCount != null) {
                if (enrollmentCount > 2) {
                    try {
                        List<PopularCourseInEachCategory> popularCourseInEachCategory = jdbcTemplate.query("SELECT c.courseName,c.coursePhoto,o.chapterCount, c.courseDuration,c.previewVideo from course c, overView o , category ct WHERE ct.categoryId = ? and  ct.categoryId = c.categoryId and c.courseId = o.courseId", (rs, rowNum) -> new PopularCourseInEachCategory(rs.getString("courseName"), rs.getString("coursePhoto"), rs.getInt("chapterCount"), rs.getString("courseDuration"), rs.getString("previewVideo")), category.getCategoryId());
                        String categoryName = jdbcTemplate.queryForObject("SELECT categoryName FROM category WHERE categoryId=?", String.class, category.getCategoryId());
                        if(popularCourseInEachCategory.size() !=0)
                        {
                            topCourseResponse.setCategoryId(category.getCategoryId());
                            topCourseResponse.setPopularCourseInEachCategoryList(popularCourseInEachCategory);
                            topCourseResponse.setCategoryName(categoryName);
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
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formatDateTime = now.format(format);
        jdbcTemplate.update("INSERT INTO notification(userName,description,notificationUrl,timeStamp) values(?,?,?,?)", userName, "Joined a new course - " + courseName, coursePhoto, formatDateTime);
        return "Enrolled successfully";
    }

    public void updateVideoPauseTime(VideoPauseRequest videoPauseRequest) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Time pauseTime = videoPauseRequest.getPauseTime();
        String videoPauseTime = pauseTime.toString();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = now.format(format);
        List<Lesson> lessonList = jdbcTemplate.query("SELECT * FROM lesson WHERE chapterId=?",new BeanPropertyRowMapper<>(Lesson.class),videoPauseRequest.getChapterId());
       // List<Lesson> sortedList = lessonList.stream().sorted().collect(Collectors.toList());
        Collections.sort(lessonList);
        for(int i=0;i<lessonList.size();i++) {
            if(lessonList.get(i).getLessonId() == videoPauseRequest.getLessonId()) {
                jdbcTemplate.update("UPDATE lessonProgress SET pauseTime=? WHERE lessonId=? and userName=? and chapterId=?", videoPauseTime,videoPauseRequest.getLessonId(), userName, videoPauseRequest.getChapterId());
                jdbcTemplate.update("UPDATE lessonProgress SET updatedTime = ? WHERE lessonId=? and userName=? and chapterId=?", formatDateTime,videoPauseRequest.getLessonId(), userName, videoPauseRequest.getChapterId());
                String lessonDuration = jdbcTemplate.queryForObject("SELECT lessonDuration FROM lesson WHERE lessonId=?", String.class, videoPauseRequest.getLessonId());
                if(lessonDuration != null) {
                    if (lessonDuration.equals(videoPauseTime)) {
                        jdbcTemplate.update("UPDATE lessonProgress SET lessonCompletedStatus=? WHERE lessonId = ? and userName=? and chapterId=?", true, videoPauseRequest.getLessonId(), userName, videoPauseRequest.getChapterId());

                        try
                        {
                            jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus = ? WHERE lessonId=?", true, lessonList.get(i+1).getLessonId());
                        }
                        catch(Exception e)
                        {
                            boolean chapterCompleted = jdbcTemplate.queryForObject("SELECT chapterCompletedStatus FROM chapterProgress WHERE chapterId=? and userName=?", Boolean.class,videoPauseRequest.getChapterId(),userName);
                            if(chapterCompleted == true)
                            {
                                List<Chapter> chaptersList = jdbcTemplate.query("SELECT * FROM chapter WHERE courseId=?",new BeanPropertyRowMapper<>(Chapter.class), videoPauseRequest.getCourseId());
                                //List<Chapter> sortedChapterList = chaptersList.stream().sorted().collect(Collectors.toList());
                                Collections.sort(chaptersList);
                                System.out.println(chaptersList+"++++++++++++++++++++");
                                for(int j=0;j<chaptersList.size();j++)
                                {
                                    if(chaptersList.get(j).getChapterId() == videoPauseRequest.getChapterId())
                                    {
                                           try
                                           {
                                               List<Lesson> lessonsList = jdbcTemplate.query("SELECT *  FROM lesson WHERE chapterId=?", new BeanPropertyRowMapper<>(Lesson.class), chaptersList.get(j+1).getChapterId());
                                               List<Lesson> sortedLessonsList = lessonsList.stream().sorted().collect(Collectors.toList());
                                               jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus = ? WHERE lessonId=?", true,sortedLessonsList.get(0).getLessonId());
                                           }
                                           catch(Exception ex)
                                           {
                                               break;
                                           }
                                    }
                                }
                            }
                        }
                    } else if (!(lessonDuration.equals("00:00:00"))) {
                        jdbcTemplate.update("UPDATE chapterProgress SET chapterStatus=? WHERE chapterId=? and userName=? and courseId=?", true, videoPauseRequest.getChapterId(), userName, videoPauseRequest.getCourseId());
                        jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus = ? WHERE lessonID=? and username=?", true, videoPauseRequest.getLessonId(), userName);
                    }
                }

            }

        }
    }
}

