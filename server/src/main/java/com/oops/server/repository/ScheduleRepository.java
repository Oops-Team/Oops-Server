package com.oops.server.repository;

import com.oops.server.compositekey.ScheduleID;
import com.oops.server.entity.Schedule;
import com.oops.server.entity.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, ScheduleID> {

    // 해당 유저의 특정 일자 일정 찾기
    Schedule findByUserAndDate(User user, LocalDate date);

    // 해당 유저의 특정 기간 일정 모두 찾기
    List<Schedule> findAllByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
