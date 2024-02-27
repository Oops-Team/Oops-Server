package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.request.TodoCreateRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.entity.DateStuff;
import com.oops.server.entity.DateTodo;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryStuff;
import com.oops.server.entity.Schedule;
import com.oops.server.entity.Tag;
import com.oops.server.entity.User;
import com.oops.server.repository.DateStuffRepository;
import com.oops.server.repository.DateTodoRepository;
import com.oops.server.repository.InventoryRepository;
import com.oops.server.repository.InventoryTagRepository;
import com.oops.server.repository.ScheduleRepository;
import com.oops.server.repository.TagRepository;
import com.oops.server.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final ScheduleRepository scheduleRepository;
    private final DateTodoRepository dateTodoRepository;
    private final DateStuffRepository dateStuffRepository;
    private final InventoryRepository inventoryRepository;
    private final TagRepository tagRepository;
    private final InventoryTagRepository inventoryTagRepository;

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
}

