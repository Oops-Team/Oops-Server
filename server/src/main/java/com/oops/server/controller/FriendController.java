package com.oops.server.controller;

import com.oops.server.security.TokenProvider;
import com.oops.server.service.FriendService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 사용자 리스트 조회
    @GetMapping("/search/{name}")
    public ResponseEntity getSearch(@RequestHeader("xAuthToken") String token,
                                    @PathVariable("name") String name) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return friendService.getSearch(userId, name);
    }

    // 친구 리스트 조회
    @GetMapping("")
    public ResponseEntity getAll(@RequestHeader("xAuthToken") String token) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return friendService.getAll(userId);
    }

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
        Long myId = tokenProvider.getUserIdFromToken(token);

        return friendService.accept(myId, friendIdMap.get("friendId"));
    }

    // 친구 삭제 & 거절
    @DeleteMapping("")
    public ResponseEntity delete(@RequestHeader("xAuthToken") String token,
            @RequestBody Map<String, Long> friendIdMap) {
        Long myId = tokenProvider.getUserIdFromToken(token);

        return friendService.delete(myId, friendIdMap.get("friendId"));
    }
}
