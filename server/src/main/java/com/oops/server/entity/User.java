package com.oops.server.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "user")
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
     * [sns type 표기 방식]
     * - Oops 자체 회원가입: oops
     * - Naver 연동 가입: naver
     * - Google 연동 가입: google
     **/
    @Column(name = "sns_type", nullable = false)
    private String snsType;

    @OneToOne(mappedBy = "user")
    private NaverUser naverUser;

    @OneToOne(mappedBy = "user")
    private GoogleUser googleUser;

    @Builder(builderMethodName = "createOopsBuilder")
    public void createOopsUser(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.snsType = "oops";
    }
}
