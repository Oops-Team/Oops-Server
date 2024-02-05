package com.oops.server.repository;

import com.oops.server.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    // Id 값으로 태그 찾기
    Tag findByTagId(Integer tagId);
}
