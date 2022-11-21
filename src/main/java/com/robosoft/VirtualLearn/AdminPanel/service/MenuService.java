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
    public SideBarResponse getUserDetails(SideBarRequest sideBar)
    {
        return dataAccess.getDetails(sideBar);
    }

    public MyProfileResponse getMyProfile(SideBarResponse response, SideBarRequest request)
    {
        return dataAccess.getMyProfile(response,request);
    }
    public List<NotificationResponse> getNotification(String userName)
    {
        return dataAccess.getNotification(userName);
    }
}
