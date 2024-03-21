package com.oops.server.repository;

import com.oops.server.compositekey.NoticeID;
import com.oops.server.entity.Notice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, NoticeID> {

    // 날짜 순으로 정렬하여 공지성 글만 가져오기
    @Query("SELECT n FROM Notice n WHERE n.type = 1 ORDER BY n.date DESC")
    List<Notice> getAllNoticeContent();

    // 가장 최근 공지 1개 가져오기
    @Query("SELECT n FROM Notice n WHERE n.type = 1 ORDER BY n.date DESC LIMIT 1")
    Notice getRecentNotice();

    // 특정 타입의 글들만 가져오기
    List<Notice> findAllByType(int type);
}
