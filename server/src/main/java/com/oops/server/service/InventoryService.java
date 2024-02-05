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

    // 인벤토리 생성
    public ResponseEntity create(Long userId, InventoryCreateRequest request) {

        // 유저 정보 가져오기
        User user = userRepository.findByUserId(userId);

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
}
