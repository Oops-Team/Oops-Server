package com.oops.server.repository;

import com.oops.server.compositekey.DateStuffID;
import com.oops.server.entity.DateStuff;
import com.oops.server.entity.Schedule;
import com.oops.server.entity.Stuff;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DateStuffRepository extends JpaRepository<DateStuff, DateStuffID> {

    // 해당 유저, 특정 일자의 모든 소지품 리스트 가져오기
    List<DateStuff> findAllByScheduleOrderByStuffStuffRank(Schedule schedule);

    // 해당 유저, 특정 일자의 모든 소지품 삭제
    @Transactional
    void deleteAllBySchedule(Schedule schedule);

    // 해당 유저, 특정 일자의 특정 소지품 삭제 (소지품 챙김)
    @Transactional
    void deleteByScheduleAndStuff(Schedule schedule, Stuff stuff);
}
