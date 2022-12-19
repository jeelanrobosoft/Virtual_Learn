package com.robosoft.VirtualLearn.AdminPanel.service;

import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserRegistration;

public interface RegistrationService {

    public long sendOtp(MobileAuth mobileAuth, String twoFaCode);

    public void deletePreviousOtp(String mobileNumber);

    public String verifyOtp(MobileAuth otp);

    public int checkMobileNumber(MobileAuth mobileAuth);

    public String resetPassword(MobileAuth auth);

    public String addDetails(UserRegistration registration);
}
