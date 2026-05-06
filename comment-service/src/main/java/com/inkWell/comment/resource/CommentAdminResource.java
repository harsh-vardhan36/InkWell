package com.inkWell.comment.resource;

import com.inkWell.comment.domain.entity.Comment;
import com.inkWell.comment.domain.enums.CommentStatus;
import com.inkWell.comment.repository.CommentRepository;
import com.inkWell.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments/admin")
public class CommentAdminResource {

    private final CommentRepository commentRepository;
    private final CommentService commentService;

    public CommentAdminResource(CommentRepository commentRepository, CommentService commentService) {
        this.commentRepository = commentRepository;
        this.commentService = commentService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Comment>> getPendingComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Comment> approveComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.approveComment(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Comment> rejectComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.rejectComment(id));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> hardDeleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Comment> updateStatus(@PathVariable Long id, @RequestParam CommentStatus status) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setStatus(status);
        return ResponseEntity.ok(commentRepository.save(comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> softDeleteComment(@PathVariable Long id) {
        commentService.softDelete(id);
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully (soft-delete)"));
    }
}
