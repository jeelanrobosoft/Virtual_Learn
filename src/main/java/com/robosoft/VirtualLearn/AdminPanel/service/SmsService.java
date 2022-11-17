package com.robosoft.VirtualLearn.AdminPanel.service;


import com.robosoft.VirtualLearn.AdminPanel.dao.DataAccessService;
import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Autowired
    DataAccessService dataAccessService;

    private final static String ACCOUNT_SID = "AC99ac200a70a89ed9899c95cfd965d006";
    private final static String AUTH_ID = "b44cc73110645a96475a69cb3fee58f7";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_ID);
    }
    public long sendOtp(MobileAuth mobileAuth, String twoFaCode) {
        Message.creator(new PhoneNumber(mobileAuth.getMobileNumber()), new PhoneNumber("+18304026893"),
                "Your Two Factor Authentication code is: " + twoFaCode).create();
        return dataAccessService.saveOtp(mobileAuth.getMobileNumber(),twoFaCode);
    }

    public void deletePreviousOtp(String mobileNumber) {
        dataAccessService.deletePreviousOtp(mobileNumber);
    }

    public String verifyOtp(MobileAuth otp) {
        return dataAccessService.verifyOtp(otp);
    }

    public int checkMobileNumber(MobileAuth mobileAuth) {
        return dataAccessService.checkMobileNumber(mobileAuth.getMobileNumber());
    }

    public void resetPassword(MobileAuth auth) {
        dataAccessService.resetPassword(auth);
    }
}
