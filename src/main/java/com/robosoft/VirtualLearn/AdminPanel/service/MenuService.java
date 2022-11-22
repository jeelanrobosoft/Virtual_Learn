package com.robosoft.VirtualLearn.AdminPanel.service;


import com.robosoft.VirtualLearn.AdminPanel.dao.MenuDataAccess;
import com.robosoft.VirtualLearn.AdminPanel.dto.MyProfileResponse;
import com.robosoft.VirtualLearn.AdminPanel.dto.NotificationResponse;
import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.SideBarResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService
{
    @Autowired
    MenuDataAccess dataAccess;
    public SideBarResponse getUserDetails(String userName) {
        return dataAccess.getDetails(userName);
    }

    public MyProfileResponse getMyProfile(SideBarResponse response, String userName) {
        return dataAccess.getMyProfile(response,userName);
    }

    public List<NotificationResponse> getNotification(String userName) {
        return dataAccess.getNotification(userName);
    }

    public String readNotification(String userName, Integer notificationId) {
        return dataAccess.readNotification(userName,notificationId);
    }
}
