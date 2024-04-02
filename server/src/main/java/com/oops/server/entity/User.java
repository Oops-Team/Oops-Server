package com.oops.server.entity;

import com.oops.server.dto.request.SignUpRequest;
import jakarta.persistence.*;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Getter
@Builder
@Table(name = "member")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 320, nullable = false)
    private String email;

    @Column
    private String password;

    /**
     * [sns type 표기 방식]
     * - Oops 자체 회원가입: oops
     * - Naver 연동 가입: naver
     * - Google 연동 가입: google
     **/
    @Column(name = "sns_type", nullable = false)
    private String snsType;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "profile_url", length = 500)
    private String profileUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "requestUser", cascade = CascadeType.REMOVE)
    private List<Friend> requestFriends;

    @OneToMany(mappedBy = "responseUser", cascade = CascadeType.REMOVE)
    private List<Friend> responseFriends;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private FcmToken fcmToken;

    public static User create(SignUpRequest request, PasswordEncoder encoder, String snsType) {
        return User.builder()
                .name(request.name())
                .email(request.email())
                .password(encoder.encode(request.password()))
                .snsType(snsType)
                .isPublic(true)
                .profileUrl("https://oops-bucket.s3.ap-northeast-2.amazonaws.com/defaultProfile.png")
                .build();
    }

    public static User createSocial(SignUpRequest request, String snsType) {
        return User.builder()
                .name(request.name())
                .email(request.email())
                .snsType(snsType)
                .isPublic(true)
                .profileUrl("https://oops-bucket.s3.ap-northeast-2.amazonaws.com/defaultProfile.png")
                .build();
    }

    public void modifyPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void modifyPassword(String password, PasswordEncoder encoder) {
        // 비밀번호 암호화 후 저장
        this.password = encoder.encode(password);
    }
}
