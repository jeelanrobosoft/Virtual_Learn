package com.robosoft.VirtualLearn.AdminPanel.service;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.robosoft.VirtualLearn.AdminPanel.dao.ProfileDao;
import com.robosoft.VirtualLearn.AdminPanel.entity.ChangePassword;
import com.robosoft.VirtualLearn.AdminPanel.entity.SaveProfile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.*;


@Service
public class ProfileService {
    @Autowired
    ProfileDao profileDao;

    @Autowired
    FinalTestService finalTestService;



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
        Blob blob = storage.create(blobInfo, Files.readAllBytes(filePath));
        return String.format(DOWNLOAD_URL, URLEncoder.encode(objectName));
    }

    public String saveMyProfile(SaveProfile saveProfile, String userName) throws IOException, ParseException {
        String profilePhotoLink = null;
        String finalDateOfBirth = null;
        String twitterLink = null;
        String faceBookLink = null;
        String gender = null;
        Integer number = checkStringContainsNumberOrNot(saveProfile.getOccupation());
        if (number == 1)
            return "Invalid Occupation";

        try {
            if (saveProfile.getGender().toLowerCase(Locale.ROOT).equals("male")
                    || saveProfile.getGender().toLowerCase(Locale.ROOT).equals("female") || saveProfile.getGender().toLowerCase(Locale.ROOT).equals("prefer not to say") ||
                    saveProfile.getGender() == null) {
                gender = saveProfile.getGender();
            } else {
                return "Invalid gender";
            }
        } catch (Exception e) {
        }
        try {
            if ((saveProfile.getTwitterLink().toLowerCase(Locale.ROOT).contains("twitter") ||
                    saveProfile.getTwitterLink() == null || saveProfile.getTwitterLink().toLowerCase(Locale.ROOT).equals("empty"))) {
                twitterLink = saveProfile.getTwitterLink();
            } else
                return "Invalid facebook or twitter link";
        } catch (Exception ex) {
        }
        try {
            if (saveProfile.getFaceBookLink().toLowerCase(Locale.ROOT).contains("facebook") || saveProfile.getFaceBookLink() == null
                    || saveProfile.getFaceBookLink().equals("empty"))
                faceBookLink = saveProfile.getFaceBookLink();
            else
                return "Invalid facebook or twitter link";

        } catch (Exception faceBook) {
        }
        try {
            saveProfile.getProfilePhoto().isEmpty();
            //profilePhotoLink = getFileUrl(saveProfile.getProfilePhoto());
            profilePhotoLink = finalTestService.uploadProfilePhoto(saveProfile.getProfilePhoto());
        } catch (NullPointerException exp) {
        }
        try {
            if(saveProfile.getDateOfBirth().equals("empty"))
            {
                finalDateOfBirth = "empty";
            }
            else {
            Date dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(saveProfile.getDateOfBirth());
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
            finalDateOfBirth = newFormat.format(dateOfBirth);
            }
        } catch (Exception e) {
        }
        profileDao.saveProfile(saveProfile, twitterLink, faceBookLink, gender, profilePhotoLink, finalDateOfBirth, userName);
        return null;
    }


    public String changePassword(ChangePassword password) {
        if (password.getNewPassword().length() >= 5)
            return profileDao.changePassword(password);
        else
            return "Invalid Password";
    }

    public int checkStringContainsNumberOrNot(String s) {
        if (s != null) {
            char[] chars = s.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char c : chars) {
                if (Character.isDigit(c)) {
                    return 1;
                }
            }
            return 0;
        }
        return 0;
    }

}

