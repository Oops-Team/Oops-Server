package com.oops.server.repository;

import com.oops.server.compositekey.DateStuffID;
import com.oops.server.entity.DateStuff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DateStuffRepository extends JpaRepository<DateStuff, DateStuffID> {
}
