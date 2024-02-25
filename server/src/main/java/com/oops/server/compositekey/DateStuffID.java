package com.oops.server.compositekey;

import com.oops.server.entity.Schedule;
import com.oops.server.entity.Stuff;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateStuffID implements Serializable {

    private Schedule schedule;
    private Stuff stuff;
}
