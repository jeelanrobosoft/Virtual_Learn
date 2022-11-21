package com.robosoft.VirtualLearn.AdminPanel.dao;


import com.robosoft.VirtualLearn.AdminPanel.entity.MobileAuth;
import com.robosoft.VirtualLearn.AdminPanel.entity.OtpVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class DataAccessService
{
    @Autowired
    JdbcTemplate jdbcTemplate;
    public long saveOtp(String mobileNumber, String twoFaCode)
    {
        String generatedTime = String.valueOf(System.currentTimeMillis()/1000);
        String expiryTime = String.valueOf((System.currentTimeMillis()/1000) + 120);
        String query = "insert into otpVerification values(?,?,?,?,true)";
        jdbcTemplate.update(query,mobileNumber,twoFaCode,generatedTime,expiryTime);
        return ((Long.parseLong(expiryTime)) / 60  ) - ((Long.parseLong(generatedTime)) / 60 );
    }
    public void deletePreviousOtp(String mobileNumber)
    {
        String query = "update otpVerification set status=false where mobileNumber='" + mobileNumber + "'  and status=true";
        jdbcTemplate.update(query);
    }
    public String verifyOtp(MobileAuth otp)
    {
        String query = "select otp,expiryTime from otpVerification where mobileNumber='" + otp.getMobileNumber() + "' and status=true";
        OtpVerification otpVerification = jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(OtpVerification.class));
        if(otpVerification.getOtp().equals(otp.getOneTimePassword()) && ((System.currentTimeMillis()/1000)<=Long.parseLong(otpVerification.getExpiryTime())))
        {
            jdbcTemplate.update("update otpVerification set status=false where mobileNumber='" + otp.getMobileNumber() + "' and status=true");
            return "Verified";
        }
        return "Verification Fail";
    }

    public int checkMobileNumber(String mobileNumber)
    {
        String query = "select count(mobileNumber) from user where mobileNumber='" + mobileNumber + "'";
        return jdbcTemplate.queryForObject(query, Integer.class);
    }

    public void resetPassword(MobileAuth auth)
    {
        String query = "update authenticate set password=? where userName=(select userName from user where mobileNumber='" + auth.getMobileNumber() + "')";
        jdbcTemplate.update(query,new BCryptPasswordEncoder().encode(auth.getOneTimePassword()));
    }
}
