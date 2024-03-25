package com.oops.server.repository;

import com.oops.server.compositekey.FriendID;
import com.oops.server.entity.Friend;
import com.oops.server.entity.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRepository extends JpaRepository<Friend, FriendID> {

    // 친구 신청을 요청한 사람, 받은 사람으로 찾기
    Friend findByRequestUserAndResponseUser(User requestUser, User responseUser);

    // 해당 유저와 현재 친구 상태인 사용자들 불러오기
    @Query("SELECT f.responseUser FROM Friend f WHERE f.requestUser = :requestUser AND f.isFriend = true")
    List<User> getFriendList(@Param("requestUser") User requestUser);

    // 해당 유저가 보낸 친구 신청들 불러오기
    @Query("SELECT f.responseUser FROM Friend f WHERE f.requestUser = :requestUser AND f.isFriend = false")
    List<User> getSendFriendRequestList(@Param("requestUser") User requestUser);

    // 해당 유저가 받은 친구 신청들 불러오기
    @Query("SELECT f.requestUser FROM Friend f WHERE f.responseUser = :responseUser AND f.isFriend = false")
    List<User> getIncomingFriendRequestList(@Param("responseUser") User requestUser);

    // (요청인 기준 검색) 특정 사용자와 완전 친구 상태인 사람들 중에서 검색 결과 가져오기
    @Query("SELECT m FROM Friend f LEFT JOIN f.responseUser m WHERE f.requestUser = :requestUser"
            + " AND f.isFriend = true AND m.name LIKE :name")
    List<User> getSearchFriendList(@Param("requestUser") User requestUser, @Param("name") String name);

    // 해당 유저의 친구들 중, 외출 시간이 N분 남은 사람들 불러오기
    @Query("SELECT m "
            + "FROM Friend f JOIN f.responseUser m JOIN m.schedules s "
            + "WHERE f.requestUser = :requestUser AND f.isFriend = true"
            + " AND s.date = :date"
            + " AND (s.outTime BETWEEN :startTime AND :endTime)")
    List<User> getStingList(@Param("requestUser") User requestUser, @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}
