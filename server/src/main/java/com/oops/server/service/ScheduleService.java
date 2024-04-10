package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.etc.TodoInventoryDto;
import com.oops.server.dto.etc.StuffDto;
import com.oops.server.dto.etc.TodoModifyTodoDto;
import com.oops.server.dto.etc.TodoTodoDto;
import com.oops.server.dto.request.StuffGetAllRequest;
import com.oops.server.dto.request.StuffTakeRequest;
import com.oops.server.dto.request.TodoCreateRequest;
import com.oops.server.dto.request.TodoInventoryModifyRequest;
import com.oops.server.dto.request.TodoModifyRequest;
import com.oops.server.dto.request.TodoStuffAddRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.dto.response.StuffGetAllResponse;
import com.oops.server.dto.response.TodoGetAllResponse;
import com.oops.server.dto.response.TodoGetOneResponse;
import com.oops.server.dto.response.TodoInventoryModifyResponse;
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
import com.oops.server.repository.InventoryStuffRepository;
import com.oops.server.repository.InventoryTagRepository;
import com.oops.server.repository.ScheduleRepository;
import com.oops.server.repository.StuffRepository;
import com.oops.server.repository.TagRepository;
import com.oops.server.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    private final InventoryStuffRepository inventoryStuffRepository;
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

            // 오늘 할 일 자체가 없을 경우
            if (dateTodoList.size() == 0) {
                isComplete = false;
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

        // 만약 해당 날짜의 스케줄이 없다면
        if (schedule == null) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK, ExceptionMessages.NOT_FOUND_SCHEDULE.get()),
                    HttpStatus.OK);
        }

        // 0-1. 챙겨야 할 것 관련 정보 먼저 가져오기
        List<DateStuff> dateStuffList = dateStuffRepository.findAllByScheduleOrderByStuffStuffRank(schedule);

        // todoTag string -> int 값으로 분리
        int[] todoTagIntArr = Arrays.stream(schedule.getTagList().split(",")).mapToInt(Integer::parseInt).toArray();
        List<Integer> todoTagList = Arrays.stream(todoTagIntArr).boxed().toList();

        // 해당 일정에 추천 인벤토리가 배치되지 않았고,
        // 해당 일정에 배치된 소지품도 아예 없는 상태라면
        if (schedule.getInventory() == null && dateStuffList.size() == 0) {
            // 인벤토리 배정 및 반영
            Inventory matchingResultInv = matchingInventory(user, todoTagList);

            // 새로 배치할 수 있는 인벤토리가 나왔다면
            if (matchingResultInv != null) {
                // 해당 일정에 인벤토리 배치
                schedule.modifyInventory(matchingResultInv);
                schedule = scheduleRepository.save(schedule);

                // 해당 인벤토리의 소지품으로 해당 일정 소지품에 배치
                List<InventoryStuff> inventoryStuffList = schedule.getInventory().getInventoryStuffs();
                for (InventoryStuff inventoryStuff : inventoryStuffList) {
                    dateStuffList.add(
                            dateStuffRepository.save(
                                    DateStuff.create(schedule, inventoryStuff.getStuff())
                            )
                    );
                }
            }
        }

        // 0-2. 챙겨야 할 것 관련 정보 담기
        List<StuffDto> stuffList = new ArrayList<>();
        for (DateStuff dateStuff : dateStuffList) {
            stuffList.add(new StuffDto(
                    dateStuff.getStuff().getImgUrl(),
                    dateStuff.getStuff().getName()
            ));
        }

        // 1. 인벤토리 관련 정보 담기
        List<TodoInventoryDto> inventoryList = new ArrayList<>();
        // 해당 일정에서 사용하고 있는 인벤토리 가져오기
        Inventory usedInventory = schedule.getInventory();
        if (usedInventory != null) {
            // 해당 일정에서 사용하고 있는 인벤토리 먼저 담기
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

        // 6. 소지품 전부 챙김 여부 담기
        Boolean isCompleteStuff = false;
        // 불러올 소지품 리스트가 없는데, 인벤토리는 배치된 상태일 경우
        if (stuffList.size() == 0 && usedInventory != null) {
            // 다 챙김 표시
            isCompleteStuff = true;
        }

        // 7. 응답
        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", new TodoGetOneResponse(
                        inventoryList,
                        todoList,
                        todoTagList,
                        goOutTime,
                        remindTime,
                        stuffList,
                        isCompleteStuff)),
                HttpStatus.OK);
    }

    // 일정 추가
    public ResponseEntity create(Long userId, TodoCreateRequest request) {
        User user = userRepository.findByUserId(userId);

        // 1. schedule 테이블에 해당 일정 정보 넣기
        LocalDate date = request.date();
        LocalTime outTime = request.goOutTime();

        String tagList = "";
        request.todoTag().sort(Comparator.naturalOrder());  // 태그 아이디값 오름차순 정렬
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
                    DefaultResponse.from(StatusCode.OK, ExceptionMessages.NOT_CREATE_INVENTORY.get()),
                    HttpStatus.OK);
        }
        schedule.modifyInventory(inventory);
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

    // 일정 전체 수정
    public ResponseEntity modifyAll(Long userId, TodoModifyRequest request) {
        User user = userRepository.findByUserId(userId);
        Schedule schedule = scheduleRepository.findByUserAndDate(user, request.date());

        // 1. 삭제한 할 일들 반영
        if (request.deleteTodoIdx() != null) {
            for (Long todoId : request.deleteTodoIdx()) {
                dateTodoRepository.deleteByTodoId(todoId);
            }
        }

        // 2. 수정한 할 일들 반영
        if (request.modifyTodo() != null) {
            for (TodoModifyTodoDto todoDto : request.modifyTodo()) {
                DateTodo dateTodo = dateTodoRepository.findByTodoId(todoDto.todoIdx());
                dateTodo.modifyContent(todoDto.todoName());
                dateTodoRepository.save(dateTodo);
            }
        }

        // 3. 추가한 할 일들 반영
        if (request.addTodoName() != null) {
            for (String todoName : request.addTodoName()) {
                dateTodoRepository.save(
                        DateTodo.create(schedule, todoName)
                );
            }
        }

        // 4. 태그 변경 (기존 태그와 다를 경우 인벤토리 재매칭)
        String oldTag = schedule.getTagList();  // 일정 수정 전 태그들
        String newTag = "";
        request.todoTag().sort(Comparator.naturalOrder());  // 오름차순 정렬
        for (Integer tagId : request.todoTag()) {
            newTag += tagId.toString() + ",";
        }
        // 만일 기존 태그와 다르다면
        boolean isExistInventory = true;
        if (!oldTag.equals(newTag)) {
            // schedule 태그 값 변경 적용
            schedule.modifyTagList(newTag);

            // 추천 인벤토리 재매칭
            Inventory inventory = matchingInventory(user, request.todoTag());
            // 인벤토리가 있는 경우
            if (inventory != null) {
                // 추천 인벤토리 변경
                schedule.modifyInventory(inventory);

                // 해당 일정의 추천 소지품 목록 변경
                List<InventoryStuff> inventoryStuffList = inventory.getInventoryStuffs();
                dateStuffRepository.deleteAllBySchedule(schedule);
                for (InventoryStuff inventoryStuff : inventoryStuffList) {
                    dateStuffRepository.save(
                            DateStuff.create(schedule, inventoryStuff.getStuff())
                    );
                }
            }
            // 인벤토리가 아예 없는 경우
            else {
                isExistInventory = false;
            }
        }

        // 5. 외출 시간 변경
        schedule.modifyOutTime(request.goOutTime());

        // 6. 알림 시간 변경
        String remindTimeStr = "";
        for (Integer remindTime : request.remindTime()) {
            remindTimeStr += remindTime.toString() + ",";
        }
        schedule.modifyNotification(remindTimeStr);

        // 7. schedule 모든 변경사항 DB에 적용
        scheduleRepository.save(schedule);

        // 8. 응답 전송
        // 정상 응답 (추천 인벤토리까지 배치한 상태)
        if (isExistInventory) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK, "성공"),
                    HttpStatus.OK);
        }
        // 현재 생성된 인벤토리가 없는 경우
        else {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK, ExceptionMessages.NOT_CREATE_INVENTORY.get()),
                    HttpStatus.OK);
        }
    }

    // 일정 전체 삭제
    public ResponseEntity deleteAll(Long userId, LocalDate date) {
        User user = userRepository.findByUserId(userId);
        scheduleRepository.deleteByUserAndDate(user, date);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 해당 일정(schedule)의 인벤토리 변경
    public ResponseEntity modifyInventory(Long userId, TodoInventoryModifyRequest request) {
        User user = userRepository.findByUserId(userId);
        Schedule schedule = scheduleRepository.findByUserAndDate(user, request.date());
        Inventory inventory = inventoryRepository.findByUserAndName(user, request.inventoryName());

        // 인벤토리 변경
        schedule.modifyInventory(inventory);
        schedule = scheduleRepository.save(schedule);

        // 해당 일정의 소지품 교체(일괄 삭제 후 추가)
        dateStuffRepository.deleteAllBySchedule(schedule);
        List<InventoryStuff> inventoryStuffList = inventory.getInventoryStuffs();
        List<StuffDto> stuffList = new ArrayList<>();   // 응답으로 보낼 객체 리스트
        for (InventoryStuff inventoryStuff : inventoryStuffList) {
            dateStuffRepository.save(new DateStuff(schedule, inventoryStuff.getStuff()));
            stuffList.add(new StuffDto(inventoryStuff.getStuff().getImgUrl(), inventoryStuff.getStuff().getName()));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공",
                        new TodoInventoryModifyResponse(inventory.getInventoryId(), stuffList)),
                HttpStatus.OK);
    }

    // 소지품 전체 목록 불러오기
    public ResponseEntity getAllStuff(Long userId, StuffGetAllRequest request) {
        // 만일 요청 데이터가 잘못됐을 경우
        if ((request.date() != null && request.inventoryId() != null) ||
                (request.date() == null && request.inventoryId() == null)) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.BAD_REQUEST, ExceptionMessages.BAD_REQUEST.get()),
                    HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUserId(userId);

        // 응답 객체
        List<StuffGetAllResponse> stuffList = new ArrayList<>();

        // 전체 소지품 목록 불러오기
        List<Stuff> stuffAllList = stuffRepository.findAllByOrderByStuffRank();

        // 현재 등록된 소지품들의 이름 목록 (비교 대상)
        List<String> stuffNameList = new ArrayList<>();

        // 해당 일정의 소지품 추가 -> 소지품 목록 조회인 경우
        if (request.date() != null) {
            // 해당 일정의 소지품 목록 불러오기
            Schedule schedule = scheduleRepository.findByUserAndDate(user, request.date());
            List<DateStuff> dateStuffList = schedule.getDateStuffs();

            // 현재 등록된 소지품 이름들 추가
            for (DateStuff dateStuff : dateStuffList) {
                stuffNameList.add(dateStuff.getStuff().getName());
            }
        }
        // 인벤토리 내 소지품 추가 및 수정 -> 소지품 목록 조회인 경우
        else if (request.inventoryId() != null) {
            // 해당 인벤토리의 소지품 목록 불러오기
            Inventory inventory = inventoryRepository.findByInventoryId(request.inventoryId());
            List<InventoryStuff> inventoryStuffList = inventory.getInventoryStuffs();

            // 현재 등록된 소지품 이름들 추가
            for (InventoryStuff inventoryStuff : inventoryStuffList) {
                stuffNameList.add(inventoryStuff.getStuff().getName());
            }
        }

        // DTO 정보 담기 수행
        for (Stuff stuff : stuffAllList) {
            boolean isSelected = false;

            // 현재 등록된 소지품이라면
            if (stuffNameList.contains(stuff.getName())) {
                isSelected = true;
            }

            stuffList.add(new StuffGetAllResponse(
                    stuff.getImgUrl(),
                    stuff.getName(),
                    isSelected));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", stuffList),
                HttpStatus.OK);
    }

    // 해당 날짜의 챙겨야 할 것 수정(소지품 수정)
    public ResponseEntity modifyStuff(Long userId, TodoStuffAddRequest request) {
        User user = userRepository.findByUserId(userId);
        Schedule schedule = scheduleRepository.findByUserAndDate(user, request.date());

        // 해당 날짜의 소지품 초기화
        dateStuffRepository.deleteAllBySchedule(schedule);

        // 소지품들을 인벤토리에도 같이 반영하는 것이라면
        if (request.isAddInventory()) {
            Inventory inventory = schedule.getInventory();
            // 해당 인벤토리의 소지품들도 초기화
            inventoryStuffRepository.deleteAllByInventory(inventory);

            for (String stuffName : request.stuffName()) {
                Stuff stuff = stuffRepository.findByName(stuffName);
                dateStuffRepository.save(new DateStuff(schedule, stuff));               // 해당 일정에 소지품 추가
                inventoryStuffRepository.save(new InventoryStuff(inventory, stuff));    // 인벤토리에도 소지품 추가
            }
        }
        // 해당 일정에만 일회성 추가하는 것이라면
        else {
            for (String stuffName : request.stuffName()) {
                Stuff stuff = stuffRepository.findByName(stuffName);
                dateStuffRepository.save(new DateStuff(schedule, stuff));   // 해당 일정에만 소지품 추가
            }
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 해당 날짜의 챙겨야 할 것 수정(소지품 삭제)
    public ResponseEntity deleteStuff(Long userId, StuffTakeRequest request) {
        // DB 내부 로직은 홈-소지품 챙김에서의 로직과 동일하므로 같은 메소드 사용함
        return takeStuff(userId, request);
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

