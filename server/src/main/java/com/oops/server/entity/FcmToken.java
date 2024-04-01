package com.oops.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fcm_token")
public class FcmToken {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private String token;

    // FK로 들고 온 것을 그대로 PK로 설정하기 위함
    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    public static FcmToken create(User user, String token) {
        return FcmToken.builder()
                .user(user)
                .token(token)
                .build();
    }

    public void modifyToken(String token) {
        this.token = token;
    }
}
