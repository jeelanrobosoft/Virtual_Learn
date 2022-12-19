package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.robosoft.VirtualLearn.AdminPanel.dto.ResultAnswerRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultHeaderRequest;
import com.robosoft.VirtualLearn.AdminPanel.entity.*;
import com.robosoft.VirtualLearn.AdminPanel.response.SubmitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static com.robosoft.VirtualLearn.AdminPanel.entity.PushNotification.sendPushNotification;

@Service
public class ModuleTestDataAccess {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public ModuleTest moduleTestQuestions(Integer testId) {
        List<Question> questions;
        ModuleTest moduleTest;
        String query = "select questionId,questionName,option_1,option_2,option_3,option_4 from question where testId=?";
        try {
            questions = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Question.class), testId);
            moduleTest = jdbcTemplate.queryForObject("select testId,testName,testDuration,questionsCount from test where testId=" + testId, new BeanPropertyRowMapper<>(ModuleTest.class));
        } catch (Exception e) {
            return null;
        }
        moduleTest.setChapterNumber(jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + testId + ")", Integer.class));
        moduleTest.setChapterName(jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + testId + ")", String.class));
        moduleTest.setQuestions(questions);
        return moduleTest;
    }

    public SubmitResponse userAnswers(UserAnswers userAnswers) throws SQLException {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
//        Integer status = checkQuestions(userAnswers);
//        if(status == 0)
//            return null;
        float chapterTestPercentage = updateUserAnswerTable(userAnswers);
        jdbcTemplate.update("update chapterProgress set chapterTestPercentage=" + chapterTestPercentage + " where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'");
        jdbcTemplate.update("update chapterProgress set chapterCompletedStatus=true,chapterStatus=false where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'");
        activeNextLessonStatus(userAnswers.getTestId());
        String coursePhoto = jdbcTemplate.queryForObject("select coursePhoto from course where courseId=(select distinct(courseId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class);
        String chapterName = jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class);
        String description = "Completed Chapter " + jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + " - " + chapterName + " - " + jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        String description1 = "You Scored " + jdbcTemplate.queryForObject("select chapterTestPercentage from chapterProgress where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ") and userName='" + userName + "'", String.class) + "% in Chapter" + jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + " - " + chapterName +", of course - " + jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        String formatDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, description, formatDateTime, coursePhoto);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, description1, formatDateTime, coursePhoto);
        String fcmToken = jdbcTemplate.queryForObject("select fcmToken from user where userName='" + userName + "'", String.class);
        sendPushNotification(fcmToken,description,"Congratulations");
        sendPushNotification(fcmToken,description1,"Hooray...!");
        Integer chapterNumber = jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", Integer.class);
        String courseName = jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        return new SubmitResponse(chapterTestPercentage, chapterNumber, courseName, chapterName);
    }

    private Integer checkQuestions(UserAnswers userAnswers) throws SQLException {
        List<Integer> questionIds =jdbcTemplate.queryForList("select questionId from question where testId=" + userAnswers.getTestId(), Integer.class);
        List<Integer> userQuestions = new ArrayList<>();
        for (Answers answers: userAnswers.getUserAnswers()) {
            userQuestions.add(answers.getQuestionId());
        }
        if(questionIds.equals(userQuestions))
            return 1;
        return 0;
    }

    public float updateUserAnswerTable(UserAnswers userAnswers) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        jdbcTemplate.update("delete from userAnswer where userName='" + userName + "' and testId=" + userAnswers.getTestId());
        String query = "select chapterId from test where testId=" + userAnswers.getTestId();
        int chapterId = jdbcTemplate.queryForObject(query, Integer.class);
        query = "select courseId from chapter where chapterId=" + chapterId;
        int courseId = jdbcTemplate.queryForObject(query, Integer.class);
        for (Answers uAnswers : userAnswers.getUserAnswers()) {
            query = "insert into userAnswer values('" + userName + "'" + "," + courseId + "," + chapterId + "," + userAnswers.getTestId() + "," + uAnswers.getQuestionId() + "," + "'" + uAnswers.getCorrectAnswer() + "'" + "," + "(select if((select correctAnswer from question where questionId=" + uAnswers.getQuestionId() + ") ='" + uAnswers.getCorrectAnswer() + "'" + ",true,false)))";
            jdbcTemplate.update(query);
        }
        Integer correctAnswerCount = jdbcTemplate.queryForObject("select count(*) from userAnswer where userAnswerStatus=true and testId=" + userAnswers.getTestId() + " and userName='" + userName + "'", Integer.class);
        Integer questionCount = jdbcTemplate.queryForObject("select questionsCount from test where testId=" + userAnswers.getTestId(), Integer.class);
        float chapterTestPercentage = (correctAnswerCount / (float) questionCount) * 100;
        return chapterTestPercentage;
    }

    public ResultHeaderRequest getResultHeader(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        float chapterTestPercentage = jdbcTemplate.queryForObject("select chapterTestPercentage from chapterProgress where testId=" + testId + " and userName='" + userName + "'", Float.class);
        String chapterName = jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select chapterId from test where testId=" + testId + ")", String.class);
        Integer chapterNumber = jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select chapterId from test where testId=" + testId + ")", Integer.class);
        String courseName = jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select chapterId from test where testId=" + testId + "))", String.class);
        int totalNumberOfQuestions = jdbcTemplate.queryForObject("select questionsCount from test where testId=" + testId, Integer.class);
        System.out.println(totalNumberOfQuestions);
        int correctAnswers = jdbcTemplate.queryForObject("select count(*) from userAnswer where userAnswerStatus=true and testId=" + testId + "' and userName='" + userName + "'", Integer.class);
        System.out.println(correctAnswers);
        int wrongAnswers = totalNumberOfQuestions - correctAnswers;
        System.out.println(wrongAnswers);
        return new ResultHeaderRequest(chapterNumber, chapterName, chapterTestPercentage, courseName, correctAnswers, wrongAnswers, totalNumberOfQuestions);
    }

    public List<ResultAnswerRequest> getResultAnswers(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return jdbcTemplate.query("select question.questionId,questionName,option_1,option_2,option_3,option_4,correctAnswer,userAnswer,userAnswerStatus from question inner join userAnswer on question.questionId=userAnswer.questionId where userAnswer.testId=" + testId + " and userName='" + userName + "'", new BeanPropertyRowMapper<>(ResultAnswerRequest.class));
    }

    public String checkForCompletedStatus(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer status = jdbcTemplate.queryForObject("select count(chapterCompletedStatus) from chapterProgress where userName='" + userName + "' and testId=" + testId + " and chapterCompletedStatus=true", Integer.class);
        if (status == 0)
            return null;
        return "You have already attended test";
    }


    public void activeNextLessonStatus(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ChapterId> chapterIds = jdbcTemplate.query("select chapterId from chapter where courseId=(select distinct(courseId) from chapterProgress where testId=" + testId + " and userName='" + userName + "')", new BeanPropertyRowMapper<>(ChapterId.class));
        Integer completedChapterId = jdbcTemplate.queryForObject("select distinct(chapterId) from chapterProgress where testId=" + testId, Integer.class);
        ListIterator iterator = chapterIds.listIterator();
        while (iterator.hasNext()) {
            ChapterId chapterId = (ChapterId) iterator.next();
            if (chapterId.getChapterId() == completedChapterId) {
                ChapterId nextChapterId = (ChapterId) iterator.next();
                jdbcTemplate.update("update lessonProgress set lessonStatus=true where lessonId=" + jdbcTemplate.query("select lessonId from lesson where chapterId=" + nextChapterId.getChapterId(), new BeanPropertyRowMapper<>(Lesson.class)).get(0).getLessonId());
                jdbcTemplate.update("update chapterProgress set chapterStatus=true where chapterId=" + nextChapterId.getChapterId() + " and userName='" + userName + "'");
            }
        }
    }

    public Integer checkForCourseScore(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer courseId = jdbcTemplate.queryForObject("select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + testId + " and userName='" + userName + "')",Integer.class);
        return jdbcTemplate.queryForObject("select courseScore from enrollment where userName=? and courseId=?", Integer.class,userName,courseId);
    }

    public Integer getCourseId(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return jdbcTemplate.queryForObject("select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + testId + " and userName='" + userName + "')",Integer.class);

    }

}


