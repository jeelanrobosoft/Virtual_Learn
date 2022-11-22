package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.robosoft.VirtualLearn.AdminPanel.dto.MyProfileResponse;
import com.robosoft.VirtualLearn.AdminPanel.dto.NotificationResponse;
import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class MenuDataAccess
{
    @Autowired
    JdbcTemplate jdbcTemplate;
    public SideBarResponse getDetails(String userName) {
        String query;
        String occupation;
        int status = jdbcTemplate.queryForObject("select count(*) from user where userName='" + userName + "'", Integer.class);
        if(status == 0)
            return null;
        try{
            occupation = jdbcTemplate.queryForObject("select subCategoryName from subCategory where subCategoryId=(select occupation from user where userName='" + userName + "')", String.class);
        } catch (Exception e){
            query = "select profilePhoto,fullName from user where userName='" + userName + "'";
            return jdbcTemplate.queryForObject(query,new BeanPropertyRowMapper<>(SideBarResponse.class));
        }
        query = "select profilePhoto,fullName from user where userName='" + userName+ "'";
        SideBarResponse response =  jdbcTemplate.queryForObject(query,new BeanPropertyRowMapper<>(SideBarResponse.class));
        response.setOccupation(occupation);
        return response;
    }
    public MyProfileResponse getMyProfile(SideBarResponse response, String userName) {
        int courseCompleted = jdbcTemplate.queryForObject("select count(*) from enrollment where completedDate>'0000-00-00' and userName='" + userName + "'", Integer.class);
        int chapterCompletedStatus = jdbcTemplate.queryForObject("select count(*) from chapterProgress where userName='" + userName + "' and chapterCompletedStatus=true", Integer.class);
        int testCompletedStatus = jdbcTemplate.queryForObject("select count(distinct(testId)) from userAnswer where userName='" + userName + "'" , Integer.class);
        MyProfileResponse myProfileResponse = jdbcTemplate.queryForObject("select email,dateOfBirth,mobileNumber,userName,occupation from user where userName='" + userName + "'",(rs,rowNum) -> {
            MyProfileResponse profileResponse = new MyProfileResponse();
            profileResponse.setUserName(rs.getString("userName"));
            profileResponse.setMobileNumber(rs.getString("mobileNumber"));
            profileResponse.setEmail(rs.getString("email"));
            profileResponse.setDateOfBirth(rs.getDate("dateOfBirth"));
            profileResponse.setOccupation(String.valueOf(rs.getInt("occupation")));
            return profileResponse;
        });
        myProfileResponse.setCourseCompleted(courseCompleted);
        myProfileResponse.setChaptersCompleted(chapterCompletedStatus);
        myProfileResponse.setTestsCompleted(testCompletedStatus);
        myProfileResponse.setProfilePhoto(response.getProfilePhoto());
        myProfileResponse.setOccupation(response.getOccupation());
        myProfileResponse.setFullName(response.getFullName());
        return myProfileResponse;
    }

    public List<NotificationResponse> getNotification(String userName) {
        List<NotificationResponse> notifications =  jdbcTemplate.query("select * from notification where userName='" + userName + "'",new BeanPropertyRowMapper<>(NotificationResponse.class));
        Collections.reverse(notifications);
        return notifications;
    }

    public String readNotification(String userName,Integer notificationId) {
        jdbcTemplate.update("update notification set readStatus=true where notificationId=" + notificationId +" and userName='" + userName + "'");
        return "Successfully";
    }
}
