package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.robosoft.VirtualLearn.AdminPanel.entity.Answers;
import com.robosoft.VirtualLearn.AdminPanel.entity.FinalTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.Question;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FinalTestDataAccess {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public FinalTest getFinalTestS(Integer testId) {
        List<Question> questions;
        FinalTest finalTest;
        String query = "select questionId,questionName,option_1,option_2,option_3,option_4 from question where testId=?";
        try {
            questions = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Question.class), testId);
            finalTest = jdbcTemplate.queryForObject("select testId,testName,testDuration,questionsCount from test where testId=" + testId, new BeanPropertyRowMapper<>(FinalTest.class));
        } catch (Exception e) {
            return null;
        }
        finalTest.setChapterNumber(jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + testId + ")", Integer.class));
        finalTest.setChapterName(jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + testId +")", String.class));
        finalTest.setQuestions(questions);
        return finalTest;
    }

    public Float getFinalTestResult(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return jdbcTemplate.queryForObject("select coursePercentage from courseProgress where userName='" + userName + "' and courseId=(select distinct(courseId) from chapterProgress where testId=" + testId + ")", Float.class);
    }

    public float userAnswers(UserAnswers userAnswers) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        float chapterTestPercentage = updateUserAnswerTable(userAnswers);
        jdbcTemplate.update("update chapterProgress set chapterTestPercentage=" + chapterTestPercentage + ",chapterCompletedStatus=true,chapterStatus=false where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'");
        int courseId = jdbcTemplate.queryForObject("select courseId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'", Integer.class);
        float sumOfChapterPercentage = jdbcTemplate.queryForObject("select sum(chapterTestPercentage) from chapterProgress where courseId=" + courseId + " and userName='" + userName + "'", Float.class);
        float totalPercentage = Integer.parseInt(((String.valueOf(jdbcTemplate.queryForObject("select count(chapterNumber) from chapter where courseId=" + courseId, Integer.class))) + "00"));
        float coursePercentage = (sumOfChapterPercentage / totalPercentage) * 100;
        String coursePhoto = jdbcTemplate.queryForObject("select coursePhoto from course where courseId=(select courseId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "')", String.class);
        String description = "Completed course" + " - " + jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'))", String.class);
        String description1 = "You Scored " + jdbcTemplate.queryForObject("select chapterTestPercentage from chapterProgress where chapterId=(select chapterId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "') and userName='" + userName + "'", String.class) + "% in course " + jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'))", String.class);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formatDateTime = dateTime.format(format);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, description, formatDateTime, coursePhoto);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, description1, formatDateTime, coursePhoto);
        jdbcTemplate.update("update courseProgress set coursePercentage=" + coursePercentage + ",courseCompletedStatus=true where courseId=" + courseId + " and userName='" + userName + "'");
        LocalDate courseCompletedDate = LocalDate.now();
        jdbcTemplate.update("update enrollment set completedDate='" + courseCompletedDate + "',courseScore=" + coursePercentage + " where userName='" + userName + "' and courseId=(select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'))");
        String congratulationsMessage = "You have Completed Chapter " + jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + ", Final Test from course: " +  jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        return chapterTestPercentage;
    }

    public float updateUserAnswerTable(UserAnswers userAnswers) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String query = "select chapterId from test where testId=" + userAnswers.getTestId();
        int chapterId = jdbcTemplate.queryForObject(query, Integer.class);
        query = "select courseId from chapter where chapterId=" + chapterId;
        int courseId = jdbcTemplate.queryForObject(query, Integer.class);
        for (Answers uAnswers : userAnswers.getUserAnswers()) {
            query = "insert into userAnswer values('" + userName + "'" + "," + courseId + "," + chapterId + "," + userAnswers.getTestId() + "," + uAnswers.getQuestionId() + "," + "'" + uAnswers.getCorrectAnswer() + "'" + "," + "(select if((select correctAnswer from question where questionId=" + uAnswers.getQuestionId() + ") ='" + uAnswers.getCorrectAnswer() + "'" + ",true,false)))";
            jdbcTemplate.update(query);
        }
        int correctAnswerCount = jdbcTemplate.queryForObject("select count(*) from userAnswer where userAnswerStatus=true and testId=" + userAnswers.getTestId(), Integer.class);
        int questionCount = jdbcTemplate.queryForObject("select questionsCount from test where testId=" + userAnswers.getTestId(), Integer.class);
        float chapterTestPercentage = (correctAnswerCount / (float) questionCount) * 100;
        return chapterTestPercentage;
    }

    public String checkForCompletedStatus(Integer testId){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer userStatus = jdbcTemplate.queryForObject("select count(*) from user where userName='" + userName + "'",Integer.class);
        if(userStatus == 0)
            return "User does not exists";
        Integer status = jdbcTemplate.queryForObject("select count(chapterCompletedStatus) from chapterProgress where userName='" + userName + "' and testId=" + testId + " and chapterCompletedStatus=true", Integer.class);
        if(status == 0)
            return null;
        return "You have already attended test";
    }
}
