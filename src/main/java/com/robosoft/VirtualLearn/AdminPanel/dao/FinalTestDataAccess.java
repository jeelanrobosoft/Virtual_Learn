package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.robosoft.VirtualLearn.AdminPanel.entity.*;
import com.robosoft.VirtualLearn.AdminPanel.response.FinalTestResultResponse;
import com.robosoft.VirtualLearn.AdminPanel.response.SubmitResponse;
import com.robosoft.VirtualLearn.AdminPanel.service.FinalTestService;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.DOWNLOAD_URL;
import static com.robosoft.VirtualLearn.AdminPanel.entity.PushNotification.sendPushNotification;

@Service
public class FinalTestDataAccess {
    @Autowired
    private JdbcTemplate jdbcTemplate;


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

    public FinalTestResultResponse getFinalTestResult(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        float chapterTestPercentage = jdbcTemplate.queryForObject("select coursePercentage from courseProgress where userName='" + userName + "' and courseId=(select distinct(courseId) from chapterProgress where testId=" + testId + ")", Float.class);
        String courseName =jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + testId + "))", String.class);
        Integer courseId = jdbcTemplate.queryForObject("select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + testId + " and userName='" + userName + "')",Integer.class);
        String certificateUrl = jdbcTemplate.queryForObject("select certificateUrl from certificate where userName='" + userName + "' and courseId=(select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + testId + " and userName='" + userName + "'))", String.class);
        return new FinalTestResultResponse(courseName,chapterTestPercentage,certificateUrl,courseId);
    }

    public String uploadProfilePhoto(MultipartFile profilePhoto)
    {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dbmgzhnzv",
                "api_key", "517396485856626",
                "api_secret", "iJJQWYkddrRz8DA_MRg01ZYXXbk",
                "secure", "true"));
        cloudinary.config.secure = true;
        try
        {
            Map params1 = ObjectUtils.asMap(
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
            );
            Map uploadResult = cloudinary.uploader().upload(profilePhoto.getBytes(), params1);
            //String publicId = uploadResult.get("public_id").toString();
            String url = uploadResult.get("secure_url").toString();

            return url;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }
    public SubmitResponse userAnswers(UserAnswers userAnswers) throws IOException, ParseException {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        float chapterTestPercentage = updateUserAnswerTable(userAnswers);
        jdbcTemplate.update("update chapterProgress set chapterTestPercentage=" + chapterTestPercentage + ",chapterCompletedStatus=true,chapterStatus=false where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'");
        int courseId = jdbcTemplate.queryForObject("select courseId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'", Integer.class);
        float sumOfChapterPercentage = jdbcTemplate.queryForObject("select sum(chapterTestPercentage) from chapterProgress where courseId=" + courseId + " and userName='" + userName + "' and chapterTestPercentage>=0", Integer.class);
        System.out.println("sumOfChapterPercentage "+sumOfChapterPercentage);
        float totalPercentage = Integer.parseInt(((String.valueOf(jdbcTemplate.queryForObject("select count(testId) from chapterProgress where courseId=" + courseId + " and userName='" + userName + "'", Integer.class))) + "00"));
        float coursePercentage = (sumOfChapterPercentage / totalPercentage) * 100;
        System.out.println("totalPercentage "+totalPercentage);
        System.out.println("coursePercentage "+coursePercentage);
        String coursePhoto = jdbcTemplate.queryForObject("select coursePhoto from course where courseId=(select courseId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "')", String.class);
        String chapterName = jdbcTemplate.queryForObject("select chapterName from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", String.class);
        String description = "Completed course" + " - " + jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'))", String.class);
        String description1 = "You Scored " + jdbcTemplate.queryForObject("select chapterTestPercentage from chapterProgress where chapterId=(select chapterId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "') and userName='" + userName + "'", String.class) + "% in course " + jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'))", String.class);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        String formatDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, description, formatDateTime, coursePhoto);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, description1, formatDateTime, coursePhoto);
        jdbcTemplate.update("update courseProgress set coursePercentage=" + coursePercentage + ",courseCompletedStatus=true where courseId=" + courseId + " and userName='" + userName + "'");
        String fcmToken = jdbcTemplate.queryForObject("select fcmToken from user where userName='" + userName + "'", String.class);
        sendPushNotification(fcmToken,description,"Congratulations");
        sendPushNotification(fcmToken,description1,"Hooray...!");
        LocalDate courseCompletedDate = LocalDate.now();
        jdbcTemplate.update("update enrollment set completedDate='" + courseCompletedDate + "',courseScore=" + coursePercentage + " where userName='" + userName + "' and courseId=(select courseId from chapter where chapterId=(select chapterId from chapterProgress where testId=" + userAnswers.getTestId() + " and userName='" + userName + "'))");
        if(coursePercentage >= 75.0)
            //finalTestService.certificate(userAnswers.getTestId());
            this.certificate(userAnswers.getTestId());
        else
            this.participationCertificate(userAnswers.getTestId());
        Integer chapterNumber = jdbcTemplate.queryForObject("select chapterNumber from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + ")", Integer.class);
        String courseName = jdbcTemplate.queryForObject("select courseName from course where courseId=(select courseId from chapter where chapterId=(select distinct(chapterId) from chapterProgress where testId=" + userAnswers.getTestId() + "))", String.class);
        return new SubmitResponse(chapterTestPercentage,chapterNumber,courseName,chapterName);
    }


    public void certificate(Integer testId) throws IOException, ParseException {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String fullName = jdbcTemplate.queryForObject("SELECT fullName FROM user WHERE username=?",String.class,userName);
        String courseName = jdbcTemplate.queryForObject("SELECT courseName FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", String.class, testId);
        Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", Integer.class, testId);
        String joinDate = jdbcTemplate.queryForObject("SELECT joinDate FROM enrollment WHERE userName = ? and courseId=?", String.class, userName, courseId);
        String completedDate = jdbcTemplate.queryForObject("SELECT completedDate FROM enrollment WHERE userName = ? and courseId=?", String.class, userName, courseId);
        String duration = jdbcTemplate.queryForObject("SELECT courseDuration FROM course WHERE courseId = ?", String.class, courseId);
        String certificateUrl = String.format(DOWNLOAD_URL, URLEncoder.encode("FinalCertificate.png"));
        BufferedImage image = ImageIO.read(new URL(certificateUrl));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm"); // 12-hour format
        java.util.Date d1 = format.parse(duration);
        java.sql.Time pastime = new java.sql.Time(d1.getTime());
        int hour = pastime.getHours();
        int minute = pastime.getMinutes();
        Graphics g = image.getGraphics();
        g.setFont(g.getFont().deriveFont(25f));
        g.setColor(Color.BLACK);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.drawString("Certificate of Completion", 90, 190);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 70));
        g.setColor(Color.RED);
        g.drawString(fullName.toUpperCase(), 90, 310);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.setColor(Color.BLACK);
        if(courseName != null) {
            g.drawString(courseName, 90, 460);
        }
        g.setFont(new Font("TimesRoman", Font.PLAIN, 35));
        g.drawString("Join Date: " + joinDate + " Completed Date: " + completedDate + " " + hour + "h " + minute + "m ", 90, 550);
        String certificateNumber = " Certificate Number: CER57RF9" + userName + "S978" + courseId;
        g.drawString(certificateNumber, 90, 700);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        byte[] imageByte = out.toByteArray();
        MultipartFile multipartFile11 = new MultipartImage(imageByte, userName+courseId, "image", "png", imageByte.length);
        String url = uploadProfilePhoto(multipartFile11);
        System.out.println("COMPLETED DATE "+completedDate);
        jdbcTemplate.update("delete from certificate where userName='" + userName + "' and courseId=" + courseId);
        jdbcTemplate.update("INSERT INTO certificate(certificateNumber,courseId,UserName,certificateUrl) values(?,?,?,?)", certificateNumber, courseId, userName, url);
    }

    public void participationCertificate(Integer testId) throws IOException, ParseException {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String fullName = jdbcTemplate.queryForObject("SELECT fullName FROM user WHERE username=?",String.class,userName);
        String courseName = jdbcTemplate.queryForObject("SELECT courseName FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", String.class, testId);
        Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", Integer.class, testId);
        String joinDate = jdbcTemplate.queryForObject("SELECT joinDate FROM enrollment WHERE userName = ? and courseId=?", String.class, userName, courseId);
        String completedDate = jdbcTemplate.queryForObject("SELECT completedDate FROM enrollment WHERE userName = ? and courseId=?", String.class, userName, courseId);
        String duration = jdbcTemplate.queryForObject("SELECT courseDuration FROM course WHERE courseId = ?", String.class, courseId);
        String certificateUrl = String.format(DOWNLOAD_URL, URLEncoder.encode("participationcertificate.jpg"));
        System.out.println(certificateUrl);
        BufferedImage image = ImageIO.read(new URL(certificateUrl));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm"); // 12-hour format
        java.util.Date d1 = format.parse(duration);
        java.sql.Time pastime = new java.sql.Time(d1.getTime());
        int hour = pastime.getHours();
        int minute = pastime.getMinutes();
        Graphics g = image.getGraphics();
        g.setFont(g.getFont().deriveFont(25f));
        g.setColor(Color.BLACK);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.drawString("Certificate of Completion", 90, 190);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 70));
        g.setColor(Color.RED);
        g.drawString(fullName.toUpperCase(), 90, 310);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.setColor(Color.BLACK);
        if(courseName != null) {
            g.drawString(courseName, 90, 460);
        }
        g.setFont(new Font("TimesRoman", Font.PLAIN, 35));
        g.drawString("Join Date: " + joinDate + " Completed Date: " + completedDate + " " + hour + "h " + minute + "m ", 90, 550);
        String certificateNumber = " Certificate Number: CER57RF9" + userName + "S978" + courseId;
        g.drawString(certificateNumber, 90, 700);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        byte[] imageByte = out.toByteArray();
        MultipartFile multipartFile11 = new MultipartImage(imageByte, userName+courseId, "image", "png", imageByte.length);
        String url = uploadProfilePhoto(multipartFile11);
        System.out.println(url);
        System.out.println("COMPLETED DATE "+completedDate);
        jdbcTemplate.update("delete from certificate where userName='" + userName + "' and courseId=" + courseId);
        jdbcTemplate.update("INSERT INTO certificate(certificateNumber,courseId,UserName,certificateUrl) values(?,?,?,?)", certificateNumber, courseId, userName, url);
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
        int correctAnswerCount = jdbcTemplate.queryForObject("select count(*) from userAnswer where userAnswerStatus=true and testId=" + userAnswers.getTestId() + " and userName='" + userName + "'", Integer.class);
        System.out.println("Correct Answer Count" + correctAnswerCount);
        int questionCount = jdbcTemplate.queryForObject("select questionsCount from test where testId=" + userAnswers.getTestId(), Integer.class);
        System.out.println("Question Answer Count" + questionCount);
        float chapterTestPercentage = (correctAnswerCount / (float) questionCount) * 100;
        System.out.println("FT: " + chapterTestPercentage);
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

    public float calculateOverallScore(Integer courseId,String userName){
        float sumOfChapterPercentage = jdbcTemplate.queryForObject("select sum(chapterTestPercentage) from chapterProgress where courseId=" + courseId + " and userName='" + userName + "' and chapterTestPercentage>=0", Integer.class);
        System.out.println("sumOfChapterPercentage "+sumOfChapterPercentage);
        float totalPercentage = Integer.parseInt(((String.valueOf(jdbcTemplate.queryForObject("select count(testId) from chapterProgress where courseId=" + courseId + " and userName='" + userName + "'", Integer.class))) + "00"));
        float coursePercentage = (sumOfChapterPercentage / totalPercentage) * 100;
        return coursePercentage;
    }

    public void updateCourseScore(float coursePercentage,Integer courseId,String userName){
        jdbcTemplate.update("update courseProgress set coursePercentage=" + coursePercentage + ",courseCompletedStatus=true where courseId=" + courseId + " and userName='" + userName + "'");

    }
}
