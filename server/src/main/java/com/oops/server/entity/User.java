package com.oops.server.entity;

import com.oops.server.dto.request.SignUpRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "member")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 320, nullable = false)
    private String email;

    @Column
    private String password;

    /**
     * [sns type 표기 방식] - Oops 자체 회원가입: oops - Naver 연동 가입: naver - Google 연동 가입: google
     **/
    @Column(name = "sns_type", nullable = false)
    private String snsType;

    @Builder
    private User(String name, String email, String password, String snsType) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.snsType = snsType;
    }

    public static User create(SignUpRequest request, PasswordEncoder encoder, String snsType) {
        return User.builder()
                   .name(request.name())
                   .email(request.email())
                   .password(encoder.encode(request.password()))
                   .snsType(snsType)
                   .build();
    }

    public static User createSocial(SignUpRequest request, String snsType) {
        return User.builder()
                   .name(request.name())
                   .email(request.email())
                   .snsType(snsType)
                   .build();
    }
}
