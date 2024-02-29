package com.oops.server.dto.response;

import com.oops.server.dto.etc.StuffDto;
import java.util.List;

public record TodoInventoryModifyResponse(
        Long inventoryId,
        List<StuffDto> stuffList
) {}
