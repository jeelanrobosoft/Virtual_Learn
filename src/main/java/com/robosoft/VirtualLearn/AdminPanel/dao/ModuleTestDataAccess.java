package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.robosoft.VirtualLearn.AdminPanel.dto.ResultAnswerRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultHeaderRequest;
import com.robosoft.VirtualLearn.AdminPanel.entity.Answers;
import com.robosoft.VirtualLearn.AdminPanel.entity.ModuleTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.Question;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.response.SubmitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        moduleTest.setChapterName(jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + testId +")", String.class));
        moduleTest.setQuestions(questions);
        return moduleTest;
    }

    public SubmitResponse userAnswers(UserAnswers userAnswers) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        jdbcTemplate.update("update chapterProgress set chapterCompletedStatus=true,chapterStatus=false where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'");
        float chapterTestPercentage = updateUserAnswerTable(userAnswers);
        jdbcTemplate.update("update chapterProgress set chapterTestPercentage=" + chapterTestPercentage + " where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'");
        String coursePhoto = jdbcTemplate.queryForObject("select coursePhoto from course where courseId=(select distinct(courseId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class);
        String description = "Completed Chapter " + jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + " - Setting up a new project, of course - " + jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        String description1 = "You Scored " + jdbcTemplate.queryForObject("select chapterTestPercentage from chapterProgress where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ") and userName='" + userName + "'", String.class) + "% in Chapter" + jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + " - Setting up a new project, of course - " + jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formatDateTime = dateTime.format(format);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, description, formatDateTime, coursePhoto);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, description1, formatDateTime, coursePhoto);
        String congratulationsMessage = "You have Completed Chapter " + jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + " - " + jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + " from course: " +  jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        return new SubmitResponse(chapterTestPercentage,congratulationsMessage);
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
        Integer correctAnswerCount = jdbcTemplate.queryForObject("select count(*) from userAnswer where userAnswerStatus=true and testId=" + userAnswers.getTestId(), Integer.class);
        Integer questionCount = jdbcTemplate.queryForObject("select questionsCount from test where testId=" + userAnswers.getTestId(), Integer.class);
        float chapterTestPercentage = (correctAnswerCount / (float) questionCount) * 100;
        return chapterTestPercentage;
    }

    public ResultHeaderRequest getResultHeader(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        float chapterTestPercentage = jdbcTemplate.queryForObject("select chapterTestPercentage from chapterProgress where testId=" + testId + " and userName='" + userName + "'", Float.class);
        String chapterName = jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select chapterId from test where testId=" + testId + ")", String.class);
        Integer chapterNumber = jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select chapterId from test where testId=" + testId+ ")", Integer.class);
        String courseName = jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select chapterId from test where testId=" + testId + "))", String.class);
        int totalNumberOfQuestions = jdbcTemplate.queryForObject("select questionsCount from test where testId=" + testId, Integer.class);
        int correctAnswers = jdbcTemplate.queryForObject("select count(*) from userAnswer where userAnswerStatus=true and testId=" + testId, Integer.class);
        int wrongAnswers = totalNumberOfQuestions - correctAnswers;
        return new ResultHeaderRequest(chapterNumber, chapterName, chapterTestPercentage, courseName, correctAnswers, wrongAnswers, totalNumberOfQuestions);
    }

    public List<ResultAnswerRequest> getResultAnswers(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return jdbcTemplate.query("select question.questionId,questionName,option_1,option_2,option_3,option_4,correctAnswer,userAnswer,userAnswerStatus from question inner join userAnswer on question.questionId=userAnswer.questionId where userAnswer.testId=" + testId + " and userName='" + userName + "'", new BeanPropertyRowMapper<>(ResultAnswerRequest.class));
    }

    public String checkForCompletedStatus(Integer testId){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer status = jdbcTemplate.queryForObject("select count(chapterCompletedStatus) from chapterProgress where userName='" + userName + "' and testId=" + testId + " and chapterCompletedStatus=true", Integer.class);
        if(status == 0)
            return null;
        return "You have already attended test";
    }
}
