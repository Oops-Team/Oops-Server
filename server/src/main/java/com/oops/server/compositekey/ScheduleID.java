package com.oops.server.compositekey;

import com.oops.server.entity.User;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleID implements Serializable {

    private User user;
    private LocalDate date;
}
