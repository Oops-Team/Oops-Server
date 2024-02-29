package com.oops.server.controller;

import com.oops.server.dto.request.StuffTakeRequest;
import com.oops.server.dto.request.TodoCreateRequest;
import com.oops.server.security.TokenProvider;
import com.oops.server.service.ScheduleService;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 일정 전체 조회(1달 기준)
    @GetMapping("")
    public ResponseEntity getMonth(@RequestHeader("xAuthToken") String token,
                                   @RequestBody Map<String, LocalDate> dateMap) {
        Long userId = tokenProvider.getUserIdFromToken(token);
        LocalDate date = dateMap.get("date");

        return scheduleService.getMonth(userId, date);
    }

    // 일정 1개 조회
    @GetMapping("/detail")
    public ResponseEntity getDetail(@RequestHeader("xAuthToken") String token,
                                    @RequestBody Map<String, LocalDate> dateMap) {
        Long userId = tokenProvider.getUserIdFromToken(token);
        LocalDate date = dateMap.get("date");

        return scheduleService.getDetail(userId, date);
    }

    // 일정 추가
    @PostMapping("/create")
    public ResponseEntity create(@RequestHeader("xAuthToken") String token, @RequestBody TodoCreateRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.create(userId, request);
    }

    // 일정 전체 삭제
    @DeleteMapping("")
    public ResponseEntity deleteAll(@RequestHeader("xAuthToken") String token,
                                    @RequestBody Map<String, LocalDate> dateMap) {
        Long userId = tokenProvider.getUserIdFromToken(token);
        LocalDate date = dateMap.get("date");

        return scheduleService.deleteAll(userId, date);
    }

    // (Home) 일정 1개 수정
    @PatchMapping("/home/{todoIdx}")
    public ResponseEntity modifyOne(@PathVariable("todoIdx") Long todoId,
                                    @RequestBody Map<String, String> todoNameMap) {
        String todoName = todoNameMap.get("todoName");

        return scheduleService.modifyOne(todoId, todoName);
    }

    // (Home) 일정 1개 삭제
    @DeleteMapping("/home/{todoIdx}")
    public ResponseEntity deleteOne(@PathVariable("todoIdx") Long todoId) {
        return scheduleService.deleteOne(todoId);
    }

    // (Home) 소지품 챙기기
    @DeleteMapping("/home/stuff")
    public ResponseEntity takeStuff(@RequestHeader("xAuthToken") String token, @RequestBody StuffTakeRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.takeStuff(userId, request);
    }
}
