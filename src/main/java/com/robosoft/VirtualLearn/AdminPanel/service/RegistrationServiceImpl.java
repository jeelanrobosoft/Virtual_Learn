package com.robosoft.VirtualLearn.AdminPanel.service;


import com.robosoft.VirtualLearn.AdminPanel.dao.DataAccessService;
import com.robosoft.VirtualLearn.AdminPanel.entity.FcmToken;
import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.entity.PushNotificationResponse;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserRegistration;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.robosoft.VirtualLearn.AdminPanel.entity.PushNotification.sendPushNotification;

@Service
public class RegistrationServiceImpl implements RegistrationService{
    @Autowired
    DataAccessService dataAccessService;

    @Autowired
    ProfileService profileService;
    @Autowired
    JdbcTemplate jdbcTemplate;

//    private final static String ACCOUNT_SID = "ACd7b80d5a6e82ec89f4be4cc8779fd230";
//    private final static String AUTH_ID = "39e3d1b7d8afb09ba645714abbd184c6";
//    +19896822968

    /***
     * React JS
     */
    private final static String ACCOUNT_SID = "AC95acb85e7047ed4bf54677e6c560f01a";
    private final static String AUTH_ID = "790a885570499dbaeb7d7aae2c3a3696";
//    +17207131767



    static {
        Twilio.init(ACCOUNT_SID, AUTH_ID);
    }

    @Override
    public long sendOtp(MobileAuth mobileAuth, String twoFaCode) {
        Message.creator(new PhoneNumber("+918431913658"),new PhoneNumber("+17207131767"), /*new PhoneNumber("+17207131767"),*/
                "Your Two Factor Authentication code is: " + twoFaCode).create();
        return dataAccessService.saveOtp(mobileAuth.getMobileNumber(), twoFaCode);
    }

    @Override
    public void deletePreviousOtp(String mobileNumber) {
        dataAccessService.deletePreviousOtp(mobileNumber);
    }

    @Override
    public String verifyOtp(MobileAuth otp) {
        return dataAccessService.verifyOtp(otp);
    }

    @Override
    public int checkMobileNumber(MobileAuth mobileAuth) {
        return dataAccessService.checkMobileNumber(mobileAuth.getMobileNumber());
    }

    @Override
    public void resetPassword(MobileAuth auth) {
        dataAccessService.resetPassword(auth);
    }

    @Override
    public String addDetails(UserRegistration registration) {
        Integer status = jdbcTemplate.queryForObject("select count(*) from otpVerification where status=false and mobileNumber='" + registration.getMobileNumber() + "'", Integer.class);
        if(status == 0)
            return "Invalid Mobile Number";
        status = profileService.checkStringContainsNumberOrNot(registration.getFullName());
        if(status == 1)
            return "Full Name cannot contain digits";
        if( (registration.getFullName().length() >= 5) && registration.getUserName().length() >= 5
                && registration.getEmail().length() >= 10 && registration.getPassword().length() >= 5)
        {
        String query = "select count(*) from user where userName='" + registration.getUserName() + "'";
        if (jdbcTemplate.queryForObject(query, Integer.class) != 1) {
            if (jdbcTemplate.queryForObject("select count(*) from user where email='" + registration.getEmail() + "'", Integer.class) != 1) {
                jdbcTemplate.update("insert into user(mobileNumber,fullName,userName,email) values(?,?,?,?)", registration.getMobileNumber().trim(), registration.getFullName().trim(), registration.getUserName().trim(), registration.getEmail().trim());
                jdbcTemplate.update("insert into authenticate values(?,?,'ROLE_USER')", registration.getUserName(), new BCryptPasswordEncoder().encode(registration.getPassword().trim()));
            } else
                return "Email Id already exists";
        } else
            return "User Name already exists";
        return null;

        }
        else
            return "Invalid fullName or userName or Email or password";
    }

    public int checkForVerificationStatus(String mobileNumber){
        return dataAccessService.checkForVerificationStatus(mobileNumber);

    }


    public String storeFcmToken(FcmToken fcmToken) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String title = "Virtual Learn";
        String body = "Login Successfully";
        PushNotificationResponse notificationResponse = sendPushNotification(fcmToken.getFcmToken(),body,title);
        if(notificationResponse.getSuccess() == 1)
        {
        String query = "update user set fcmToken=? where userName=?";
        dataAccessService.storeFcmToken(query,fcmToken.getFcmToken(),userName);
        return "Ok..!";
        }
        else
            return "Invalid FCM Token";
    }
}
