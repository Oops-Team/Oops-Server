package com.oops.server.controller;

import com.oops.server.dto.request.InventoryCreateRequest;
import com.oops.server.service.InventoryService;
import com.oops.server.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class InventoryController {

    private final InventoryService inventoryService;
    private final TokenProvider tokenProvider;

    // 인벤토리 생성
    @PostMapping("inventories/create")
    public ResponseEntity create(@RequestHeader("xAuthToken") String token,
            @RequestBody InventoryCreateRequest request) {

        // 헤더에서 유저 정보 가져오기
        Long userId = tokenProvider.getUserIdFromToken(token);

        return inventoryService.create(userId, request);
    }
}
