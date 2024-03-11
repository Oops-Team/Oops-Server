package com.oops.server.compositekey;

import com.oops.server.entity.User;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendID implements Serializable {

    private User requestUser;
    private User responseUser;
}
