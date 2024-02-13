package com.oops.server.service;

import com.oops.server.dto.CustomUserDetails;
import com.oops.server.entity.User;
import com.oops.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Long userId = Long.valueOf(username);
        User user = userRepository.findByUserId(userId);

        return new CustomUserDetails(String.valueOf(user.getUserId()));
    }
}
