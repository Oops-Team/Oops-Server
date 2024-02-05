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

    @OneToMany(mappedBy = "user")
    private List<Inventory> inventory;

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
