package com.oops.server.repository;

import com.oops.server.compositekey.NoticeID;
import com.oops.server.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, NoticeID> {

}
