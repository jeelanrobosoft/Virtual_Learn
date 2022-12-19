package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.entity.OtpVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.sql.PseudoColumnUsage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.robosoft.VirtualLearn.AdminPanel.common.Constants.DOWNLOAD_URL;
import static com.robosoft.VirtualLearn.AdminPanel.entity.PushNotification.sendPushNotification;

@Service
public class DataAccessService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public long saveOtp(String mobileNumber, String twoFaCode) {
        String generatedTime = String.valueOf(System.currentTimeMillis() / 1000);
        String expiryTime = String.valueOf((System.currentTimeMillis() / 1000) + 120);
        String query = "insert into otpVerification values(?,?,?,?,true)";
        jdbcTemplate.update(query, mobileNumber, twoFaCode, generatedTime, expiryTime);
        return ((Long.parseLong(expiryTime)) / 60) - ((Long.parseLong(generatedTime)) / 60);
    }

    public void deletePreviousOtp(String mobileNumber) {
        String query = "update otpVerification set status=false where mobileNumber='" + mobileNumber + "'  and status=true";
        jdbcTemplate.update(query);
    }

    public String verifyOtp(MobileAuth otp) {
        String query = "select otp,expiryTime from otpVerification where mobileNumber='" + otp.getMobileNumber() + "' and status=true";
        OtpVerification otpVerification = jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(OtpVerification.class));
        if (otpVerification.getOtp().equals(otp.getOneTimePassword()) && ((System.currentTimeMillis() / 1000) <= Long.parseLong(otpVerification.getExpiryTime()))) {
            jdbcTemplate.update("update otpVerification set status=false where mobileNumber='" + otp.getMobileNumber() + "' and status=true");
            return "Verified";
        }
        return "Verification Fail";
    }

    public int checkMobileNumber(String mobileNumber) {
        String query = "select count(mobileNumber) from user where mobileNumber='" + mobileNumber + "'";
        return jdbcTemplate.queryForObject(query, Integer.class);
    }

    public void resetPassword(MobileAuth auth) {
        String userName = jdbcTemplate.queryForObject("select userName from user where mobileNumber='" + auth.getMobileNumber() +"'", String.class);
        String query = "update authenticate set password=? where userName=(select userName from user where mobileNumber='" + auth.getMobileNumber() + "')";
        jdbcTemplate.update(query, new BCryptPasswordEncoder().encode(auth.getOneTimePassword()));
        String message = "Successfully changed your Password";
        String photoUrl = String.format(DOWNLOAD_URL, URLEncoder.encode("password_change_success.png"));
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formatDateTime = dateTime.format(format);
        jdbcTemplate.update("insert into notification(userName,description,timeStamp,notificationUrl) values(?,?,?,?)", userName, message, formatDateTime, photoUrl);
        String fcmToken = jdbcTemplate.queryForObject("select fcmToken from user where userName='" + userName + "'", String.class);
        sendPushNotification(fcmToken,message,"Virtual Learn");
    }

    public int checkForVerificationStatus(String mobileNumber) {
        return jdbcTemplate.queryForObject("select count(*) from otpVerification where status=true and mobileNumber='" + mobileNumber + "'", Integer.class);
    }

    public void storeFcmToken(String query, String fcmToken, String userName) {
        jdbcTemplate.update(query,fcmToken,userName);
    }
}
