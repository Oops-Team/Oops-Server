package com.oops.server.controller;

import com.oops.server.dto.request.TodoCreateRequest;
import com.oops.server.security.TokenProvider;
import com.oops.server.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/todo")
@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final TokenProvider tokenProvider;

    // 일정 추가
    @PostMapping("/create")
    public ResponseEntity create(@RequestHeader("xAuthToken") String token, @RequestBody TodoCreateRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);
        return scheduleService.create(userId, request);
    }
}
