package com.robosoft.VirtualLearn.AdminPanel.service;


import com.robosoft.VirtualLearn.AdminPanel.entity.UserRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    @Autowired
    JdbcTemplate jdbcTemplate;

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
