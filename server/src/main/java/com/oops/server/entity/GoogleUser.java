package com.oops.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "google_user")
public class GoogleUser {
    @Id
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "sns_id", length = 45, nullable = false)
    private String id;

    @Column(length = 100)
    private String token;
}
