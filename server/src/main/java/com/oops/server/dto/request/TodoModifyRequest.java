package com.oops.server.dto.request;

import com.oops.server.dto.etc.TodoModifyTodoDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 일정(schedule) 전체 수정할 때 쓰이는 dto
public record TodoModifyRequest(
        LocalDate date,
        List<Long> deleteTodoIdx,
        List<TodoModifyTodoDto> modifyTodo,
        List<String> addTodoName,
        List<Integer> todoTag,
        LocalTime goOutTime,
        List<Integer> remindTime
) {}
