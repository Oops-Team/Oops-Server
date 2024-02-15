package com.oops.server.dto.response;

import java.util.List;

// 인벤토리 상세 조회 시 사용
public record InventoryGetOneResponse(
        String inventoryName,
        List<String> stuffImgURIList,
        List<String> stuffNameList,
        int stuffNum
) {}
