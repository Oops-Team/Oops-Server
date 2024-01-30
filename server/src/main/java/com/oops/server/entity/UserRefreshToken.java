//package com.oops.server.entity;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.MapsId;
//import jakarta.persistence.OneToOne;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Getter
//@Entity
//public class UserRefreshToken {
//
//    @Id
//    private Long userId;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @MapsId
//    @JoinColumn(name = "id")
//    private User user;
//
//    private String refreshToken;
//
//    public UserRefreshToken(User user, String refreshToken) {
//        this.user = user;
//        this.refreshToken = refreshToken;
//    }
//
//    public void updateRefreshToken(String refreshToken) {
//        this.refreshToken = refreshToken;
//    }
//
//    public boolean validateRefreshToken(String refreshToken) {
//        return this.refreshToken.equals(refreshToken);
//    }
//}
