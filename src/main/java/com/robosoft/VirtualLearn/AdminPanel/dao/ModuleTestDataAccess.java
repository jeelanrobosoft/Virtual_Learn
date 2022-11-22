package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.robosoft.VirtualLearn.AdminPanel.dto.ModuleTestRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultAnswerRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultHeaderRequest;
import com.robosoft.VirtualLearn.AdminPanel.entity.Answers;
import com.robosoft.VirtualLearn.AdminPanel.entity.ModuleTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.Question;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ModuleTestDataAccess
{
    @Autowired
    JdbcTemplate jdbcTemplate;
    public ModuleTest moduleTestQuestions(ModuleTestRequest request)
    {
        List<Question> questions;
        ModuleTest moduleTest;
        String query = "select questionId,questionName,option_1,option_2,option_3,option_4 from question where testId=?";
        try
        {
            questions = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Question.class), request.getTestId());
            moduleTest = jdbcTemplate.queryForObject("select testId,testName,testDuration,questionsCount from test where testId=" + request.getTestId(), new BeanPropertyRowMapper<>(ModuleTest.class));
        }
        catch (Exception e)
        {
            return null;
        }
        moduleTest.setQuestions(questions);
        return moduleTest;
    }
    public float userAnswers(UserAnswers userAnswers)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        jdbcTemplate.update("update chapterProgress set chapterCompletedStatus=true,chapterStatus=false where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'");
        float chapterTestPercentage =  updateUserAnswerTable(userAnswers);
        System.out.println(chapterTestPercentage);
        jdbcTemplate.update("update chapterProgress set chapterTestPercentage=" + chapterTestPercentage+ " where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'");
        String coursePhoto = jdbcTemplate.queryForObject("select coursePhoto from course where courseId=(select distinct(courseId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class);
        String description = "Completed Chapter " + jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + " - Setting up a new project, of course - " +jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        String description1 = "You Scored " + jdbcTemplate.queryForObject("select chapterTestPercentage from chapterProgress where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ") and userName='" + userName + "'", String.class) + "% in Chapter" + jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class) + " - Setting up a new project, of course - " +jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        /* Inserting into notification */
//        String photoUrl = String.format(DOWNLOAD_URL, URLEncoder.encode("password_change_success.png"));
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formatDateTime = dateTime.format(format);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)",userName,description,formatDateTime,coursePhoto);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)",userName,description1,formatDateTime,coursePhoto);
        return chapterTestPercentage;
    }
    public float updateUserAnswerTable(UserAnswers userAnswers)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String query = "select chapterId from test where testId=" + userAnswers.getTestId();
        int chapterId = jdbcTemplate.queryForObject(query, Integer.class);
        query = "select courseId from chapter where chapterId=" + chapterId;
        int courseId = jdbcTemplate.queryForObject(query, Integer.class);
        for (Answers uAnswers : userAnswers.getUserAnswers())
        {
            query = "insert into userAnswer values('" + userName + "'" + "," + courseId + "," + chapterId + "," + userAnswers.getTestId() + "," + uAnswers.getQuestionId() + "," + "'" + uAnswers.getCorrectAnswer() + "'" + "," + "(select if((select correctAnswer from question where questionId=" + uAnswers.getQuestionId() + ") ='" + uAnswers.getCorrectAnswer() + "'" + ",true,false)))";
            jdbcTemplate.update(query);
        }
        int correctAnswerCount = jdbcTemplate.queryForObject("select count(*) from userAnswer where userAnswerStatus=true and testId=" + userAnswers.getTestId(), Integer.class);
        System.out.println(correctAnswerCount);
        int questionCount = jdbcTemplate.queryForObject("select questionsCount from test where testId=" + userAnswers.getTestId(), Integer.class);
        System.out.println(questionCount);
        float chapterTestPercentage = (correctAnswerCount / (float) questionCount) * 100;
        return chapterTestPercentage;
    }
    public ResultHeaderRequest getResultHeader(ModuleTestRequest testRequest)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        float chapterTestPercentage = jdbcTemplate.queryForObject("select chapterTestPercentage from chapterProgress where testId=" + testRequest.getTestId() + " and userName='" + userName + "'", Float.class);
        String chapterName = jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select chapterId from test where testId=" + testRequest.getTestId() + ")", String.class);
        Integer chapterNumber = jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select chapterId from test where testId=" + testRequest.getTestId() + ")", Integer.class);
        String courseName = jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select chapterId from test where testId=" + testRequest.getTestId() + "))", String.class);
        int totalNumberOfQuestions = jdbcTemplate.queryForObject("select questionsCount from test where testId=" + testRequest.getTestId(), Integer.class);
        int correctAnswers = jdbcTemplate.queryForObject("select count(*) from userAnswer where userAnswerStatus=true and testId=" + testRequest.getTestId(), Integer.class);
        int wrongAnswers = totalNumberOfQuestions - correctAnswers;
        return new ResultHeaderRequest(chapterNumber,chapterName,chapterTestPercentage,courseName,correctAnswers,wrongAnswers,totalNumberOfQuestions);
    }
    public List<ResultAnswerRequest> getResultAnswers(ModuleTest request)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return jdbcTemplate.query("select question.questionId,questionName,option_1,option_2,option_3,option_4,correctAnswer,userAnswer,userAnswerStatus from question inner join userAnswer on question.questionId=userAnswer.questionId where userAnswer.testId=" +request.getTestId() + " and userName='" + userName + "'",new BeanPropertyRowMapper<>(ResultAnswerRequest.class));

    }
//    public int getCorrectAnswersCount(UserAnswers userAnswers){
//        int correctAnswerCount = 0;
//        List<Answers> correctAnswers = jdbcTemplate.query("select questionId,correctAnswer from question where testId=" + userAnswers.getTestId(),new BeanPropertyRowMapper<>(Answers.class));
//        List<Answers> usrAnswer = userAnswers.getUserAnswers();
//        for (Answers cAnswers: correctAnswers) {
//            for (Answers uAnswers: usrAnswer) {
//                if(cAnswers.getQuestionId() == uAnswers.getQuestionId()) {
//                    if(cAnswers.getCorrectAnswer().equals(uAnswers.getCorrectAnswer()))
//                        correctAnswerCount++;
//                }
//            }
//        }
//        return correctAnswerCount;
//    }
}
