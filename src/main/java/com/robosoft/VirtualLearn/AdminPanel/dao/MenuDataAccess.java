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
    public SideBarResponse getDetails(SideBarRequest userName)
    {
        String query;
        String occupation;
        int status = jdbcTemplate.queryForObject("select count(*) from user where userName='" + userName.getUserName() + "'", Integer.class);
        if(status == 0)
            return null;
        try
        {
             occupation = jdbcTemplate.queryForObject("select subCategoryName from subCategory where subCategoryId=(select occupation from user where userName='" + userName.getUserName() + "')", String.class);
        }
        catch (Exception e)
        {
        query = "select profilePhoto,fullName from user where userName='" + userName.getUserName() + "'";
        return jdbcTemplate.queryForObject(query,new BeanPropertyRowMapper<>(SideBarResponse.class));
        }
        query = "select profilePhoto,fullName from user where userName='" + userName.getUserName()+ "'";
        SideBarResponse response =  jdbcTemplate.queryForObject(query,new BeanPropertyRowMapper<>(SideBarResponse.class));
        response.setOccupation(occupation);
        return response;
    }
    public MyProfileResponse getMyProfile(SideBarResponse response, SideBarRequest userName)
    {
        int courseCompleted = jdbcTemplate.queryForObject("select count(*) from enrollment where completedDate>'0000-00-00' and userName='" + userName.getUserName() + "'", Integer.class);
        int chapterCompletedStatus = jdbcTemplate.queryForObject("select count(*) from chapterProgress where userName='" + userName.getUserName() + "' and courseId=(select courseId from enrollment where userName='" + userName.getUserName() + "') and chapterId=(select chapterId from chapter where courseId=(select courseId from enrollment where userName='" + userName +"')) and chapterCompletedStatus=true", Integer.class);
        int testCompletedStatus = jdbcTemplate.queryForObject("select count(*) from chapterProgress where userName='" + userName.getUserName() + "' and courseId=(select courseId from enrollment where userName='" + userName.getUserName() + "') and chapterId=(select chapterId from chapter where courseId=(select courseId from enrollment where userName='"+userName +"')) and chapterTestPercentage>=0.00", Integer.class);
        MyProfileResponse myProfileResponse = jdbcTemplate.queryForObject("select email,dateOfBirth,mobileNumber,userName,occupation from user where userName='" + userName.getUserName() + "'",(rs,rowNum) -> {
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
    public List<NotificationResponse> getNotification(String userName)
    {
        List<NotificationResponse> notifications =  jdbcTemplate.query("select notificationId,description,timeStamp,notificationUrl from notification where userName='" + userName + "'",new BeanPropertyRowMapper<>(NotificationResponse.class));
        Collections.reverse(notifications);
        return notifications;
    }
}
