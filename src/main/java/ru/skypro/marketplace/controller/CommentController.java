package ru.skypro.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.skypro.marketplace.dto.CommentDto;
import ru.skypro.marketplace.dto.Comments;
import ru.skypro.marketplace.dto.CreateOrUpdateComment;
import ru.skypro.marketplace.service.impl.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ads/{adId}/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping({"", "/"})
    public ResponseEntity<Comments> getComments(@PathVariable Integer adId) {

        Comments comments = commentService.getCommentsByAdId(adId);
        return ResponseEntity.status(HttpStatus.OK).body(comments);
    }

    @PostMapping({"", "/"})
    public ResponseEntity<CommentDto> addComment(@PathVariable Integer adId,
                                                 @RequestBody CreateOrUpdateComment createOrUpdateComment) {

        CommentDto comment = commentService.addComment(adId, createOrUpdateComment);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("@commentService.isCommentOwner(authentication, #commentId) or hasRole('ADMIN')")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentId, Authentication authentication) {
        return commentService.deleteComment(commentId, authentication);
    }

    @PatchMapping("/{commentId}")
    @PreAuthorize("@commentService.isCommentOwner(authentication, #commentId) or hasRole('ADMIN')")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Integer commentId, Authentication authentication,
                                                    @RequestBody CreateOrUpdateComment CreateOrUpdateComment) {

        CommentDto updatedComment = commentService.updateComment(commentId, CreateOrUpdateComment, authentication);
        return ResponseEntity.status(HttpStatus.OK).body(updatedComment);
    }
}
