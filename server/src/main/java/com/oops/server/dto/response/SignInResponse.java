package com.oops.server.dto.response;

import com.oops.server.dto.etc.RemindAlertDto;
import java.util.List;

public record SignInResponse(
        String name,
        String xAuthToken,
        List<RemindAlertDto> alertList
) {}
