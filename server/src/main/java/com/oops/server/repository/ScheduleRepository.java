package com.oops.server.repository;

import com.oops.server.compositekey.ScheduleID;
import com.oops.server.entity.Schedule;
import com.oops.server.entity.User;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, ScheduleID> {

    // 해당 유저의 특정 일자 일정 찾기
    Schedule findByUserAndDate(User user, LocalDate date);
}
