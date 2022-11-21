package com.robosoft.VirtualLearn.AdminPanel.service;

import com.robosoft.VirtualLearn.AdminPanel.entity.UserAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
            try
            {
                System.out.println(username);
                UserAuth auth = jdbcTemplate.queryForObject("select * from authenticate where userName='" + username + "'",new BeanPropertyRowMapper<>(UserAuth.class));
                return new MyUserDetailsImpl(auth);
            }
            catch (Exception e)
            {
                throw new UsernameNotFoundException("User not found");
            }
        }
    }

