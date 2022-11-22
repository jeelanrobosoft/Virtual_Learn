package com.robosoft.VirtualLearn.AdminPanel.service;

import com.robosoft.VirtualLearn.AdminPanel.entity.*;
import com.robosoft.VirtualLearn.AdminPanel.request.EnrollmentRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.FilterRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.VideoPauseRequest;
import com.robosoft.VirtualLearn.AdminPanel.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class UserService
{
    @Autowired
    private JdbcTemplate jdbcTemplate;


    int pages = 2;
    int lowerLimit = 0;
    int upperLimit = pages;

    public List<Category> getCategories()
    {
        List<Category> categories=  jdbcTemplate.query("SELECT * FROM category limit ?,?",(rs, rowNum) ->
                new Category(rs.getInt("categoryId"),rs.getString("categoryName"),rs.getString("categoryPhoto")),lowerLimit,upperLimit);
        lowerLimit = lowerLimit+pages;

        if(categories.size()==0)
        {
            lowerLimit=0;
            List <Category> categories1= jdbcTemplate.query("SELECT * FROM category limit ?,?", (rs, rowNum) ->
                    new Category(rs.getInt("categoryId"),rs.getString("categoryName"),rs.getString("categoryPhoto")),lowerLimit,upperLimit);
            return categories1;
        }
        return categories;
    }


    public List<SubCategory> getSubCategories(Category category)
    {
        List<SubCategory> subCategories=  jdbcTemplate.query("SELECT * FROM subCategory WHERE categoryId = ? limit ?,?",(rs, rowNum) ->
                new SubCategory(rs.getInt("categoryId"),rs.getInt("subCategoryId"),rs.getString("subCategoryName")),category.getCategoryId(),lowerLimit,upperLimit);
        lowerLimit = lowerLimit+pages;
        if(subCategories.size()==0)
        {
            lowerLimit=0;
            List<SubCategory> subCategories1=  jdbcTemplate.query("SELECT * FROM subCategory WHERE categoryId = ? limit ?,?",(rs, rowNum) ->
                    new SubCategory(rs.getInt("categoryId"),rs.getInt("subCategoryId"),rs.getString("subCategoryName")),category.getCategoryId(),lowerLimit,upperLimit);
            return subCategories1;
        }
        return subCategories;
    }


    public OverviewResponse getOverviewOfCourse(int courseId) {
        try {
            return jdbcTemplate.queryForObject("SELECT courseName,coursePhoto,categoryName,chapterCount,lessonCount,courseTagLine,previewVideo,overView.description,testCount,courseMaterialId,courseDuration,learningOutCome,requirements,instructorName,url,profilePhoto,instructor.description AS instructorDescription FROM overView INNER JOIN instructor ON overView.instructorId = instructor.instructorId  INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(OverviewResponse.class), courseId);
        } catch (Exception e) {
            System.out.println(e);
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
        for(Integer courseId:courseIds)
        {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT course.courseId,coursePhoto,courseName,category.categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class),courseId);
            try {
                Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
                allCoursesResponse.setChapterCount(chapterCount);
            }
            catch (Exception e)
            {
                allCoursesResponse.setChapterCount(0);
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
            if (completedChapter < totalChapter) {
                OngoingResponse ongoingResponse = jdbcTemplate.queryForObject("SELECT courseId,courseName,coursePhoto FROM course WHERE courseId = ?", new BeanPropertyRowMapper<>(OngoingResponse.class), i);
                ongoingResponse.setCompletedChapter(completedChapter);
                ongoingResponse.setTotalChapter(totalChapter);
                ongoingResponses.add(ongoingResponse);
            }
        }
        return ongoingResponses;
    }


    public CourseChapterResponse getCourseChapterResponse(Integer courseId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId from chapter WHERE courseId = ?", Integer.class, courseId);
            CourseChapterResponse courseChapterResponse = jdbcTemplate.queryForObject("SELECT courseName,categoryName,chapterCount,lessonCount,testCount,courseDuration,courseCompletedStatus FROM overView INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId INNER JOIN courseProgress on course.courseId = courseProgress.courseId AND userName = ?", new BeanPropertyRowMapper<>(CourseChapterResponse.class), courseId,userName);
            courseChapterResponse.setEnrolled(true);
            String courseDuration = courseChapterResponse.getCourseDuration();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date1 = timeFormat.parse(courseDuration);
            long sum = date1.getTime();
            List<ChapterResponse> chapterResponses = new ArrayList<>();
            for (Integer i : chapterIds) {
                chapterResponses.add(getChapterResponse(userName, i));
                String testDuration = jdbcTemplate.queryForObject("SELECT testDuration FROM test WHERE chapterId = ?", String.class,i);
                timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date2 = timeFormat.parse(testDuration);
                sum += date2.getTime();
            }
            String totalDuration = timeFormat.format(new Date(sum));
            courseChapterResponse.setTotalDuration(totalDuration);
            courseChapterResponse.setChapterResponses(chapterResponses);
            if(courseChapterResponse.getCourseCompletedStatus())
            {
                courseChapterResponse.setCoursePercentage(jdbcTemplate.queryForObject("SELECT coursePercentage FROM courseProgress WHERE userName = ? AND courseId = ?", Float.class,userName,courseId));
                courseChapterResponse.setJoinedDate(jdbcTemplate.queryForObject("SELECT joinDate FROM enrollment WHERE userName = ? AND courseId = ?",String.class,userName,courseId));
                courseChapterResponse.setCompletedDate(jdbcTemplate.queryForObject("SELECT completedDate FROM enrollment WHERE userName = ? AND courseId = ?",String.class,userName,courseId));
                courseChapterResponse.setCertificateUrl(jdbcTemplate.queryForObject("SELECT certificateUrl FROM certificate WHERE userName = ? AND courseId = ?",String.class,userName,courseId));
            }
            return courseChapterResponse;
        }catch (Exception e)
        {
            System.out.println(e);
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId from chapter WHERE courseId = ? order by chapterNumber", Integer.class, courseId);
            CourseChapterResponse courseChapterResponse = jdbcTemplate.queryForObject("SELECT courseName,categoryName,chapterCount,lessonCount,testCount,courseDuration FROM overView INNER JOIN course ON overView.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(CourseChapterResponse.class), courseId);
            List<ChapterResponse> chapterResponses = new ArrayList<>();
            courseChapterResponse.setEnrolled(false);
            for (Integer i : chapterIds) {
                chapterResponses.add(getChapterResponse(userName, i));
            }
            courseChapterResponse.setChapterResponses(chapterResponses);
            return courseChapterResponse;
        }
    }

    public ChapterResponse getChapterResponse(String userName, Integer chapterId) {
        try {
            ChapterResponse chapterResponse = jdbcTemplate.queryForObject("SELECT chapterNumber,chapterName,chapterCompletedStatus,chapterStatus FROM chapter INNER JOIN chapterProgress ON chapter.chapterId = chapterProgress.chapterId WHERE chapter.chapterId = ? AND chapterProgress.userName = ?", new BeanPropertyRowMapper<>(ChapterResponse.class), chapterId, userName);
            List<LessonResponse> lessonResponses = getLessonResponses(chapterId);
            try
            {
                Integer testId = jdbcTemplate.queryForObject("SELECT testId FROM test WHERE chapterId = ?", Integer.class,chapterId);
                Integer questionCount = jdbcTemplate.queryForObject("SELECT count(questionId) FROM question WHERE testId = ?", Integer.class,testId);
                ChapterResponse chapterResponse1 = jdbcTemplate.queryForObject("SELECT test.testId,testName,testDuration,chapterTestPercentage FROM test INNER JOIN chapterProgress ON test.testId = chapterProgress.testId AND chapterProgress.userName = ? AND chapterProgress.chapterId = ?",new BeanPropertyRowMapper<>(ChapterResponse.class),userName,chapterId);
                chapterResponse.setTestId(chapterResponse1.getTestId());
                chapterResponse.setTestName(chapterResponse1.getTestName());
                chapterResponse.setTestDuration(chapterResponse1.getTestDuration());
                chapterResponse.setChapterTestPercentage(chapterResponse1.getChapterTestPercentage());
                chapterResponse.setQuestionCount(questionCount);
            }
            catch (Exception e)
            {
            }
            chapterResponse.setLessonResponses(lessonResponses);
            return chapterResponse;

        } catch (Exception e) {
            ChapterResponse chapterResponse=  jdbcTemplate.queryForObject("SELECT chapter.chapterId,chapterNumber,chapterName FROM chapter WHERE chapterId = ?", new BeanPropertyRowMapper<>(ChapterResponse.class), chapterId);
            List<LessonResponse> lessonResponses = getLessonResponses(chapterId);
            try
            {
                Integer testId = jdbcTemplate.queryForObject("SELECT testId FROM test WHERE chapterId = ?", Integer.class,chapterId);
                Integer questionCount = jdbcTemplate.queryForObject("SELECT count(questionId) FROM question WHERE testId = ?", Integer.class,testId);
                ChapterResponse chapterResponse1 = jdbcTemplate.queryForObject("SELECT test.testId,testName,testDurationFROM test test.chapterId = ?",new BeanPropertyRowMapper<>(ChapterResponse.class),chapterId);
                chapterResponse.setTestId(chapterResponse1.getTestId());
                chapterResponse.setTestName(chapterResponse1.getTestName());
                chapterResponse.setTestDuration(chapterResponse1.getTestDuration());
                chapterResponse.setQuestionCount(questionCount);
            }
            catch (Exception exception)
            {
            }
            chapterResponse.setLessonResponses(lessonResponses);
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
                    LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration,videoLink,lessonCompletedStatus,lessonStatus FROM lesson INNER JOIN lessonProgress on lesson.lessonId = lessonProgress.lessonId AND lessonProgress.userName = ? AND lesson.lessonId = ? order by lessonNumber", new BeanPropertyRowMapper<>(LessonResponse.class), userName, lessonId);
                    lessonResponses.add(lessonResponse);
                } catch (Exception e) {
                    System.out.println(e);
                    String lessonName = jdbcTemplate.queryForObject("SELECT lessonName FROM lesson WHERE lessonId = ?", String.class, lessonId);
                    if (lessonName.equalsIgnoreCase("Introduction")) {
                        LessonResponse lessonResponse = jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration,videoLink FROM lesson WHERE lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), lessonId);
                        lessonResponses.add(lessonResponse);                    }
                    LessonResponse lessonResponse =jdbcTemplate.queryForObject("SELECT lesson.lessonId,lessonNumber,lessonName,lessonDuration FROM lesson WHERE lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), lessonId);
                    lessonResponses.add(lessonResponse);                }
            }return lessonResponses;
        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }

    public List<CompletedResponse> getCourseCompletedResponse()
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return jdbcTemplate.query("SELECT course.courseId,courseName,coursePercentage,coursePhoto FROM course INNER JOIN courseProgress ON course.courseId = courseProgress.courseId AND courseProgress.userName = ? AND courseProgress.courseCompletedStatus = true",new BeanPropertyRowMapper<>(CompletedResponse.class),userName);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public Continue getLastPlayed(Integer courseId )
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            jdbcTemplate.queryForObject("SELECT userName FROM enrollment WHERE courseId = ? AND userName = ?",new BeanPropertyRowMapper<>(Enrollment.class),courseId,userName);
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId FROM chapter WHERE courseId = ?", Integer.class,courseId);
            int lessonId = 0;
            for(int chapterId :chapterIds)
            {
                System.out.println(chapterId);
                lessonId = jdbcTemplate.queryForObject("SELECT lesson.lessonId FROM lesson INNER JOIN lessonProgress ON lesson.lessonId = lessonProgress.lessonId WHERE lessonProgress.pauseTime < lesson.lessonDuration  AND lessonProgress.pauseTime > '00.00.00' AND lessonProgress.userName = ? AND lesson.chapterId = ?", Integer.class,userName,chapterId);
                break;
            }
            return jdbcTemplate.queryForObject("SELECT chapterNumber,lessonNumber,lesson.lessonId,pauseTime,videoLink FROM lesson INNER JOIN lessonProgress ON lesson.lessonId = lessonProgress.lessonId INNER JOIN chapter ON lesson.chapterId = chapter.chapterId AND lesson.lessonId = ? AND lessonProgress.userName = ?", new BeanPropertyRowMapper<>(Continue.class),lessonId,userName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public List<AllCoursesResponse> searchCourses(String search)
    {
        List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE courseName LIKE '"+search+"%'", Integer.class);
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        for (Integer courseId:courseIds)
        {
            AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON category.categoryId = course.categoryId WHERE courseId = ?",new BeanPropertyRowMapper<>(AllCoursesResponse.class),courseId);
            Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
            allCoursesResponse.setChapterCount(chapterCount);
            allCoursesResponses.add(allCoursesResponse);
        }
        return allCoursesResponses;
    }

    public List<AllCoursesResponse> searchFilter(FilterRequest filterRequest)
    {
        List<AllCoursesResponse> allCoursesResponses = new ArrayList<>();
        try {
            if(filterRequest.getCategoryId() == null || filterRequest.getCategoryId().isEmpty())
            {
                for(int i = 0; i<filterRequest.getChapterStartCount().size();i++)
                {
                    List<AllCoursesResponse> allCoursesResponses1 = jdbcTemplate.query("SELECT course.courseId,courseName,coursePhoto,count(chapter.courseId) AS chapterCount,categoryName FROM chapter INNER JOIN course ON course.courseId = chapter.courseId INNER JOIN category ON course.categoryId = category.categoryId GROUP BY chapter.courseId HAVING count(chapter.courseId) >= ? AND count(chapter.courseId) <= ?",new BeanPropertyRowMapper<>(AllCoursesResponse.class),filterRequest.getChapterStartCount().get(i),filterRequest.getChapterEndCount().get(i));
                    allCoursesResponses.addAll(allCoursesResponses1);
                }
            } else if (filterRequest.getChapterStartCount() == null || filterRequest.getChapterStartCount().isEmpty()) {
                List<AllCoursesResponse> allCoursesResponses1 = new ArrayList<>();
                for(int i =0; i<filterRequest.getCategoryId().size();i++) {
                    List<Integer> courseIds = jdbcTemplate.queryForList("SELECT courseId FROM course WHERE categoryId = ?", Integer.class, filterRequest.getCategoryId().get(i));
                    for (Integer courseId : courseIds) {
                        AllCoursesResponse allCoursesResponse = jdbcTemplate.queryForObject("SELECT course.courseId,courseName,coursePhoto,categoryName FROM course INNER JOIN category ON course.categoryId = category.categoryId AND course.courseId = ? group by course.courseId", new BeanPropertyRowMapper<>(AllCoursesResponse.class), courseId);
                        Integer chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, courseId);
                        allCoursesResponse.setChapterCount(chapterCount);
                        allCoursesResponses1.add(allCoursesResponse);
                    }
                }
                allCoursesResponses.addAll(allCoursesResponses1);
            }
            else {
                int categoryListSize = filterRequest.getCategoryId().size()-1;
                int durationListSize = filterRequest.getChapterStartCount().size()-1;
                while(categoryListSize >= 0 && durationListSize >= 0) {
                    List<AllCoursesResponse> allCoursesResponses1 = jdbcTemplate.query("SELECT course.courseId,courseName,coursePhoto,count(chapter.courseId) AS chapterCount,categoryName FROM chapter INNER JOIN course ON course.courseId = chapter.courseId INNER JOIN category ON course.categoryId = category.categoryId AND category.categoryId = ? GROUP BY chapter.courseId HAVING count(chapter.courseId) > ? AND count(chapter.courseId) < ?", new BeanPropertyRowMapper<>(AllCoursesResponse.class), filterRequest.getCategoryId().get(categoryListSize), filterRequest.getChapterStartCount().get(durationListSize), filterRequest.getChapterEndCount().get(durationListSize));
                    allCoursesResponses.addAll(allCoursesResponses1);
                    categoryListSize--;
                    durationListSize--;
                }
            }
            return allCoursesResponses;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    //chk

    public List<HomeResponseTopHeader> HomePageTopBar()   // front end should send username when ever they call home api as a response
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user= jdbcTemplate.queryForObject("SELECT occupation FROM user WHERE userName = ?",(rs, rowNum) -> {
            return new User(rs.getInt("occupation"));
        }, userName);
        if(user.getOccupation() == 0)
        {
            List<HomeResponseTopHeader> courseListForStudent = jdbcTemplate.query("SELECT coursePhoto, courseName FROM course",new BeanPropertyRowMapper<>(HomeResponseTopHeader.class));
            return courseListForStudent;
        }
        else
        {
            try
            {
                List<HomeResponseTopHeader> course = jdbcTemplate.query("SELECT coursePhoto, courseName FROM course WHERE subCategoryId= ?",(rs, rowNum) -> {
                    return new HomeResponseTopHeader(rs.getString("coursePhoto"), rs.getString("courseName"));
                }, user.getOccupation());
                if(course.size() !=0)
                {
                    return course;
                }
            }
            catch(NullPointerException e)
            {
                int categoryId = jdbcTemplate.queryForObject("SELECT categoryId from subCategory WHERE subcategoryId = ?", new Object[] {user.getOccupation()}, Integer.class);
                List<HomeResponseTopHeader> courses = jdbcTemplate.query("SELECT * FROM course WHERE categoryId = ?",(rs, rowNum) -> {
                    return new HomeResponseTopHeader(rs.getString("coursePhoto"), rs.getString("courseName"));
                }, categoryId);
                return courses;
            }
        }
        return null;
    }
    //"All" in home page display all the courses
    public List<HomeAllCourse> getAllCourses()
    {
        List<HomeAllCourse> allCourses = jdbcTemplate.query("SELECT overView.courseId, coursePhoto, courseName,categoryId, chapterCount FROM course,overView WHERE course.courseId = overView.courseId",(rs, rowNum) -> {
            return new HomeAllCourse(rs.getInt("courseId"),rs.getString("coursePhoto"), rs.getString("courseName"), rs.getInt("categoryId"),rs.getInt("chapterCount"));
        });
        System.out.println(allCourses);
        return allCourses;
    }
    //"popular" in home page , filter all the courses with maximum enrollments more than 5
    public List<HomeAllCourse> getPopularCourses()
    {
        List<HomeAllCourse> popularCourseList = new ArrayList<>();
        List<Enrollment> allEnrolledCourses = jdbcTemplate.query("SELECT distinct courseId FROM enrollment",(rs, rowNum) -> {
            return new Enrollment(rs.getInt("courseId"));
        });
        for(int i=0;i<allEnrolledCourses.size();i++)
        {
            int enrolmentCount = jdbcTemplate.queryForObject("SELECT count(courseId) FROM enrollment WHERE courseId= ?", new Object[] {allEnrolledCourses.get(i).getCourseId()}, Integer.class);
            if(enrolmentCount >1)
            {
                HomeAllCourse homeAllCourse= jdbcTemplate.queryForObject("SELECT c.coursePhoto, c.courseName,c.categoryId, o.chapterCount FROM course c,overView o WHERE c.courseId=? and c.courseId = o.courseId",(rs, rowNum) -> {
                    return new HomeAllCourse(rs.getString("coursePhoto"), rs.getString("courseName"), rs.getInt("categoryId"),rs.getInt("chapterCount"));
                },allEnrolledCourses.get(i).getCourseId());
                popularCourseList.add(homeAllCourse);
            }
        }
        System.out.println(popularCourseList);
        return popularCourseList;
    }
    public List<HomeAllCourse> getNewCourses()
    {
        List<HomeAllCourse> newCourseList = new ArrayList<>();
        List<HomeAllCourse> allNewCourses = jdbcTemplate.query("SELECT course.courseId, coursePhoto, courseName,categoryId, chapterCount FROM course,overView WHERE course.courseId = overView.courseId",(rs, rowNum) -> {
            return new HomeAllCourse(rs.getInt("courseId"),rs.getString("coursePhoto"), rs.getString("courseName"), rs.getInt("categoryId"),rs.getInt("chapterCount"));
        });
        //System.out.println(allNewCourses.size());
        int size = allNewCourses.size()-1;
        int newCourseLimit = size/2;
        for(int i=size;i>=newCourseLimit;i--)
        {
            //System.out.println(allNewCourses.get(i).getCourseId());
            HomeAllCourse homeAllCourse= jdbcTemplate.queryForObject("SELECT c.coursePhoto, c.courseName,c.categoryId, o.chapterCount FROM course c,overView o WHERE c.courseId=? and c.courseId = o.courseId",(rs, rowNum) -> {
                return new HomeAllCourse(rs.getString("coursePhoto"), rs.getString("courseName"), rs.getInt("categoryId"),rs.getInt("chapterCount"));
            },allNewCourses.get(i).getCourseId());
            newCourseList.add(homeAllCourse);
        }
        return newCourseList;
    }
    public Map<String,List<PopularCourseInEachCategory>> popularCoursesInCategory()
    {
        Map<String,List<PopularCourseInEachCategory>> topCoursesList  = new HashMap<>();
        List<Category> categoriesList = jdbcTemplate.query("SELECT * FROM category",(rs, rowNum) -> {
            return new Category(rs.getInt("categoryId"), rs.getString("categoryName"), rs.getString("categoryPhoto"));
        });
        if(categoriesList.size() == 0)
        {
            return null;
        }
        for(int i=0;i<categoriesList.size();i++)
        {
            int enrollmentCount = jdbcTemplate.queryForObject("SELECT count(c.courseId) FROM enrollment e, course c , category ct WHERE  ct.categoryId = ? and ct.categoryId = c.categoryId and c.courseId = e.courseId", new Object[] {categoriesList.get(i).getCategoryId()}, Integer.class);
            if(enrollmentCount >2)
            {
                try
                {
                    List<PopularCourseInEachCategory> popularCourseInEachCategory = jdbcTemplate.query("SELECT c.courseName,c.coursePhoto,o.chapterCount, c.courseDuration,c.previewVideo from course c, overView o , category ct WHERE ct.categoryId = ? and  ct.categoryId = c.categoryId and c.courseId = o.courseId",(rs, rowNum) -> {
                        return new PopularCourseInEachCategory(rs.getString("courseName"),rs.getString("coursePhoto"),rs.getInt("chapterCount"), rs.getString("courseDuration"),rs.getString("previewVideo"));
                    },categoriesList.get(i).getCategoryId());
                    System.out.println(popularCourseInEachCategory);
                    String categoryName = jdbcTemplate.queryForObject("SELECT categoryName FROM category WHERE categoryId=?", new Object[] {categoriesList.get(i).getCategoryId()}, String.class);
                    topCoursesList.put(categoryName,popularCourseInEachCategory);

                }
                catch(EmptyResultDataAccessException expn)
                {
                    return null;
                }
            }
        }
        return topCoursesList;
    }
    public String enrollment(EnrollmentRequest enrollmentRequest)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try
        {
            Enrollment enrollmentRequest1 = jdbcTemplate.queryForObject("SELECT * FROM enrollment WHERE userName= ? and courseId = ?",(rs, rowNum) -> {
                return new Enrollment(rs.getString("userName"), rs.getInt("courseId"), rs.getDate("joinDate"),rs.getInt("courseScore"));
            },userName,enrollmentRequest.getCourseId());
            return "Already enrolled";
        }
        catch(EmptyResultDataAccessException e)
        {
            jdbcTemplate.update("INSERT INTO enrollment(userName,courseId,joinDate) values(?,?,?)",userName, enrollmentRequest.getCourseId(), enrollmentRequest.getJoinDate());
            jdbcTemplate.update( "INSERT INTO courseProgress(userName,courseId) values(?,?)", userName, enrollmentRequest.getCourseId());
            List<Chapter> chaptersOfCourse = jdbcTemplate.query("SELECT * FROM chapter WHERE courseId = ?",(rs, rowNum) -> {
                return new Chapter(rs.getInt("chapterId"), rs.getInt("courseId"), rs.getInt("chapterNumber"),rs.getString("chapterName"),rs.getString("chapterDuration"));
            },enrollmentRequest.getCourseId());

            for(int i=0;i<chaptersOfCourse.size();i++)
            {
                List<Lesson> lessonsOfChapter = new ArrayList<>();
                try
                {
                    Integer testIdOfChapter = jdbcTemplate.queryForObject("SELECT testId FROM test WHERE chapterId = ?",new Object[] {chaptersOfCourse.get(i).getChapterId()},Integer.class);
                    jdbcTemplate.update( "INSERT INTO chapterProgress(userName,courseId,chapterId,testId) values(?,?,?,?)", userName, enrollmentRequest.getCourseId(),chaptersOfCourse.get(i).getChapterId(),testIdOfChapter);
                    lessonsOfChapter = jdbcTemplate.query( "SELECT * FROM lesson WHERE chapterId = ?",(rs, rowNum) -> {
                        return new Lesson(rs.getInt("lessonId"), rs.getInt("lessonNumber"), rs.getInt("chapterId"),rs.getString("lessonName"), rs.getString("lessonduration"),rs.getString("videoLink"));
                    },chaptersOfCourse.get(i).getChapterId());
                }
                catch(Exception exception)
                {
                    jdbcTemplate.update( "INSERT INTO chapterProgress(userName,courseId,chapterId) values(?,?,?)", userName, enrollmentRequest.getCourseId(),chaptersOfCourse.get(i).getChapterId());
                    lessonsOfChapter = jdbcTemplate.query( "SELECT * FROM lesson WHERE chapterId = ?",(rs, rowNum) -> {
                        return new Lesson(rs.getInt("lessonId"), rs.getInt("lessonNumber"), rs.getInt("chapterId"),rs.getString("lessonName"), rs.getString("lessonduration"),rs.getString("videoLink"));
                    },chaptersOfCourse.get(i).getChapterId());
                }
                for(int j=0;j<lessonsOfChapter.size();j++)
                {
                    jdbcTemplate.update("INSERT INTO lessonProgress(userName,chapterId,lessonId) values(?,?,?)", userName,chaptersOfCourse.get(i).getChapterId(),lessonsOfChapter.get(j).getLessonId());
                }
            }
        }
        String courseName = jdbcTemplate.queryForObject("SELECT courseName FROM course WHERE courseId = ?", new Object[] {enrollmentRequest.getCourseId()}, String.class);
        String coursePhoto = jdbcTemplate.queryForObject("SELECT coursePhoto FROM course WHERE courseId=?",new Object[] {enrollmentRequest.getCourseId()}, String.class);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formatDateTime = now.format(format);
        jdbcTemplate.update("INSERT INTO notification(userName,description,notificationUrl,timeStamp) values(?,?,?,?)",userName,"Joined a new course - "+courseName, coursePhoto,formatDateTime);
        return "Enrolled successfully";
    }
    public void updateVideoPauseTime(VideoPauseRequest videoPauseRequest)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //System.out.println(videoPauseRequest.getPauseTime());
        Time pauseTime = videoPauseRequest.getPauseTime();
        String videoPauseTime =pauseTime.toString();
        jdbcTemplate.update( "UPDATE lessonProgress SET pauseTime=? WHERE lessonId=? and userName=? and chapterId=?",videoPauseTime, videoPauseRequest.getLessonId(), userName,videoPauseRequest.getChapterId());
        VideoPauseResponse videoPauseResponse1 = jdbcTemplate.queryForObject("SELECT  userName,  pauseTime,  lessonId, chapterId FROM lessonProgress  WHERE lessonId=? and userName=? and chapterId=?",(rs, rowNum) -> {
            return new VideoPauseResponse(rs.getString("userName"),rs.getString("pauseTime"), rs.getInt("lessonId"), rs.getInt("chapterId"));
        },videoPauseRequest.getLessonId(),userName,videoPauseRequest.getChapterId());
        String lessonDuration = jdbcTemplate.queryForObject("SELECT lessonDuration FROM lesson WHERE lessonId=?", new Object[] {videoPauseRequest.getLessonId()}, String.class);
        if(lessonDuration.equals(videoPauseTime))
        {
            jdbcTemplate.update("UPDATE lessonProgress SET lessonCompletedStatus=? WHERE lessonId = ? and userName=? and chapterId=?", true,videoPauseRequest.getLessonId(),userName,videoPauseRequest.getChapterId());
            jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus=? WHERE lessonId = ? and userName=? and chapterId=?", false,videoPauseRequest.getLessonId(),userName,videoPauseRequest.getChapterId());
        }
        else if (!(lessonDuration.equals("00:00:00")))
        {
            jdbcTemplate.update("UPDATE chapterProgress SET chapterStatus=? WHERE chapterId=? and userName=? and courseId=?",true,videoPauseRequest.getChapterId(),userName,videoPauseRequest.getCourseId());
            jdbcTemplate.update("UPDATE lessonProgress SET lessonStatus = ? WHERE lessonID=? and username=?", true,videoPauseRequest.getLessonId(), userName);
            System.out.println("updated++");
        }
    }
//    public Map<Integer, List<Notification>> pullNotification()
//    {
//        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
//        try
//        {
//            Map<Integer,List<Notification>> userNotifications = new HashMap<>();
//            List<Notification> notifications = jdbcTemplate.query("SELECT description,timestamp,notificationUrl FROM notification WHERE username=?", (rs, rowNum) -> {
//                return new Notification(rs.getString("description"), rs.getTimestamp("timeStamp"),rs.getString("notificationUrl"));
//            },userName);
//            System.out.println(notifications);
//            userNotifications.put(notifications.size(), notifications);
//            return userNotifications;
//        }
//        catch(EmptyResultDataAccessException e)
//        {
//            return null;
//        }
//    }
}