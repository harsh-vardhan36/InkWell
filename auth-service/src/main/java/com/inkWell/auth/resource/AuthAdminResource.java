package com.inkWell.auth.resource;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Plan;
import com.inkWell.auth.domain.enums.Role;
import com.inkWell.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AuthAdminResource {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Plan plan) {
        return ResponseEntity.ok(userRepository.searchUsers(query, role, plan));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getUserStats() {
        Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", userRepository.count());
        stats.put("pro", userRepository.findAll().stream().filter(u -> u.getPlan() == Plan.PRO).count());
        stats.put("free", userRepository.findAll().stream().filter(u -> u.getPlan() == Plan.FREE).count());
        stats.put("readers", userRepository.findAll().stream().filter(u -> u.getRole() == Role.READER).count());
        stats.put("authors", userRepository.findAll().stream().filter(u -> u.getRole() == Role.AUTHOR).count());
        stats.put("admins", userRepository.findAll().stream().filter(u -> u.getRole() == Role.ADMIN).count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Role role = Role.valueOf(payload.get("role").toUpperCase());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        boolean active = (boolean) payload.getOrDefault("isActive", payload.getOrDefault("enabled", true));
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
