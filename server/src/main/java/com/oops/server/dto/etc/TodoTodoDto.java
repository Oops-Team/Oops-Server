package com.oops.server.dto.etc;

// 일정 1개 조회에서 사용하는 todoList dto
public record TodoTodoDto(
        Long todoIdx,
        String todoName,
        Boolean isComplete
) {}
