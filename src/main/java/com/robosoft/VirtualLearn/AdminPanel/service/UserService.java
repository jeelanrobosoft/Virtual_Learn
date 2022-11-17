package com.robosoft.VirtualLearn.AdminPanel.service;

import com.robosoft.VirtualLearn.AdminPanel.entity.*;
import com.robosoft.VirtualLearn.AdminPanel.request.EnrollmentRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.VideoPauseRequest;
import com.robosoft.VirtualLearn.AdminPanel.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

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
            return jdbcTemplate.queryForObject("SELECT courseName,coursePhoto,categoryName,chapterCount,lessonCount,courseTagLine,previewVideo,overview.description,testCount,courseMaterialId,courseDuration,learningOutCome,requirements,instructorName,url,profilePhoto,instructor.description AS instructorDescription FROM overview INNER JOIN instructor ON overview.instructorId = instructor.instructorId  INNER JOIN course ON overview.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(OverviewResponse.class), courseId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<CourseResponse> getBasicCourses(int categoryId) {
        return jdbcTemplate.query("SELECT courseName,previewVideo,chapterCount,courseDuration FROM overView INNER JOIN course ON course.courseId = overview.courseId WHERE categoryId = " + categoryId + " AND difficultyLevel = 'Beginner'", new BeanPropertyRowMapper<>(CourseResponse.class));
    }

    public List<CourseResponse> getAdvanceCourses(int categoryId) {
        return jdbcTemplate.query("SELECT courseName,previewVideo,chapterCount,courseDuration FROM overView INNER JOIN course ON course.courseId = overview.courseId WHERE categoryId = " + categoryId + " AND difficultyLevel = 'Advanced'", new BeanPropertyRowMapper<>(CourseResponse.class));
    }

    public List<AllCoursesResponse> getAllCoursesOf(int categoryId) {
        return jdbcTemplate.query("SELECT coursePhoto,courseName,chapterCount,categoryName FROM overView INNER JOIN course ON course.courseId = overview.courseId INNER JOIN category ON category.categoryId = course.categoryId WHERE category.categoryId = " + categoryId + " ", new BeanPropertyRowMapper<>(AllCoursesResponse.class));
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
                OngoingResponse ongoingResponse = jdbcTemplate.queryForObject("SELECT courseName,coursePhoto FROM course WHERE courseId = ?", new BeanPropertyRowMapper<>(OngoingResponse.class), i);
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
        CourseChapterResponse courseChapterResponse = jdbcTemplate.queryForObject("SELECT courseName,categoryName,chapterCount,lessonCount,testCount,courseDuration,courseCompletedStatus FROM overView INNER JOIN course ON Overview.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId INNER JOIN courseProgress on course.courseId = courseProgress.courseId AND userName = ?", new BeanPropertyRowMapper<>(CourseChapterResponse.class), courseId,userName);
        List<ChapterResponse> chapterResponses = new ArrayList<>();
        for (Integer i : chapterIds) {
            chapterResponses.add(getChapterResponse(userName, i));
        }
        courseChapterResponse.setChapterResponses(chapterResponses);
        return courseChapterResponse;
        }catch (Exception e)
        {
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId from chapter WHERE courseId = ?", Integer.class, courseId);
            CourseChapterResponse courseChapterResponse = jdbcTemplate.queryForObject("SELECT courseName,categoryName,chapterCount,lessonCount,testCount,courseDuration FROM overView INNER JOIN course ON Overview.courseId = course.courseId AND course.courseId = ? INNER JOIN category ON course.categoryId = category.categoryId", new BeanPropertyRowMapper<>(CourseChapterResponse.class), courseId);
            List<ChapterResponse> chapterResponses = new ArrayList<>();
            for (Integer i : chapterIds) {
                chapterResponses.add(getChapterResponse(userName, i));
            }
            courseChapterResponse.setChapterResponses(chapterResponses);
            return courseChapterResponse;
        }
    }

    public ChapterResponse getChapterResponse(String userName, Integer chapterId) {
        try {
        return jdbcTemplate.queryForObject("SELECT chapterNumber,chapterName,chapterCompletedStatus FROM chapter INNER JOIN chapterProgress ON chapter.chapterId = chapterProgress.chapterId WHERE chapter.chapterId = ? AND chapterProgress.userName = ?", new BeanPropertyRowMapper<>(ChapterResponse.class), chapterId, userName);

        } catch (Exception e) {
            return jdbcTemplate.queryForObject("SELECT chapterNumber,chapterName FROM chapter WHERE chapterId = ?", new BeanPropertyRowMapper<>(ChapterResponse.class), chapterId);
        }
    }

    public List<LessonResponse> getLessonResponses(Integer chapterId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Integer> lessonIds = jdbcTemplate.queryForList("SELECT lessonId from lesson WHERE chapterId = ?", Integer.class, chapterId);
            List<LessonResponse> lessonResponses = new ArrayList<>();
            for (Integer lessonId : lessonIds) {
                try {
                     lessonResponses.add(jdbcTemplate.queryForObject("SELECT lessonNumber,lessonName,lessonDuration,videoLink,lessonCompletedStatus FROM lesson INNER JOIN lessonProgress on lesson.lessonId = lessonProgress.lessonId AND lessonProgress.userName = ? AND lesson.lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), userName, lessonId));
                } catch (Exception e) {
                    String lessonName = jdbcTemplate.queryForObject("SELECT lessonName FROM lesson WHERE lessonId = ?", String.class, lessonId);
                    if (lessonName.equalsIgnoreCase("Introduction")) {
                         lessonResponses.add(jdbcTemplate.queryForObject("SELECT lessonNumber,lessonName,lessonDuration,videoLink FROM lesson WHERE lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), lessonId));
                    }
                    lessonResponses.add(jdbcTemplate.queryForObject("SELECT lessonNumber,lessonName,lessonDuration FROM lesson WHERE lessonId = ?", new BeanPropertyRowMapper<>(LessonResponse.class), lessonId));
                }
            }return lessonResponses;
        }catch(Exception e){
                return null;
        }
    }

    public List<CompletedResponse> getCourseCompletedResponse()
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return jdbcTemplate.query("SELECT courseName,coursePercentage,coursePhoto FROM course INNER JOIN courseProgress ON course.courseId = courseProgress.courseId AND courseProgress.userName = ? AND courseProgress.courseCompletedStatus = true",new BeanPropertyRowMapper<>(CompletedResponse.class),userName);
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
            List<HomeResponseTopHeader> courseListForStudent = jdbcTemplate.query("SELECT coursePhoto, courseName FROM course",(rs, rowNum) -> {
                return new HomeResponseTopHeader(rs.getString("coursePhoto"), rs.getString("courseName"));
            });
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
        List<HomeAllCourse> allCourses = jdbcTemplate.query("SELECT overview.courseId, coursePhoto, courseName,categoryId, chapterCount FROM course,overView WHERE course.courseId = overView.courseId",(rs, rowNum) -> {
            return new HomeAllCourse(rs.getInt("courseId"),rs.getString("coursePhoto"), rs.getString("courseName"), rs.getInt("categoryId"),rs.getInt("chapterCount"));
        });
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
        List<HomeAllCourse> allNewCourses = jdbcTemplate.query("SELECT course.courseId, coursePhoto, courseName,categoryId, chapterCount FROM course,overview WHERE course.courseId = overview.courseId",(rs, rowNum) -> {
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

    //10-11-2022**********************************************************************************

    public Map<Integer,List<PopularCourseInEachCategory>> popularCoursesInCategory()
    {
        Map<Integer,List<PopularCourseInEachCategory>> topCoursesList  = new HashMap<>();
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
                    List<PopularCourseInEachCategory> popularCourseInEachCategory = jdbcTemplate.query("SELECT c.courseName,o.chapterCount, c.courseDuration,c.previewVideo from course c, overview o , category ct WHERE ct.categoryId = ? and  ct.categoryId = c.categoryId and c.courseId = o.courseId",(rs, rowNum) -> {
                        return new PopularCourseInEachCategory(rs.getString("courseName"), rs.getInt("chapterCount"), rs.getString("courseDuration"),rs.getString("previewVideo"));
                    },categoriesList.get(i).getCategoryId());
                    System.out.println(popularCourseInEachCategory);
                    topCoursesList.put(categoriesList.get(i).getCategoryId(),popularCourseInEachCategory);

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
        String result = "";
        try
        {
            Enrollment enrollmentRequest1 = jdbcTemplate.queryForObject("SELECT * FROM enrollment WHERE userName= ? and courseId = ?",(rs, rowNum) -> {
                return new Enrollment(rs.getString("userName"), rs.getInt("courseId"), rs.getDate("joinDate"),rs.getInt("courseScore"));
            },userName,enrollmentRequest.getCourseId());
            return "You have already enrolled for this course";
        }
        catch(EmptyResultDataAccessException e)
        {
            jdbcTemplate.update("INSERT INTO enrollment(userName,courseId,joinDate) values(?,?,?)",userName, enrollmentRequest.getCourseId(), enrollmentRequest.getJoinDate());
            jdbcTemplate.update( "INSERT INTO courseProgress(userName,courseId) values(?,?)", userName, enrollmentRequest.getCourseId());
            List<Chapter> chaptersOfCourse = jdbcTemplate.query("SELECT * FROM chapter WHERE courseId = ?",(rs, rowNum) -> {
                return new Chapter(rs.getInt("chapterId"), rs.getInt("courseId"), rs.getInt("chapterNumber"),rs.getString("chapterName"),rs.getString("chapterDuration"));
            },enrollmentRequest.getCourseId());

            if(chaptersOfCourse.size() == 0)
            {
                result = "Chapters are not available for this course";
            }
            else
            {
                for(int i=0;i<chaptersOfCourse.size();i++)
                {
                    try
                    {
                        Integer testIdOfChapter = jdbcTemplate.queryForObject("SELECT testId FROM test WHERE chapterId = ?",new Object[] {chaptersOfCourse.get(i).getChapterId()},Integer.class);

                        jdbcTemplate.update( "INSERT INTO chapterProgress(userName,courseId,chapterId,testId) values(?,?,?,?)", userName, enrollmentRequest.getCourseId(),chaptersOfCourse.get(i).getChapterId(),testIdOfChapter);

                    }
                    catch(EmptyResultDataAccessException ex)
                    {
                        result= "Test  is not available available for this course";
                    }
                    List<Lesson> lessonsOfChapter = jdbcTemplate.query( "SELECT * FROM lesson WHERE chapterId = ?",(rs, rowNum) -> {
                        return new Lesson(rs.getInt("lessonId"), rs.getInt("lessonNumber"), rs.getInt("chapterId"),rs.getString("lessonName"), rs.getString("lessonduration"),rs.getString("videoLink"));
                    },chaptersOfCourse.get(i).getChapterId());

                    if(lessonsOfChapter.size() == 0)
                    {
                        result= "Lessons are not available for this course";
                    }
                    else
                    {
                        for(int j=0;j<lessonsOfChapter.size();j++)
                        {
                            jdbcTemplate.update("INSERT INTO lessonProgress(userName,chapterId,lessonId) values(?,?,?)", userName,chaptersOfCourse.get(i).getChapterId(),lessonsOfChapter.get(j).getLessonId());
                        }
                    }
                }
            }
        }
        String courseName = jdbcTemplate.queryForObject("SELECT courseName FROM course WHERE courseId = ?", new Object[] {enrollmentRequest.getCourseId()}, String.class);
        String coursePhoto = jdbcTemplate.queryForObject("SELECT coursePhoto FROM course WHERE courseId=?",new Object[] {enrollmentRequest.getCourseId()}, String.class);
        jdbcTemplate.update("INSERT INTO notification(userName,description,notificationUrl) values(?,?,?)",userName,"Joined a new course - "+courseName, coursePhoto);
        return "You have enrolled for this course successfully, "+result;
    }
    //*******************************11-11-2022********************************************************************************************************************************************************************************************************************
    public void updateVideoPauseTime(VideoPauseRequest videoPauseRequest)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(videoPauseRequest.getPauseTime());
        Time pauseTime = videoPauseRequest.getPauseTime();
        String videoPauseTime =pauseTime.toString();

        jdbcTemplate.update( "UPDATE lessonProgress SET pauseTime=? WHERE lessonId=? and userName=? and chapterId=?",videoPauseTime, videoPauseRequest.getLessonId(), userName,videoPauseRequest.getChapterId());
        VideoPauseResponse videoPauseResponse1 = jdbcTemplate.queryForObject("SELECT  userName,  pauseTime,  lessonId, chapterId FROM lessonProgress  WHERE lessonId=? and userName=? and chapterId=?",(rs, rowNum) -> {
            return new VideoPauseResponse(rs.getString("userName"),rs.getString("pauseTime"), rs.getInt("lessonId"), rs.getInt("chapterId"));
        },videoPauseRequest.getLessonId(),userName,videoPauseRequest.getChapterId());


        String lessonDuration = jdbcTemplate.queryForObject("SELECT lessonDuration FROM lesson WHERe lessonId=?", new Object[] {videoPauseRequest.getLessonId()}, String.class);

        if(lessonDuration.equals(videoPauseResponse1.getPauseTime()))
        {
            jdbcTemplate.update("UPDATE lessonProgress SET lessonCompletedStatus=? WHERe lessonId = ?", true,videoPauseRequest.getLessonId());
        }
    }
    // pull notification
    public Map<Integer, List<Notification>> pullNotification()
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        try
        {
            Map<Integer,List<Notification>> userNotifications = new HashMap<>();
            List<Notification> notifications = jdbcTemplate.query("SELECT description,timestamp,notificationUrl FROM notification WHERE username=?", (rs, rowNum) -> {
                return new Notification(rs.getString("description"), rs.getTimestamp("timeStamp"),rs.getString("notificationUrl"));
            },userName);
            System.out.println(notifications);
            userNotifications.put(notifications.size(), notifications);
            return userNotifications;
        }
        catch(EmptyResultDataAccessException e)
        {
            return null;
        }
    }

}