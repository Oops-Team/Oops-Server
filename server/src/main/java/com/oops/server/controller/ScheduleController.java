package com.oops.server.controller;

import com.oops.server.dto.request.StuffGetAllRequest;
import com.oops.server.dto.request.StuffTakeRequest;
import com.oops.server.dto.request.TodoCreateRequest;
import com.oops.server.dto.request.TodoInventoryModifyRequest;
import com.oops.server.dto.request.TodoModifyRequest;
import com.oops.server.dto.request.TodoStuffAddRequest;
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
    @GetMapping("/{date}")
    public ResponseEntity getMonth(@RequestHeader("xAuthToken") String token,
                                   @PathVariable("date") LocalDate date) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.getMonth(userId, date);
    }

    // 일정 1개 조회
    @GetMapping("/detail/{date}")
    public ResponseEntity getDetail(@RequestHeader("xAuthToken") String token,
                                    @PathVariable("date") LocalDate date) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.getDetail(userId, date);
    }

    // 일정 추가
    @PostMapping("/create")
    public ResponseEntity create(@RequestHeader("xAuthToken") String token,
            @RequestBody TodoCreateRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.create(userId, request);
    }

    // 일정 전체 수정
    @PatchMapping("")
    public ResponseEntity modifyAll(@RequestHeader("xAuthToken") String token,
            @RequestBody TodoModifyRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.modifyAll(userId, request);
    }

    // 일정 전체 삭제
    @DeleteMapping("")
    public ResponseEntity deleteAll(@RequestHeader("xAuthToken") String token,
            @RequestBody Map<String, LocalDate> dateMap) {
        Long userId = tokenProvider.getUserIdFromToken(token);
        LocalDate date = dateMap.get("date");

        return scheduleService.deleteAll(userId, date);
    }

    // 챙겨야 할 것 수정(다른 인벤토리로 선택 및 변경)
    @PatchMapping("/inventories/select")
    public ResponseEntity modifyInventory(@RequestHeader("xAuthToken") String token,
            @RequestBody TodoInventoryModifyRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.modifyInventory(userId, request);
    }

    // 챙겨야 할 것 수정(소지품 수정 -> 소지품 목록 조회)
    @PostMapping("/stuff")
    public ResponseEntity getAllStuff(@RequestHeader("xAuthToken") String token,
            @RequestBody StuffGetAllRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.getAllStuff(userId, request);
    }

    // 챙겨야 할 것 수정(소지품 수정)
    @PatchMapping("/stuff")
    public ResponseEntity modifyStuff(@RequestHeader("xAuthToken") String token,
            @RequestBody TodoStuffAddRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.modifyStuff(userId, request);
    }

    // 챙겨야 할 것 수정(소지품 삭제)
    @DeleteMapping("/stuff")
    public ResponseEntity deleteStuff(@RequestHeader("xAuthToken") String token,
            @RequestBody StuffTakeRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.deleteStuff(userId, request);
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
    public ResponseEntity takeStuff(@RequestHeader("xAuthToken") String token,
            @RequestBody StuffTakeRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return scheduleService.takeStuff(userId, request);
    }

    // (Home) 할 일 완료/미완료 체크
    @PatchMapping("/home/{todoIdx}/check")
    public ResponseEntity checkTodo(@PathVariable("todoIdx") Long todoId,
            @RequestBody Map<String, Boolean> isCompleteMap) {
        boolean isComplete = isCompleteMap.get("isTodoComplete");

        return scheduleService.checkTodo(todoId, isComplete);
    }
}
