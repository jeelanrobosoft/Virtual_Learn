package com.robosoft.VirtualLearn.AdminPanel.service;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.robosoft.VirtualLearn.AdminPanel.dao.FinalTestDataAccess;
import com.robosoft.VirtualLearn.AdminPanel.entity.FinalTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.request.CertificateRequest;
import com.robosoft.VirtualLearn.AdminPanel.request.FinalTestRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.*;
import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.DOWNLOAD_URL;

@Service
public class FinalTestService
{

    @Autowired
    FinalTestDataAccess testDataAccess;

    @Autowired
    JdbcTemplate jdbcTemplate;
    public FinalTest finalTestService(FinalTestRequest request)
    {
        return testDataAccess.getFinalTestS(request);
    }

    public Float getFinalTestResult(FinalTestRequest request)
    {
        return testDataAccess.getFinalTestResult(request);
    }

    public float userAnswers(UserAnswers userAnswers)
    {
        return testDataAccess.userAnswers(userAnswers);
    }

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

    public void certificate(Integer testId) throws IOException, ParseException
    {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String courseName = jdbcTemplate.queryForObject("SELECT courseName FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))",new Object[]{testId}, String.class);
        Integer courseId= jdbcTemplate.queryForObject("SELECT courseId FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", new Object[]{testId}, Integer.class);
        String joinDate = jdbcTemplate.queryForObject("SELECT joinDate FROM enrollment WHERE userName = ? and courseId=?", new Object[]{userName,courseId},String.class);
        String completedDate = jdbcTemplate.queryForObject("SELECT completedDate FROM enrollment WHERE userName = ? and courseId=?", new Object[]{userName,courseId},String.class);
        String duration = jdbcTemplate.queryForObject("SELECT courseDuration FROM course WHERE courseId = ?", new Object[] {courseId}, String.class);
        BufferedImage image = ImageIO.read(new File("src/main/resources/Final Certificate.png"));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm"); // 12 hour format
        java.util.Date d1 =(java.util.Date)format.parse(duration);
        java.sql.Time ppstime = new java.sql.Time(d1.getTime());
        Integer hour = ppstime.getHours();
        Integer minute = ppstime.getMinutes();
        Graphics g = image.getGraphics();
        g.setFont(g.getFont().deriveFont(25f));
        g.setColor(Color.BLACK);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.drawString("Certificate of Completion", 90, 190);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 70));
        g.setColor(Color.RED);
        g.drawString(userName, 90, 310);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.setColor(Color.BLACK);
        g.drawString(courseName, 90,460);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 35));
        g.drawString("Join Date: "+joinDate+" Completed Date: "+completedDate+" "+hour+"h "+minute+"m ", 90,590);
        String certificateNumber = " Certificate Number: CER57RF9"+userName+"S978"+courseId;
        g.drawString(certificateNumber, 90,700);
        g.dispose();
        ImageIO.write(image, "jpg", new File("src/main/resources/CerificateData/"+userName+courseId+".png"));
        File fileItem = new File("C:\\Users\\Chandana I K\\Pictures\\"+userName+courseId+".png");
        System.out.println(fileItem.getName());
        FileInputStream input = new FileInputStream(fileItem);
        MultipartFile multipartFile = new MockMultipartFile("fileItem", fileItem.getName(), "image/png", IOUtils.toByteArray(input));
        String url =getFileUrl(multipartFile);
        jdbcTemplate.update("INSERT INTO certificate(,certificateNumber,courseId,UserName,certificateUrl) values(?,?,?,?)", courseId,userName,url);

    }

    public String viewCertificate(CertificateRequest certificateRequest)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer courseId= jdbcTemplate.queryForObject("SELECT courseId FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", new Object[]{certificateRequest.getTestId()}, Integer.class);
        String certificateUrl = jdbcTemplate.queryForObject("SELECT certificateUrl FROm certificate WHERE userName=? and courseId=?",new Object[] {userName, courseId}, String.class);
        return certificateUrl;
    }
}
