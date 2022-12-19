package com.robosoft.VirtualLearn.AdminPanel.service;


import com.aspose.pdf.Document;
import com.aspose.pdf.Image;
import com.aspose.pdf.Page;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.robosoft.VirtualLearn.AdminPanel.dao.FinalTestDataAccess;
import com.robosoft.VirtualLearn.AdminPanel.entity.FinalTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.MultipartImage;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.response.FinalTestResultResponse;
import com.robosoft.VirtualLearn.AdminPanel.response.SubmitResponse;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
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
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.*;

@Service
public class FinalTestService {

    @Autowired
    FinalTestDataAccess testDataAccess;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public FinalTest finalTestService(Integer testId) {
        return testDataAccess.getFinalTestS(testId);
    }

    public FinalTestResultResponse getFinalTestResult(Integer testId) {
        return testDataAccess.getFinalTestResult(testId);
    }

    public SubmitResponse userAnswers(UserAnswers userAnswers) {
        return testDataAccess.userAnswers(userAnswers);
    }

    public String checkForCompletedStatus(Integer testId) {
        return testDataAccess.checkForCompletedStatus(testId);
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + Objects.requireNonNull(multiPart.getOriginalFilename()).replace(" ", "_");
    }

    public String getFileUrl(MultipartFile multipartFile) throws IOException {
        String objectName = generateFileName(multipartFile);
        FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
        File file = convertMultiPartToFile(multipartFile);
        Path filePath = file.toPath();
        Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId(FIREBASE_PROJECT_ID).build().getService();
        BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();
        storage.create(blobInfo, Files.readAllBytes(filePath));
        //file.delete();
//        Blob blob = storage.create(blobInfo, Files.readAllBytes(filePath));
        return String.format(DOWNLOAD_URL, URLEncoder.encode(objectName));
    }

