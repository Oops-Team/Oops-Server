package com.oops.server.repository;

import com.oops.server.compositekey.DateTodoID;
import com.oops.server.entity.DateTodo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DateTodoRepository extends JpaRepository<DateTodo, DateTodoID> {
}
