package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.dto.etc.CustomUserDetails;
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

        if (user == null) {
            throw new UsernameNotFoundException(ExceptionMessages.NOT_FOUND_USER.get());
        }

        return new CustomUserDetails(String.valueOf(user.getUserId()));
    }
}
