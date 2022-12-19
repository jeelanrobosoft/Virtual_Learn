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
import static com.robosoft.VirtualLearn.AdminPanel.entity.PushNotification.sendPushNotification;

@Service
public class ProfileDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void saveProfile(SaveProfile saveProfile,String twitterLink,String faceBookLink,String gender, String profilePhotoLink, String finalDateOfBirth, String userName) {
        if(!(saveProfile.getOccupation() == null))
            jdbcTemplate.update("update user set occupation=? where userName=?",saveProfile.getOccupation(),userName);
        if(!(gender == null))
            jdbcTemplate.update("update user set gender=? where userName=?",gender,userName);
        if(twitterLink.equals("empty"))
            jdbcTemplate.update("update user set twitterLink=null where userName='" + userName + "'");
        else
            jdbcTemplate.update("update user set twitterLink=? where userName='" + userName +"'",twitterLink);
        if(faceBookLink.equals("empty"))
            jdbcTemplate.update("update user set faceBookLink=null where userName='" + userName + "'");
        else
            jdbcTemplate.update("update user set faceBookLink=? where userName='" + userName + "'",faceBookLink);
        if(!(profilePhotoLink == null))
            jdbcTemplate.update("update user set profilePhoto=? where userName=?",profilePhotoLink,userName);
        if(finalDateOfBirth.equals("empty"))
            jdbcTemplate.update("update user set dateOfBirth=null where userName='" + userName + "'");
        else
            jdbcTemplate.update("update user set dateOfBirth=? where userName='" + userName + "'",finalDateOfBirth);
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
            String fcmToken = jdbcTemplate.queryForObject("select fcmToken from user where userName='" + userName + "'", String.class);
            sendPushNotification(fcmToken,message,"Virtual Learn");
            return "Password Changed Successfully";
        } else {
            return "Reset Password Failed";
        }
    }
}
