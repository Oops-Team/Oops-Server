package com.oops.server.repository;

import com.oops.server.compositekey.FriendID;
import com.oops.server.entity.Friend;
import com.oops.server.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, FriendID> {

    // 친구 신청을 요청한 사람, 받은 사람으로 찾기
    Friend findByRequestUserAndResponseUser(User requestUser, User responseUser);

    // 요청인의 아이디로 친구 목록 불러오기
    List<Friend> findAllByRequestUser(User requestUser);

    // 피요청인(신청을 받은 사람)의 아이디로 친구 목록 불러오기
    List<Friend> findAllByResponseUser(User responseUser);
}
