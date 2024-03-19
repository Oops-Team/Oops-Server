package com.oops.server.repository;

import com.oops.server.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 특정 sns의 회원 찾기
    User findByEmailAndSnsType(String email, String snsType);

    // 닉네임으로 회원 찾기
    User findByName(String name);

    // 유저 고유 아이디값으로 해당 회원 찾기
    User findByUserId(Long userId);

    // 특정 사용자와 친구가 아닌 사용자들 중 검색한 닉네임과 부분 일치하는 회원 찾기
    @Query("SELECT u FROM User u " +
            "WHERE u.userId != :userId " +  // 현재 사용자 자신은 제외
            "AND u.name LIKE :name " +      // 입력된 닉네임과 일치하는 사용자 검색
            "AND NOT EXISTS (" +
            "SELECT f FROM Friend f " +
            "WHERE (f.requestUser.userId = :userId AND f.responseUser = u) " +
            "OR (f.responseUser.userId = :userId AND f.requestUser = u)" +
            ")")
    List<User> getSearchNotFriendList(@Param("userId") Long userId, @Param("name") String nickname);
}
