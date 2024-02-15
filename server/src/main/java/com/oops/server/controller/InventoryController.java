package com.oops.server.controller;

import com.oops.server.dto.request.InventoryAddObjectRequest;
import com.oops.server.dto.request.InventoryCreateRequest;
import com.oops.server.service.InventoryService;
import com.oops.server.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @PostMapping("/inventories/create")
    public ResponseEntity create(@RequestHeader("xAuthToken") String token,
                                 @RequestBody InventoryCreateRequest request) {

        // 헤더에서 유저 정보 가져오기
        Long userId = tokenProvider.getUserIdFromToken(token);

        return inventoryService.create(userId, request);
    }

    // 인벤토리 수정
    @PatchMapping("/inventories/{inventoryIdx}")
    public ResponseEntity modify(@PathVariable("inventoryIdx") Long inventoryId,
                                 @RequestBody InventoryCreateRequest request) {

        return inventoryService.modify(inventoryId, request);
    }

    // 인벤토리 삭제
    @DeleteMapping("/inventories/{inventoryIdx}")
    public ResponseEntity delete(@PathVariable("inventoryIdx") Long inventoryId) {

        return inventoryService.delete(inventoryId);
    }

    // 인벤토리 내 소지품 추가
    @PostMapping("/inventories/{inventoryIdx}/stuff")
    public ResponseEntity addObject(@PathVariable("inventoryIdx") Long inventoryId,
                                    @RequestBody InventoryAddObjectRequest request) {

        return inventoryService.addObject(inventoryId, request.stuffName());
    }

    // 인벤토리 내 소지품 수정
    @PatchMapping("/inventories/{inventoryIdx}/stuff")
    public ResponseEntity modifyObject(@PathVariable("inventoryIdx") Long inventoryId,
                                       @RequestBody InventoryAddObjectRequest request) {

        return inventoryService.modifyObject(inventoryId, request.stuffName());
    }

    // 인벤토리 전체 조회
    @GetMapping("/inventories")
    public ResponseEntity getAll(@RequestHeader("xAuthToken") String token) {

        // 헤더에서 유저 정보 가져오기
        Long userId = tokenProvider.getUserIdFromToken(token);

        return inventoryService.getAll(userId);
    }
}
