package com.oops.server.dto.response;

import com.oops.server.dto.etc.TodoInventoryDto;
import com.oops.server.dto.etc.StuffDto;
import com.oops.server.dto.etc.TodoTodoDto;
import java.time.LocalTime;
import java.util.List;

public record TodoGetOneResponse(
        List<TodoInventoryDto> inventoryList,
        List<TodoTodoDto> todoList,
        List<Integer> todoTagList,
        LocalTime goOutTime,
        List<Integer> remindTime,
        List<StuffDto> stuffList,
        Boolean isCompleteStuff
) {}
