package com.oops.server.repository;

import com.oops.server.entity.DateTodo;
import com.oops.server.entity.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DateTodoRepository extends JpaRepository<DateTodo, Long> {

    // 해당 유저, 특정 일자의 완료 혹은 미완료된 할 일 모두 가져오기
    List<DateTodo> findAllByScheduleAndIsComplete(Schedule schedule, boolean isComplete);
}
