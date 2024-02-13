package com.oops.server.service;

import com.oops.server.context.StatusCode;
import com.oops.server.dto.request.InventoryCreateRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryTag;
import com.oops.server.entity.Tag;
import com.oops.server.entity.User;
import com.oops.server.repository.InventoryRepository;
import com.oops.server.repository.InventoryTagRepository;
import com.oops.server.repository.TagRepository;
import com.oops.server.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final TagRepository tagRepository;
    private final InventoryTagRepository inventoryTagRepository;
    private final UserRepository userRepository;

    // 인벤토리 이름 중복 검사
    public boolean validateDuplicateName(User user, String name) {
        // 중복 시 false 반환
        return inventoryRepository.findByUserAndName(user, name) == null;
    }

    // 인벤토리 생성
    public ResponseEntity create(Long userId, InventoryCreateRequest request) {

        // 유저 정보 가져오기
        User user = userRepository.findByUserId(userId);

        // 인벤토리 이름이 중복되었다면
        if (!validateDuplicateName(user, request.inventoryName())) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.CONFLICT, "이미 있는 이름이에요"),
                    HttpStatus.CONFLICT);
        }

        // 인벤토리 데이터 추가
        Inventory inventory = Inventory.create(user, request.inventoryName());
        inventoryRepository.save(inventory);
        inventory = inventoryRepository.findByUserAndName(user,
                request.inventoryName());

        // 인벤토리 태그 데이터 추가
        List<Tag> tagList = new ArrayList<>();
        for (Integer tagId : request.inventoryTag()) {
            tagList.add(tagRepository.findByTagId(tagId));
        }
        for (Tag tag : tagList) {
            InventoryTag inventoryTag = InventoryTag.create(inventory, tag);
            inventoryTagRepository.save(inventoryTag);
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인벤토리 수정
    public ResponseEntity modify(Long inventoryId, InventoryCreateRequest request) {

        // 해당 인벤토리가 없을 경우
        if (inventoryRepository.findByInventoryId(inventoryId) == null) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.NOT_FOUND, "해당 인벤토리가 없습니다."),
                    HttpStatus.NOT_FOUND);
        }

        Inventory inventory = inventoryRepository.findByInventoryId(inventoryId);

        // 인벤토리 이름이 중복되었다면
        if (!validateDuplicateName(inventory.getUser(), request.inventoryName())) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.CONFLICT, "이미 있는 이름이에요"),
                    HttpStatus.CONFLICT);
        }

        // 인벤토리 테이블 갱신 (이름 갱신)
        inventory.setName(request.inventoryName());
        inventoryRepository.save(inventory);

        // 인벤토리 태그 테이블 갱신 (태그 갱신)
        inventoryTagRepository.deleteAllByInventory(inventory);

        List<Tag> tagList = new ArrayList<>();
        for (Integer tagId : request.inventoryTag()) {
            tagList.add(tagRepository.findByTagId(tagId));
        }
        for (Tag tag : tagList) {
            InventoryTag inventoryTag = InventoryTag.create(inventory, tag);
            inventoryTagRepository.save(inventoryTag);
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }
}
