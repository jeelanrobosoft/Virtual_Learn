package com.robosoft.VirtualLearn.AdminPanel.service;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.robosoft.VirtualLearn.AdminPanel.dao.ProfileDao;
import com.robosoft.VirtualLearn.AdminPanel.entity.ChangePassword;
import com.robosoft.VirtualLearn.AdminPanel.entity.SaveProfile;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Date;
import java.util.Objects;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.*;


@Service
public class ProfileService {
    @Autowired
    ProfileDao profileDao;

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

    public void saveMyProfile(SaveProfile saveProfile, String userName) throws IOException, ParseException {
        String profilePhotoLink = null;
        String finalDateOfBirth = null;
        if (saveProfile.getProfilePhoto() != null) {
            profilePhotoLink = getFileUrl(saveProfile.getProfilePhoto());
        }
        if (saveProfile.getDateOfBirth() != null) {
            Date dateOfBirth = new SimpleDateFormat("dd/MM/yyyy").parse(saveProfile.getDateOfBirth());
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
            finalDateOfBirth = newFormat.format(dateOfBirth);
        }
        profileDao.saveProfile(saveProfile, profilePhotoLink, finalDateOfBirth, userName);
    }

    public String changePassword(ChangePassword password) {
        return profileDao.changePassword(password);
    }
}

