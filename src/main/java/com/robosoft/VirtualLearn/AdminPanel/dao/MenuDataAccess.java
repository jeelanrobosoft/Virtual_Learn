package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.robosoft.VirtualLearn.AdminPanel.dto.MyProfileResponse;
import com.robosoft.VirtualLearn.AdminPanel.dto.NotificationResponse;
import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MenuDataAccess {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public SideBarResponse getDetails(String userName) {
        String query;
        String occupation;
        int status = jdbcTemplate.queryForObject("select count(*) from user where userName='" + userName + "'", Integer.class);
        if (status == 0)
            return null;
        query = "select profilePhoto,fullName,occupation from user where userName='" + userName + "'";
        SideBarResponse response = jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(SideBarResponse.class));
        Integer notificationCount = jdbcTemplate.queryForObject("select count(notificationId) from notification where userName='" + userName + "' and readStatus=false", Integer.class);
        response.setNotificationCount(notificationCount);
        return response;
    }

    public MyProfileResponse getMyProfile(SideBarResponse response, String userName) {
        int courseCompleted = jdbcTemplate.queryForObject("select count(*) from enrollment where completedDate>'0000-00-00' and userName='" + userName + "'", Integer.class);
        int chapterCompletedStatus = jdbcTemplate.queryForObject("select count(*) from chapterProgress where userName='" + userName + "' and chapterCompletedStatus=true", Integer.class);
        int testCompletedStatus = jdbcTemplate.queryForObject("select count(distinct(testId)) from userAnswer where userName='" + userName + "'", Integer.class);
        MyProfileResponse myProfileResponse = jdbcTemplate.queryForObject("select email,dateOfBirth,mobileNumber,userName,occupation,gender,twitterLink,faceBookLink from user where userName='" + userName + "'", (rs, rowNum) -> {
            MyProfileResponse profileResponse = new MyProfileResponse();
            profileResponse.setUserName(rs.getString("userName"));
            profileResponse.setMobileNumber(rs.getString("mobileNumber"));
            profileResponse.setEmail(rs.getString("email"));
            profileResponse.setDateOfBirth(rs.getString("dateOfBirth"));
            profileResponse.setOccupation(rs.getString("occupation"));
            profileResponse.setGender(rs.getString("gender"));
            profileResponse.setTwitterLink(rs.getString("twitterLink"));
            profileResponse.setFaceBookLink(rs.getString("faceBookLink"));
            return profileResponse;
        });
        System.out.println(response.getOccupation());
        myProfileResponse.setCourseCompleted(courseCompleted);
        myProfileResponse.setChaptersCompleted(chapterCompletedStatus);
        myProfileResponse.setTestsCompleted(testCompletedStatus);
        myProfileResponse.setProfilePhoto(response.getProfilePhoto());
        myProfileResponse.setOccupation(response.getOccupation());
        myProfileResponse.setFullName(response.getFullName());
        return myProfileResponse;
    }

    public List<NotificationResponse> getNotification(String userName) {
        List<NotificationResponse> notifications = jdbcTemplate.query("select * from notification where userName='" + userName + "'", new BeanPropertyRowMapper<>(NotificationResponse.class));
        Collections.reverse(notifications);
        return notifications;
    }

    public String readNotification(String userName, Integer notificationId) {

        Integer status = jdbcTemplate.queryForObject("select count(*) from notification where notificationId=" + notificationId,Integer.class);
        if(status == 0)
            return "Notification Not Exists";
        jdbcTemplate.update("update notification set readStatus=true where notificationId=" + notificationId + " and userName='" + userName + "'");
        return "Successfully";
    }
}
