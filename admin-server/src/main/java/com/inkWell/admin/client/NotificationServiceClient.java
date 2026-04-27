package com.inkWell.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "notification-service", path = "/notifications/admin")
public interface NotificationServiceClient {

    @PostMapping("/platform-broadcast")
    Map<String, String> broadcastNotification(@RequestBody Object notificationRequest);
}
