package com.inkWell.admin.client;

import com.inkWell.admin.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service", path = "/auth/admin")
public interface AuthServiceClient {

    @GetMapping("/users")
    List<UserDto> getAllUsers();

    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PutMapping("/users/{id}/role")
    UserDto updateUserRole(@PathVariable("id") Long id, @RequestParam("role") String role);

    @PutMapping("/users/{id}/status")
    UserDto updateUserStatus(@PathVariable("id") Long id, @RequestParam("active") boolean active);

    @DeleteMapping("/users/{id}")
    Map<String, String> deleteUser(@PathVariable("id") Long id);
}