    public String getFileUrlCertificate(MultipartFile multipartFile) throws IOException {

        FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
        File file = convertMultiPartToFile(multipartFile);
        Path filePath = file.toPath();
        Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId(FIREBASE_PROJECT_ID).build().getService();
        BlobId blobId = BlobId.of(FIREBASE_BUCKET, multipartFile.getName());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();
        storage.create(blobInfo, Files.readAllBytes(filePath));
        //file.delete();
//        Blob blob = storage.create(blobInfo, Files.readAllBytes(filePath));
        return String.format(DOWNLOAD_URL, URLEncoder.encode(multipartFile.getName()));
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
            // Upload the image
            Map params1 = ObjectUtils.asMap(
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true
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
    public void certificate(Integer testId) throws IOException, ParseException {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String fullName = jdbcTemplate.queryForObject("SELECT fullName FROM user WHERE username=?",String.class,userName);
        String courseName = jdbcTemplate.queryForObject("SELECT courseName FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", String.class, testId);
        Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", Integer.class, testId);
        String joinDate = jdbcTemplate.queryForObject("SELECT joinDate FROM enrollment WHERE userName = ? and courseId=?", String.class, userName, courseId);
        String completedDate = jdbcTemplate.queryForObject("SELECT completedDate FROM enrollment WHERE userName = ? and courseId=?", String.class, userName, courseId);
        String duration = jdbcTemplate.queryForObject("SELECT courseDuration FROM course WHERE courseId = ?", String.class, courseId);
//     File f = new File("src/main/resources/Final Certificate.png");
//        f.setReadable(true); //read
//        f.setWritable(true); //write
//       // f.setExecutable(true); //execute
        // System.out.println(System.getProperty("com/robosoft/VirtualLearn/FinalCertificate.png"));
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

        //*****************************************


        //BufferedImage  Convert to  ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        //ByteArrayOutputStream  Convert to  byte[]
        byte[] imageByte = out.toByteArray();
        // Will  byte[]  Convert to  MultipartFile
        MultipartFile multipartFile11 = new MultipartImage(imageByte, userName+courseId, "image", "png", imageByte.length);
        //System.out.println(multipartFile11.getName());
       // String url = getFileUrlCertificate(multipartFile11);
        String url = uploadProfilePhoto(multipartFile11);
        System.out.println(url);
        //**********************************************


        // ImageIO.write(image, "png", new File("src/main/resources/" + userName + courseId + ".png"));
        //File fileItem = new File("src/main/resources/" + userName + courseId + ".png");
        // FileInputStream input = new FileInputStream(fileItem);
        //MultipartFile multipartFile = new MockMultipartFile("fileItem", fileItem.getName(), "image/png", IOUtils.toByteArray(input));
        //String url = getFileUrl(multipartFile);
        //String pdfUrl = pdf(userName,courseId);
        //System.out.println(pdfUrl);
        jdbcTemplate.update("delete from certificate where userName='" + userName + "' and courseId=" + courseId);
        jdbcTemplate.update("INSERT INTO certificate(certificateNumber,courseId,UserName,certificateUrl) values(?,?,?,?)", certificateNumber, courseId, userName, url);
    }

    public void certificateWithoutTest(Integer courseId) throws IOException, ParseException {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String fullName = jdbcTemplate.queryForObject("SELECT fullName FROM user WHERE username=?",String.class,userName);
        String courseName = jdbcTemplate.queryForObject("SELECT courseName FROM course WHERE courseId=?", String.class,courseId);
        //Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", Integer.class, testId);
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
        //BufferedImage  Convert to  ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        //ByteArrayOutputStream  Convert to  byte[]
        byte[] imageByte = out.toByteArray();
        // Will  byte[]  Convert to  MultipartFile
        MultipartFile multipartFile11 = new MultipartImage(imageByte, userName+courseId, "image", "png", imageByte.length);
        //System.out.println(multipartFile11.getName());
        String url = uploadProfilePhoto(multipartFile11);
        System.out.println(url);
        //**********************************************


        // ImageIO.write(image, "png", new File("src/main/resources/" + userName + courseId + ".png"));
        //File fileItem = new File("src/main/resources/" + userName + courseId + ".png");
        // FileInputStream input = new FileInputStream(fileItem);
        //MultipartFile multipartFile = new MockMultipartFile("fileItem", fileItem.getName(), "image/png", IOUtils.toByteArray(input));
        //String url = getFileUrl(multipartFile);
        //String pdfUrl = pdf(userName,courseId);
        //System.out.println(pdfUrl);
        jdbcTemplate.update("delete from certificate where userName='" + userName + "' and courseId=" + courseId);
        jdbcTemplate.update("INSERT INTO certificate(certificateNumber,courseId,UserName,certificateUrl) values(?,?,?,?)", certificateNumber, courseId, userName, url);
    }


    public String pdf(String userName, Integer courseId) throws IOException {
        Path _dataDir = Paths.get("src/main/resources/CertificateData");
        Document document = new Document();
       // PageSize A4 = new PageSize(297, 420);
        Page page = document.getPages().add();
        page.setPageSize(597,520);
        Image image1 = new Image();
        // Load sample JPEG image file
        image1.setFile(Paths.get(_dataDir.toString(), userName+courseId+".png").toString());
        page.getParagraphs().add(image1);
        // Save output PDF document
        document.save(Paths.get(_dataDir.toString(),userName+courseId+".pdf").toString());
        File fileItem = new File("src/main/resources/CertificateData/"+userName+courseId+".pdf");
        FileInputStream input = new FileInputStream(fileItem);
        MultipartFile multipartFile = new MockMultipartFile("fileItem", fileItem.getName(), "image/pdf", IOUtils.toByteArray(input));
        String url = getFileUrl(multipartFile);
        return url;
    }

    public String viewCertificate(Integer testId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer courseId = jdbcTemplate.queryForObject("SELECT courseId FROM course WHERE courseId=(SELECT courseId FROM chapter WHERE chapterId=(SELECT chapterId FROM test WHERE testId=?))", Integer.class, testId);
        return jdbcTemplate.queryForObject("SELECT certificateUrl FROm certificate WHERE userName=? and courseId=?", String.class, userName, courseId);
    }

    public String getPdfUrl(Integer courseId)
    {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return jdbcTemplate.queryForObject("SELECT pdfUrl FROM certificate WHERE userName=? and courseId=?",String.class,userName,courseId);
    }
}
