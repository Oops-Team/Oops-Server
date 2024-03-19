package com.oops.server.dto.response;

import com.oops.server.dto.etc.FriendDto;
import java.util.List;

public record FriendGetSearchResponse(
        List<FriendDto> friendList,
        List<FriendDto> notFriendList
) {}
