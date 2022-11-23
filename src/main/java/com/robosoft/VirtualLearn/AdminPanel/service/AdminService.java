package com.robosoft.VirtualLearn.AdminPanel.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.robosoft.VirtualLearn.AdminPanel.entity.*;
import com.robosoft.VirtualLearn.AdminPanel.request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.*;

@Service
public class AdminService
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private File convertMultiPartToFile(MultipartFile file) throws IOException
    {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    private String generateFileName(MultipartFile multiPart)
    {
        return new Date().getTime() + "-" + Objects.requireNonNull(multiPart.getOriginalFilename()).replace(" ", "_");
    }

    public String getFileUrl(MultipartFile multipartFile) throws IOException
    {
        String objectName = generateFileName(multipartFile);
        FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
        File file = convertMultiPartToFile(multipartFile);
        Path filePath = file.toPath();
        Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId(FIREBASE_PROJECT_ID).build().getService();
        BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();
        storage.create(blobInfo, Files.readAllBytes(filePath));
        Blob blob = storage.create(blobInfo, Files.readAllBytes(filePath));
        file.delete();
        return String.format(DOWNLOAD_URL, URLEncoder.encode(objectName));
    }

    public String addInstructor(InstructorRequest instructorRequest) throws IOException
    {
        String profilePhotoLink = getFileUrl(instructorRequest.getProfilePhoto());
        jdbcTemplate.update("INSERT INTO instructor(instructorId, instructorName,url,description,profilePhoto) values(?,?,?,?,?)",instructorRequest.getInstructorId(),instructorRequest.getInstructorName(), instructorRequest.getUrl(),instructorRequest.getDescription(),profilePhotoLink);
        return "Instructor added successfully";
    }

    public int addCategory(CategoryRequest category) throws IOException
    {
        try
        {
            jdbcTemplate.queryForObject("SELECT * FROM category WHERE categoryName = ?", new BeanPropertyRowMapper<>(Category.class),category.getCategoryName());
            return 0;
        }
        catch (Exception e)
        {
//            if(category.getCategoryName().isEmpty())
//                throw new EmptyStackException();
//            else
                String categoryPhoto = getFileUrl(category.getCategoryPhoto());
                return jdbcTemplate.update("INSERT INTO category(categoryName,categoryPhoto) VALUES(?,?)",category.getCategoryName(),categoryPhoto);
        }
    }

    public int addSubCategory(SubCategory subCategory)
    {
        try
        {
            jdbcTemplate.queryForObject("SELECT * FROM subCategory WHERE subCategoryName = ?", new BeanPropertyRowMapper<>(Category.class),subCategory.getSubCategoryName());
            return 0;
        }
        catch (Exception e)
        {
            return jdbcTemplate.update("INSERT INTO subCategory(categoryId,subCategoryName) VALUES(?,?)",subCategory.getCategoryId(),subCategory.getSubCategoryName());
        }
    }

    public String addCourse(CourseRequest courseRequest) throws IOException
    {
        String coursePhotoLink = getFileUrl(courseRequest.getCoursePhoto());
        String courseVideoLink = getFileUrl(courseRequest.getPreviewVideo());
        jdbcTemplate.update("INSERT INTO course(coursePhoto,courseName,previewVideo,categoryId,subCategoryId) VALUES(?,?,?,?,?)",coursePhotoLink,courseRequest.getCourseName(),courseVideoLink,courseRequest.getCategoryId(), courseRequest.getSubCategoryId());
        List<Enrollment> enrolledUsers = jdbcTemplate.query("SELECT UserName FROM enrollment",(rs, rowNum) -> {
            return new Enrollment(rs.getString("userName"));
        });
        for(int i=0;i<enrolledUsers.size();i++)
        {
            String categoryName= jdbcTemplate.queryForObject("SELECT categoryName FROM category WHERE categoryId=?", new Object[] {courseRequest.getCategoryId()}, String.class);
            String courseUrl = String.format(DOWNLOAD_URL,URLEncoder.encode("alert_notification.png"));
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd:MM:yyyy:HH:mm:ss");
            String formatDateTime = now.format(format);

            jdbcTemplate.update("INSERT INTO notification(userName,description,notificationUrl,timeStamp) values(?,?,?,?)",enrolledUsers.get(i).getUserName(),"Hey "+enrolledUsers.get(i).getUserName()+", There is a new course about "+courseRequest.getCourseName()+" added to the topic "+categoryName, courseUrl,formatDateTime);
        }
        return "course added successfully";
    }

    public int addOverView(Overview overview)
    {
        try
        {
            jdbcTemplate.queryForObject("SELECT courseId FROM overview WHERE courseId = ?",new BeanPropertyRowMapper<>(Overview.class),overview.getCourseId());
            return 0;
        }
        catch (Exception e)
        {
            int chapterCount = jdbcTemplate.queryForObject("SELECT COUNT(courseId) FROM chapter WHERE courseId = ?", Integer.class, overview.getCourseId());
            int lessonCount = 0;
            List<Integer> chapterIds = jdbcTemplate.queryForList("SELECT chapterId FROM chapter WHERE courseId = ?", Integer.class,overview.getCourseId());
            for(int i : chapterIds)
            {
                lessonCount += jdbcTemplate.queryForObject("SELECT COUNT(chapterId) FROM lesson WHERE chapterId = ?", Integer.class, i);
            }
            List<Integer> chapterIDs = jdbcTemplate.queryForList("SELECT chapterId FROM chapter WHERE courseId = ?", Integer.class, overview.getCourseId());

            int testCount = 0;
            for (int i : chapterIDs)
            {
                testCount += jdbcTemplate.queryForObject("SELECT COUNT(testId) FROM test WHERE chapterId = ?", Integer.class, i);
            }
            return jdbcTemplate.update("INSERT INTO overView(courseId,courseTagLine,description,chapterCount,lessonCount,testCount,learningOutCome,requirements,instructorId,difficultyLevel) VALUES(?,?,?,?,?,?,?,?,?,?)",
                    overview.getCourseId(), overview.getCourseTagLine(), overview.getDescription(), chapterCount, lessonCount, testCount, overview.getLearningOutCome(),overview.getRequirements(), overview.getInstructorId(),overview.getDifficultyLevel());
        }
    }

    public int addChapter(Chapter chapter)
    {
        return jdbcTemplate.update("INSERT INTO chapter(courseId,chapterNumber,chapterName) VALUES(?,?,?)",chapter.getCourseId(),chapter.getChapterNumber(),chapter.getChapterName());
    }

    public String addLesson(LessonRequest lessonRequest) throws IOException, ParseException
    {
        String lessonVideoLink = getFileUrl(lessonRequest.getVideoLink());
        String lessonTime = lessonRequest.getLessonDuration().toString();
        jdbcTemplate.update("INSERT INTO lesson(lessonNumber,chapterId,lessonName,lessonDuration,videoLink) VALUES(?,?,?,?,?)",lessonRequest.getLessonNumber(),lessonRequest.getChapterId(),lessonRequest.getLessonName(), lessonTime,lessonVideoLink);

        // after adding lessons get the chapter duration add ,update it
        String chapterTime = jdbcTemplate.queryForObject("SELECT chapterDuration FROM chapter WHERE chapterId = ?", new Object[]{lessonRequest.getChapterId()},String.class);
        String lessonTimeInfo = String.valueOf(lessonRequest.getLessonDuration());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date1 = timeFormat.parse(chapterTime);
        Date date2 = timeFormat.parse(lessonTimeInfo);
        long sumOfDurations = date1.getTime() + date2.getTime();
        String courseDuration = timeFormat.format(new Date(sumOfDurations));
        jdbcTemplate.update("UPDATE chapter SET chapterDuration = ? WHERE chapterId = ?", new Object[]{courseDuration,lessonRequest.getChapterId()});

        //after adding chapter duration get the course duration, add all chapters duration under that course , update course duration
        long durationsSum=0;
        String finalDuration="";
        Date durationDate1 = null;
        Date durationDate2;
        jdbcTemplate.update( "UPDATE course SET courseDuration= ? WHERE courseId = ?", new Object[] {"00:00:00", lessonRequest.getCourseId()});
        String courseDurationInfo = jdbcTemplate.queryForObject("SELECT courseDuration FROM course WHERE courseId = ?", new Object[] {lessonRequest.getCourseId()}, String.class);

        List<Chapter> chapterList = jdbcTemplate.query("SELECT chapterDuration FROM chapter WHERE courseId= ?",(rs, rowNum) -> {
            return new Chapter(rs.getString("chapterDuration"));
        }, lessonRequest.getCourseId());
        System.out.println(chapterList);

        for(int i=0;i<chapterList.size();i++)
        {
            String chapterDurationInfo = chapterList.get(i).getChapterDuration();
            SimpleDateFormat timeFormatInfo = new SimpleDateFormat("HH:mm:ss");
            timeFormatInfo.setTimeZone(TimeZone.getTimeZone("UTC"));
            durationDate1 = timeFormatInfo.parse(courseDurationInfo);
            durationDate2 = timeFormatInfo.parse(chapterDurationInfo);
            durationsSum = durationDate1.getTime() + durationDate2.getTime();
            finalDuration = timeFormat.format(new Date(durationsSum));
            courseDurationInfo = finalDuration;
        }
        jdbcTemplate.update("UPDATE chapter SET chapterDuration = ? WHERE chapterId = ?", new Object[] {finalDuration,lessonRequest.getCourseId()});
        return "Lesson Added SuccessFully";
    }

    public String addTest(TestRequest test)
    {
        String testDuration = test.getTestDuration().toString();
        jdbcTemplate.update("INSERT INTO test(testId,testName,chapterId,testDuration,passingGrade) values(?,?,?,?,?)", test.getTestId(), test.getTestName(), test.getChapterId(),testDuration, test.getPassingGrade());
        return "Test Added Successfully";
    }
    //adding questions under each test
    public String addQuestion(Question question)
    {
        int questionsCount = jdbcTemplate.queryForObject("SELECT questionsCount FROM test WHERE testId = ?", new Object[] {question.getTestId()}, Integer.class);
        jdbcTemplate.update("INSERT INTO question(questionId,questionName,testId, option_1, option_2,option_3,option_4,correctAnswer) values(?,?,?,?,?,?,?,?)",question.getQuestionId(), question.getQuestionName(), question.getTestId(), question.getOption_1(), question.getOption_2(), question.getOption_3(),question.getOption_4(),question.getCorrectAnswer());
        jdbcTemplate.update( "UPDATE test SET questionsCount = ? where testId = ?", new Object[] {questionsCount+1, question.getTestId()});
        return "Question added successfully";
    }
    public int addPolicy(Policy policy)
    {
        try
        {
            jdbcTemplate.queryForObject("SELECT termsAndConditions FROM policy WHERE termsAndConditions = ? AND privacyPolicy = ?",String.class,policy.getTermsAndConditions(),policy.getPrivacyPolicy());
            return 0;
        }
        catch (Exception e)
        {
            return jdbcTemplate.update("INSERT INTO policy(termsAndConditions,privacyPolicy) VALUES(?,?)",policy.getTermsAndConditions(),policy.getPrivacyPolicy());
        }
    }

    public String addAdminDetails(AdminRegistration adminRegistration) {
        if(adminRegistration.getUserName() != null && adminRegistration.getPassword() != null && adminRegistration.getFullName() != null){
            if(adminRegistration.getFullName().length() >=  5 && adminRegistration.getPassword().length() > 5 && adminRegistration.getUserName().length() >= 5 ){
        jdbcTemplate.update("insert into admin values(?,?,?)",adminRegistration.getUserName(),adminRegistration.getFullName(),adminRegistration.getMobileNumber());
        jdbcTemplate.update("insert into authenticate values(?,?,?)",adminRegistration.getUserName(),new BCryptPasswordEncoder().encode(adminRegistration.getPassword()),"ROLE_ADMIN");
        return "registered successfully";
            }
        else { return "enter all the fields" ;}
        }
        else { return "enter all the fields" ;}
    }
}
