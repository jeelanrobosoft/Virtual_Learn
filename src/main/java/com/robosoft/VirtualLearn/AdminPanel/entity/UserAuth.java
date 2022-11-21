package com.robosoft.VirtualLearn.AdminPanel.entity;


import lombok.Data;

@Data
public class UserAuth
{
    private String userName;
    private String password;
    private String role;
}
