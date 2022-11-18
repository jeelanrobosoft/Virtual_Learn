package com.robosoft.VirtualLearn.AdminPanel.entity;


import lombok.Data;

@Data
public class ChangePassword {
    private String currentPassword;
    private String newPassword;
}
