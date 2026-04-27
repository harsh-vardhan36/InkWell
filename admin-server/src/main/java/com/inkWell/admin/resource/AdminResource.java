package com.inkWell.admin.resource;

import com.inkWell.admin.client.*;
import com.inkWell.admin.domain.entity.AuditLog;
import com.inkWell.admin.dto.UserDto;
import com.inkWell.admin.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminResource {

    private final AuthServiceClient authServiceClient;
    private final PostServiceClient postServiceClient;
    private final CommentServiceClient commentServiceClient;
    private final CategoryServiceClient categoryServiceClient;
    private final NewsletterServiceClient newsletterServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final AuditService auditService;

    // --- Dashboard & Analytics ---

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard() {
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("totalUsers", authServiceClient.getAllUsers().size());
        dashboardData.put("totalPosts", postServiceClient.getAllPosts().size());
        dashboardData.put("totalComments", commentServiceClient.getAllComments().size());
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> viewPlatformAnalytics() {
        // In a real app, this would call specialized analytics endpoints
        return ResponseEntity.ok(postServiceClient.getPostStats());
    }

    // --- User Management ---

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> manageUsers() {
        return ResponseEntity.ok(authServiceClient.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authServiceClient.getUserById(id));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long id, @RequestParam String role) {
        UserDto updated = authServiceClient.updateUserRole(id, role);
        auditService.logAction("CHANGE_ROLE", "USER", id.toString(), "Changed role to " + role);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<UserDto> suspendUser(@PathVariable Long id) {
        UserDto updated = authServiceClient.updateUserStatus(id, false);
        auditService.logAction("SUSPEND", "USER", id.toString(), "Suspended user account");
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/users/{id}/reactivate")
    public ResponseEntity<UserDto> reactivateUser(@PathVariable Long id) {
        UserDto updated = authServiceClient.updateUserStatus(id, true);
        auditService.logAction("REACTIVATE", "USER", id.toString(), "Reactivated user account");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        Map<String, String> response = authServiceClient.deleteUser(id);
        auditService.logAction("DELETE", "USER", id.toString(), "Permanently deleted user");
        return ResponseEntity.ok(response);
    }

    // --- Post Management ---

    @GetMapping("/posts")
    public ResponseEntity<List<Object>> manageAllPosts() {
        return ResponseEntity.ok(postServiceClient.getAllPosts());
    }

    @PutMapping("/posts/{id}/feature")
    public ResponseEntity<Map<String, String>> featurePost(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean featured) {
        Map<String, String> response = postServiceClient.featurePost(id, featured);
        auditService.logAction(featured ? "FEATURE" : "UNFEATURE", "POST", id.toString(), "");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long id) {
        Map<String, String> response = postServiceClient.deletePost(id);
        auditService.logAction("DELETE", "POST", id.toString(), "Admin deleted post");
        return ResponseEntity.ok(response);
    }

    // --- Comment Management ---

    @GetMapping("/comments")
    public ResponseEntity<List<Object>> manageComments() {
        return ResponseEntity.ok(commentServiceClient.getAllComments());
    }

    @PutMapping("/comments/{id}/approve")
    public ResponseEntity<Map<String, String>> approveComment(@PathVariable Long id) {
        Map<String, String> response = commentServiceClient.updateCommentStatus(id, "APPROVED");
        auditService.logAction("APPROVE", "COMMENT", id.toString(), "");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable Long id) {
        Map<String, String> response = commentServiceClient.deleteComment(id);
        auditService.logAction("DELETE", "COMMENT", id.toString(), "");
        return ResponseEntity.ok(response);
    }

    // --- Category & Tag Management ---

    @GetMapping("/categories")
    public ResponseEntity<List<Object>> manageCategories() {
        return ResponseEntity.ok(categoryServiceClient.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<Object> createCategory(@RequestBody Object category) {
        Object created = categoryServiceClient.createCategory(category);
        auditService.logAction("CREATE", "CATEGORY", "NEW", "Created category");
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long id) {
        Map<String, String> response = categoryServiceClient.deleteCategory(id);
        auditService.logAction("DELETE", "CATEGORY", id.toString(), "");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<Object>> manageTags() {
        return ResponseEntity.ok(categoryServiceClient.getAllTags());
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Map<String, String>> deleteTag(@PathVariable Long id) {
        Map<String, String> response = categoryServiceClient.deleteTag(id);
        auditService.logAction("DELETE", "TAG", id.toString(), "");
        return ResponseEntity.ok(response);
    }

    // --- Newsletter & Notifications ---

    @GetMapping("/subscribers")
    public ResponseEntity<List<Object>> viewSubscribers() {
        return ResponseEntity.ok(newsletterServiceClient.getAllSubscribers());
    }

    @PostMapping("/newsletter/send")
    public ResponseEntity<Map<String, String>> sendNewsletter(@RequestBody Object newsletterRequest) {
        Map<String, String> response = newsletterServiceClient.sendNewsletter(newsletterRequest);
        auditService.logAction("SEND_NEWSLETTER", "SYSTEM", "N/A", "Sent bulk newsletter");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notifications/broadcast")
    public ResponseEntity<Map<String, String>> sendPlatformNotification(@RequestBody Object notificationRequest) {
        Map<String, String> response = notificationServiceClient.broadcastNotification(notificationRequest);
        auditService.logAction("BROADCAST_NOTIFICATION", "SYSTEM", "N/A", "Sent platform-wide notification");
        return ResponseEntity.ok(response);
    }

    // --- Audit Logs ---

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> viewAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }
}
