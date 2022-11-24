package com.robosoft.VirtualLearn.AdminPanel.service;


import com.robosoft.VirtualLearn.AdminPanel.dao.DataAccessService;
import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserRegistration;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationServiceImpl implements RegistrationService{
    @Autowired
    DataAccessService dataAccessService;


    @Autowired
    JdbcTemplate jdbcTemplate;

    private final static String ACCOUNT_SID = "ACd7b80d5a6e82ec89f4be4cc8779fd230";
    private final static String AUTH_ID = "cedfefdd1b6d02c0051b9fc191f0f551";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_ID);
    }

    @Override
    public long sendOtp(MobileAuth mobileAuth, String twoFaCode) {
        Message.creator(new PhoneNumber(mobileAuth.getMobileNumber()), new PhoneNumber("+19896822968"),
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
        String query = "select count(*) from user where userName='" + registration.getUserName() + "'";
        if (jdbcTemplate.queryForObject(query, Integer.class) != 1) {
            if (jdbcTemplate.queryForObject("select count(*) from user where email='" + registration.getEmail() + "'", Integer.class) != 1) {
                jdbcTemplate.update("insert into user(mobileNumber,fullName,userName,email) values(?,?,?,?)", registration.getMobileNumber(), registration.getFullName(), registration.getUserName(), registration.getEmail());
                jdbcTemplate.update("insert into authenticate values(?,?,'ROLE_USER')", registration.getUserName(), new BCryptPasswordEncoder().encode(registration.getPassword()));
            } else
                return "Email Id already exists";
        } else
            return "User Name already exists";
        return null;
    }
}