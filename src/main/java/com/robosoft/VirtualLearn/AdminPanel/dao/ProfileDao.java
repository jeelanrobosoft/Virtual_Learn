package com.robosoft.VirtualLearn.AdminPanel.dao;

import com.robosoft.VirtualLearn.AdminPanel.entity.ChangePassword;
import com.robosoft.VirtualLearn.AdminPanel.entity.SaveProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.DOWNLOAD_URL;

@Service
public class ProfileDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void saveProfile(SaveProfile saveProfile, String profilePhotoLink, String finalDateOfBirth, String userName) {
        String query = "update user set dateOfBirth=?,profilePhoto=?,occupation=?,gender=?,twitterLink=?,faceBookLink=? where userName='" + userName + "'";
        jdbcTemplate.update(query, finalDateOfBirth, profilePhotoLink, saveProfile.getOccupation(), saveProfile.getGender(), saveProfile.getTwitterLink(), saveProfile.getFaceBookLink());
    }

    public String changePassword(ChangePassword password) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String query = "select password from authenticate where userName='" + userName + "'";
        String currentPassword = jdbcTemplate.queryForObject(query, String.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(password.getCurrentPassword(), currentPassword)) {
            query = "update authenticate set password=? where userName='" + userName + "'";
            jdbcTemplate.update(query, new BCryptPasswordEncoder().encode(password.getNewPassword()));
            String message = "Successfully changed your Password";
            String photoUrl = String.format(DOWNLOAD_URL, URLEncoder.encode("password_change_success.png"));
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formatDateTime = dateTime.format(format);
            jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, message, formatDateTime, photoUrl);
            return "Password Changed Successfully";
        } else {
            return "Reset Password Failed";
        }
    }
}
