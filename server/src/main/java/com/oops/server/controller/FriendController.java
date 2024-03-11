package com.oops.server.controller;

import com.oops.server.security.TokenProvider;
import com.oops.server.service.FriendService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/friends")
@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final TokenProvider tokenProvider;

    // 친구 신청
    @PostMapping("/request")
    public ResponseEntity request(@RequestHeader("xAuthToken") String token,
            @RequestBody Map<String, String> nameMap) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return friendService.request(userId, nameMap.get("name"));
    }

    // 친구 수락
    @PatchMapping("/accept")
    public ResponseEntity accept(@RequestHeader("xAuthToken") String token,
            @RequestBody Map<String, Long> friendIdMap) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return friendService.accept(userId, friendIdMap.get("friendId"));
    }
}
