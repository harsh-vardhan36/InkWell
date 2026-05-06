package com.inkWell.comment.service;

import com.inkWell.comment.domain.entity.Comment;
import com.inkWell.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing blog post comments.
 * Handles the full lifecycle of comments, including creation, approval, 
 * deletion, and engagement metrics (likes/replies).
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Retrieves all comments in the system.
     * 
     * @return A list of all {@link Comment}s.
     */
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    /**
     * Retrieves a single comment by its unique ID.
     * 
     * @param id The ID of the comment.
     * @return The {@link Comment} entity.
     * @throws RuntimeException if the comment is not found.
     */
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    /**
     * Retrieves all approved comments for a specific post.
     * 
     * @param postId The ID of the post.
     * @return A list of approved {@link Comment}s.
     */
    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdAndIsApprovedTrue(postId);
    }

    /**
     * Adds a new comment to a post.
     * Currently auto-approves all comments.
     * 
     * @param comment The comment entity to be added.
     * @return The saved {@link Comment} entity.
     */
    public Comment addComment(Comment comment) {
        comment.setApproved(true); // Auto-approve for now
        return commentRepository.save(comment);
    }

    /**
     * Saves or updates a comment entity.
     * 
     * @param comment The comment entity to save.
     * @return The saved {@link Comment} entity.
     */
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    /**
     * Permanently deletes a comment from the database.
     * 
     * @param id The ID of the comment to delete.
     */
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    /**
     * Performs a soft delete by marking the comment as deleted.
     * 
     * @param id The ID of the comment to soft delete.
     */
    public void softDelete(Long id) {
        Comment comment = getCommentById(id);
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    /**
     * Returns the total number of comments for a specific post.
     * 
     * @param postId The ID of the post.
     * @return Total comment count.
     */
    public long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    /**
     * Retrieves all replies associated with a parent comment.
     * 
     * @param parentId The ID of the parent comment.
     * @return A list of reply {@link Comment}s.
     */
    public List<Comment> getReplies(Long parentId) {
        return commentRepository.findByParentCommentId(parentId);
    }

    /**
     * Increments the like count for a specific comment.
     * 
     * @param id The ID of the comment.
     */
    @org.springframework.transaction.annotation.Transactional
    public void likeComment(Long id) {
        commentRepository.incrementLikeCount(id);
    }

    /**
     * Decrements the like count for a specific comment.
     * 
     * @param id The ID of the comment.
     */
    @org.springframework.transaction.annotation.Transactional
    public void unlikeComment(Long id) {
        commentRepository.decrementLikeCount(id);
    }

    /**
     * Approves a comment so it becomes visible on the post.
     * 
     * @param id The ID of the comment to approve.
     * @return The updated {@link Comment} entity.
     */
    public Comment approveComment(Long id) {
        Comment comment = getCommentById(id);
        comment.setApproved(true);
        return commentRepository.save(comment);
    }

    /**
     * Rejects or unapproves a comment.
     * 
     * @param id The ID of the comment to reject.
     * @return The updated {@link Comment} entity.
     */
    public Comment rejectComment(Long id) {
        Comment comment = getCommentById(id);
        comment.setApproved(false);
        return commentRepository.save(comment);
    }
}
