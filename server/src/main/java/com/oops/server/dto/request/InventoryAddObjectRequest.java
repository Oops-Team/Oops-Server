package com.oops.server.dto.request;

import java.util.List;

public record InventoryAddObjectRequest(
        List<String> stuffName
) {}
