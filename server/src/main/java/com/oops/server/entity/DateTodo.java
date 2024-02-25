package com.oops.server.entity;

import com.oops.server.compositekey.DateTodoID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(DateTodoID.class)
@Table(name = "date_todo")
public class DateTodo {

    @Id
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "date", referencedColumnName = "date"),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    private Schedule schedule;

    @Id
    @Column(length = 20)
    private String content;
}
