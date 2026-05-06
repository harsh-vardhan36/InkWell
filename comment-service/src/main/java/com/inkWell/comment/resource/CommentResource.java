package com.inkWell.comment.resource;

import com.inkWell.comment.domain.entity.Comment;
import com.inkWell.comment.dto.CommentDTO;
import com.inkWell.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentResource {

    private final CommentService commentService;

    public CommentResource(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId).stream()
                .map(this::convertToDTO)
                .toList());
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentCount(postId));
    }

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@RequestBody CommentDTO commentDTO) {
        Comment comment = Comment.builder()
                .postId(commentDTO.getPostId())
                .authorId(commentDTO.getAuthorId())
                .content(commentDTO.getContent())
                .parentCommentId(commentDTO.getParentCommentId())
                .build();
        return ResponseEntity.ok(convertToDTO(commentService.addComment(comment)));
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentDTO>> getReplies(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getReplies(id).stream()
                .map(this::convertToDTO)
                .toList());
    }

    private CommentDTO convertToDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .authorName(comment.getAuthorName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likes(comment.getLikes())
                .parentCommentId(comment.getParentCommentId())
                .build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long id) {
        commentService.unlikeComment(id);
        return ResponseEntity.ok().build();
    }

}
