package com.oops.server.service;

import com.oops.server.context.StatusCode;
import com.oops.server.dto.request.InventoryCreateRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryStuff;
import com.oops.server.entity.InventoryTag;
import com.oops.server.entity.Stuff;
import com.oops.server.entity.Tag;
import com.oops.server.entity.User;
import com.oops.server.repository.InventoryStuffRepository;
import com.oops.server.repository.InventoryRepository;
import com.oops.server.repository.InventoryTagRepository;
import com.oops.server.repository.StuffRepository;
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
    private final StuffRepository stuffRepository;
    private final InventoryTagRepository inventoryTagRepository;
    private final InventoryStuffRepository inventoryStuffRepository;
    private final UserRepository userRepository;

    // 인벤토리 이름 중복 검사
    public boolean validateDuplicateName(User user, String name) {
        // 중복 시 false 반환
        return inventoryRepository.findByUserAndName(user, name) == null;
    }

    // 인벤토리 태그 저장
    public void saveInventoryTags(Inventory inventory, List<Integer> tagIdList) {

        List<Tag> tagList = new ArrayList<>();

        for (Integer tagId : tagIdList) {
            tagList.add(tagRepository.findByTagId(tagId));
        }

        for (Tag tag : tagList) {
            InventoryTag inventoryTag = InventoryTag.create(inventory, tag);
            inventoryTagRepository.save(inventoryTag);
        }
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
        saveInventoryTags(inventory, request.inventoryTag());

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
        saveInventoryTags(inventory, request.inventoryTag());

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인벤토리 삭제
    public ResponseEntity delete(Long inventoryId) {

        inventoryRepository.deleteByInventoryId(inventoryId);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인벤토리 내 소지품 추가
    public ResponseEntity addObject(Long inventoryId, List<String> stuffNameList) {

        Inventory inventory = inventoryRepository.findByInventoryId(inventoryId);

        // 해당 인벤토리가 없을 경우
        if (inventory == null) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.NOT_FOUND, "해당 인벤토리가 없습니다."),
                    HttpStatus.NOT_FOUND);
        }

        for (String stuffName : stuffNameList) {
            Stuff stuff = stuffRepository.findByName(stuffName);
            InventoryStuff inventoryStuff = InventoryStuff.create(inventory, stuff);
            inventoryStuffRepository.save(inventoryStuff);
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인벤토리 내 소지품 수정
    public ResponseEntity modifyObject(Long inventoryId, List<String> stuffNameList) {

        Inventory inventory = inventoryRepository.findByInventoryId(inventoryId);

        // 해당 인벤토리가 없을 경우
        if (inventory == null) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.NOT_FOUND, "해당 인벤토리가 없습니다."),
                    HttpStatus.NOT_FOUND);
        }

        // 해당 인벤토리의 소지품 전체 삭제 후 추가
        inventoryStuffRepository.deleteAllByInventory(inventory);

        for (String stuffName : stuffNameList) {
            Stuff stuff = stuffRepository.findByName(stuffName);
            InventoryStuff inventoryStuff = InventoryStuff.create(inventory, stuff);
            inventoryStuffRepository.save(inventoryStuff);
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인벤토리 전체 조회
    public ResponseEntity getAll(Long userId) {

        User user = userRepository.findByUserId(userId);
        List<Inventory> inventoryList = inventoryRepository.findAllByUser(user);

        // 인벤토리 idx & 이름 가져오기
        List<Long> inventoryIdxList = new ArrayList<>();
        List<String> inventoryNameList = new ArrayList<>();
        for (Inventory tempInventory : inventoryList) {
            inventoryIdxList.add(tempInventory.getInventoryId());
            inventoryNameList.add(tempInventory.getName());

            // 해당 인벤토리의 소지품 관련 정보 가져오기

        }

        return null;
    }
}
