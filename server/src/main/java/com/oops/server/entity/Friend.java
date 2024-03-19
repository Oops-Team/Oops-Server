package com.oops.server.entity;

import com.oops.server.compositekey.FriendID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@IdClass(FriendID.class)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "friend")
public class Friend {

    @Id
    @ManyToOne
    @JoinColumn(name = "request_id", referencedColumnName = "user_id")
    private User requestUser;

    @Id
    @ManyToOne
    @JoinColumn(name = "response_id", referencedColumnName = "user_id")
    private User responseUser;

    @Column(name = "is_friend", nullable = false)
    private boolean isFriend;

    // 친구 신청 시 삽입할 행
    public static Friend create(User requestUser, User responseUser) {
        return Friend.builder()
                .requestUser(requestUser)
                .responseUser(responseUser)
                .isFriend(false)
                .build();
    }

    // 서로 친구가 되었을 때(친구 수락을 했을 때)의 삽입할 역방향 행
    public static Friend createFriendState(User requestUser, User responseUser) {
        return Friend.builder()
                     .requestUser(requestUser)
                     .responseUser(responseUser)
                     .isFriend(true)
                     .build();
    }

    // 친구 신청을 수락할 때 사용
    public void acceptRequest() {
        this.isFriend = true;
    }
}
