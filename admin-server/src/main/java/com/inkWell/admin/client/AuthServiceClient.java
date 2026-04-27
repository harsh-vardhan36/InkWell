package com.inkWell.admin.client;

import com.inkWell.admin.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service", path = "/auth/admin")
public interface AuthServiceClient {

    @GetMapping("/users")
    List<UserDto> getAllUsers(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "plan", required = false) String plan
    );

    @GetMapping("/stats")
    Map<String, Long> getUserStats();

    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PutMapping("/users/{id}/role")
    UserDto updateUserRole(@PathVariable("id") Long id, @RequestBody Map<String, String> payload);

    @PutMapping("/users/{id}/status")
    UserDto updateUserStatus(@PathVariable("id") Long id, @RequestBody Map<String, Object> payload);

    @DeleteMapping("/users/{id}")
    Map<String, String> deleteUser(@PathVariable("id") Long id);
}
