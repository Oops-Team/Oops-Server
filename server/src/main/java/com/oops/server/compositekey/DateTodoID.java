package com.oops.server.compositekey;

import com.oops.server.entity.Schedule;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateTodoID implements Serializable {

    private Schedule schedule;
    private String content;
}
