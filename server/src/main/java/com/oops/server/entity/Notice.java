package com.oops.server.entity;

import com.oops.server.compositekey.NoticeID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Getter
@IdClass(NoticeID.class)
@Table(name = "notice")
public class Notice {

    @Id
    @Column(length = 100)
    private String title;

    @Id
    private LocalDate date;

    @Column(length = 1000)
    private String content;

    private int type;
}
