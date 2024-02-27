package com.oops.server.dto.response;

import com.oops.server.entity.Stuff;
import java.util.List;

// 인벤토리 전체 조회 시 사용
public record InventoryGetAllResponse(
        List<Long> inventoryIdx,
        List<Integer> inventoryIconIdx,
        List<String> inventoryName,
        int stuffNum,
        List<Stuff> stuffList
) {}
