package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.etc.TodoInventoryDto;
import com.oops.server.dto.etc.StuffDto;
import com.oops.server.dto.etc.TodoTodoDto;
import com.oops.server.dto.request.StuffTakeRequest;
import com.oops.server.dto.request.TodoCreateRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.dto.response.TodoGetAllResponse;
import com.oops.server.dto.response.TodoGetOneResponse;
import com.oops.server.entity.DateStuff;
import com.oops.server.entity.DateTodo;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryStuff;
import com.oops.server.entity.Schedule;
import com.oops.server.entity.Stuff;
import com.oops.server.entity.Tag;
import com.oops.server.entity.User;
import com.oops.server.repository.DateStuffRepository;
import com.oops.server.repository.DateTodoRepository;
import com.oops.server.repository.InventoryRepository;
import com.oops.server.repository.InventoryTagRepository;
import com.oops.server.repository.ScheduleRepository;
import com.oops.server.repository.StuffRepository;
import com.oops.server.repository.TagRepository;
import com.oops.server.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final UserRepository userRepository;
    private final StuffRepository stuffRepository;
    private final TagRepository tagRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTagRepository inventoryTagRepository;
    private final ScheduleRepository scheduleRepository;
    private final DateTodoRepository dateTodoRepository;
    private final DateStuffRepository dateStuffRepository;

    // 확인하고 싶은 일정의 태그 리스트 값을 넣으면
    // 일정-인벤토리 매칭 후
    // 알맞은 인벤토리를 반환하는 메소드
    public Inventory matchingInventory(User user, List<Integer> todoTagList) {
        int totalMatchCount = 0;
        Inventory resultInventory = null;
        List<Inventory> inventoryList = inventoryRepository.findAllByUser(user);

        // 매칭할 인벤토리 자체가 없다면
        if (inventoryList.size() == 0) {
            return null;
        }

        // 인벤토리 하나씩 비교
        for (Inventory inventory : inventoryList) {
            int tempMatchCount = 0;

            // 일정의 태그와 비교
            for (Integer todoTagId : todoTagList) {
                Tag tag = tagRepository.findByTagId(todoTagId);

                // 매칭되는 데이터가 있다면
                if (inventoryTagRepository.findByInventoryAndTag(inventory, tag) != null) {
                    tempMatchCount++;

                    // 현재 제일 일치하는 인벤토리라면
                    if (tempMatchCount > totalMatchCount) {
                        // 해당 인벤토리로 결정
                        totalMatchCount = tempMatchCount;
                        resultInventory = inventory;
                    }
                }
            }
        }

        // 만일 매칭된 인벤토리가 하나도 없다면
        if (resultInventory == null) {
            // 유저의 인벤토리 중 임의로 하나 선택
            resultInventory = inventoryList.get(0);
        }

        return resultInventory;
    }

    // 일정 전체 조회(1달 기준)
    public ResponseEntity getMonth(Long userId, LocalDate date) {
        User user = userRepository.findByUserId(userId);
        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());

        List<Schedule> scheduleList = scheduleRepository.findAllByUserAndDateBetween(user, startDate, endDate);
        List<TodoGetAllResponse> dateDto = new ArrayList<>();

        for (Schedule schedule : scheduleList) {
            List<DateTodo> dateTodoList = schedule.getDateTodo();
            boolean isComplete = true;

            for (DateTodo dateTodo : dateTodoList) {
                // 미완료한 일정이 있을 경우
                if (!dateTodo.isComplete()) {
                    isComplete = false;
                    break;
                }
            }

            dateDto.add(new TodoGetAllResponse(schedule.getDate(), isComplete));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", dateDto),
                HttpStatus.OK);
    }

    // 일정 1개 조회
    public ResponseEntity getDetail(Long userId, LocalDate date) {
        User user = userRepository.findByUserId(userId);
        Schedule schedule = scheduleRepository.findByUserAndDate(user, date);

        // todoTag string -> int 값으로 분리
        int[] todoTagIntArr = Arrays.stream(schedule.getTagList().split(",")).mapToInt(Integer::parseInt).toArray();
        List<Integer> todoTagList = Arrays.stream(todoTagIntArr).boxed().toList();

        // 해당 일정에 추천 인벤토리가 배치되지 않은 상태라면
        if (schedule.getInventory() == null) {
            // 인벤토리 배정 및 반영
            schedule.setInventory(matchingInventory(user, todoTagList));
            schedule = scheduleRepository.save(schedule);
        }

        // 1. 인벤토리 관련 정보 담기
        List<TodoInventoryDto> inventoryList = new ArrayList<>();
        // 해당 일정에서 사용하고 있는 인벤토리 먼저 담기
        Inventory usedInventory = schedule.getInventory();
        inventoryList.add(new TodoInventoryDto(
                usedInventory.getInventoryId(),
                usedInventory.getName(),
                usedInventory.getIcon(),
                true));
        // 그 외 인벤토리 담기
        List<Inventory> allInventoryList = inventoryRepository.findAllByUser(user);
        for (Inventory inventory : allInventoryList) {
            // 현재 사용 중인 인벤토리가 아니라면
            if (inventory.getInventoryId() != usedInventory.getInventoryId()) {
                inventoryList.add(new TodoInventoryDto(
                        inventory.getInventoryId(),
                        inventory.getName(),
                        inventory.getIcon(),
                        false
                ));
            }
        }

        // 2. 오늘 할 일 관련 정보 담기
        List<TodoTodoDto> todoList = new ArrayList<>();
        // 완료되지 않은 할 일 담기
        List<DateTodo> notCompleteTodoList = dateTodoRepository.findAllByScheduleAndIsComplete(schedule, false);
        for (DateTodo dateTodo : notCompleteTodoList) {
            todoList.add(new TodoTodoDto(
                    dateTodo.getTodoId(),
                    dateTodo.getContent(),
                    false
            ));
        }
        // 완료된 할 일 담기
        List<DateTodo> completeTodoList = dateTodoRepository.findAllByScheduleAndIsComplete(schedule, true);
        for (DateTodo dateTodo : completeTodoList) {
            todoList.add(new TodoTodoDto(
                    dateTodo.getTodoId(),
                    dateTodo.getContent(),
                    true
            ));
        }

        // 3. 관련 태그 목록 담기
        // 위 부분에서 만든 todoTagList 값으로 대체

        // 4. 외출 시간 담기
        LocalTime goOutTime = schedule.getOutTime();

        // 5. 알림 시간 담기
        int[] remindTimeIntArr = Arrays.stream(schedule.getNotification().split(",")).mapToInt(Integer::parseInt)
                .toArray();
        List<Integer> remindTime = Arrays.stream(remindTimeIntArr).boxed().toList();

        // 6. 챙겨야 할 것 관련 정보 담기
        List<DateStuff> dateStuffList = dateStuffRepository.findAllBySchedule(schedule);
        List<StuffDto> stuffList = new ArrayList<>();
        for (DateStuff dateStuff : dateStuffList) {
            stuffList.add(new StuffDto(
                    dateStuff.getStuff().getImg_url(),
                    dateStuff.getStuff().getName()
            ));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", new TodoGetOneResponse(
                        inventoryList,
                        todoList,
                        todoTagList,
                        goOutTime,
                        remindTime,
                        stuffList)),
                HttpStatus.OK);
    }

    // 일정 추가
    public ResponseEntity create(Long userId, TodoCreateRequest request) {
        User user = userRepository.findByUserId(userId);

        // 1. schedule 테이블에 해당 일정 정보 넣기
        LocalDate date = request.date();
        LocalTime outTime = request.goOutTime();

        String tagList = "";
        for (Integer todoTagId : request.todoTag()) {
            tagList += todoTagId.toString() + ",";
        }

        String notification = "";
        for (Integer remindTimeId : request.remindTime()) {
            notification += remindTimeId.toString() + ",";
        }

        Schedule schedule = scheduleRepository.save(
                Schedule.create(user, date, tagList, outTime, notification)
        );

        // 2. data_todo 테이블에 해당 일정의 할 일 넣기
        for (String todo : request.todoName()) {
            dateTodoRepository.save(
                    DateTodo.create(schedule, todo)
            );
        }

        // 3. 해당 일정과 연관된 인벤토리 지정
        Inventory inventory = matchingInventory(user, request.todoTag());
        // 만일 인벤토리가 없다면
        if (inventory == null) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK, ExceptionMessages.NOT_FOUND_INVENTORY.get()),
                    HttpStatus.OK);
        }
        schedule.setInventory(inventory);
        schedule = scheduleRepository.save(schedule);

        // 4. 해당 인벤토리의 소지품으로 date_stuff 테이블에 소지품들 넣기 (초기화)
        List<InventoryStuff> inventoryStuffList = inventory.getInventoryStuffs();
        for (InventoryStuff inventoryStuff : inventoryStuffList) {
            dateStuffRepository.save(
                    DateStuff.create(schedule, inventoryStuff.getStuff())
            );
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 일정 전체 삭제
    public ResponseEntity deleteAll(Long userId, LocalDate date) {
        User user = userRepository.findByUserId(userId);
        scheduleRepository.deleteByUserAndDate(user, date);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // (Home) 일정 1개 수정
    public ResponseEntity modifyOne(Long todoId, String todoName) {
        DateTodo dateTodo = dateTodoRepository.findByTodoId(todoId);
        dateTodo.modifyContent(todoName);
        dateTodoRepository.save(dateTodo);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // (Home) 일정 1개 삭제
    public ResponseEntity deleteOne(Long todoId) {
        dateTodoRepository.deleteByTodoId(todoId);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // (Home) 소지품 챙기기
    public ResponseEntity takeStuff(Long userId, StuffTakeRequest request) {
        User user = userRepository.findByUserId(userId);
        Schedule schedule = scheduleRepository.findByUserAndDate(user, request.date());
        Stuff stuff = stuffRepository.findByName(request.stuffName());

        // 해당 소지품 챙기기 (삭제)
        dateStuffRepository.deleteByScheduleAndStuff(schedule, stuff);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // (Home) 할 일 완료/미완료 체크 (변경)
    public ResponseEntity checkTodo(Long todoId, boolean isComplete) {
        DateTodo dateTodo = dateTodoRepository.findByTodoId(todoId);
        dateTodo.modifyIsComplete(isComplete);
        dateTodoRepository.save(dateTodo);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }
}

