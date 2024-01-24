package ru.skypro.marketplace.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.marketplace.dto.CommentDto;
import ru.skypro.marketplace.dto.Comments;
import ru.skypro.marketplace.dto.CreateOrUpdateComment;
import ru.skypro.marketplace.dto.SecurityUser;
import ru.skypro.marketplace.entity.Ad;
import ru.skypro.marketplace.entity.Comment;
import ru.skypro.marketplace.entity.User;
import ru.skypro.marketplace.exception.AdNotFoundException;
import ru.skypro.marketplace.exception.CommentNotFoundException;
import ru.skypro.marketplace.exception.ForbiddenException;
import ru.skypro.marketplace.repository.AdRepository;
import ru.skypro.marketplace.repository.CommentRepository;
import ru.skypro.marketplace.repository.UserRepository;
import ru.skypro.marketplace.service.mapper.CommentMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper,
                          AdRepository adRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    public Comments getCommentsByAdId(Integer adId) {

        List<Comment> comments = commentRepository.findByAdId(adId);
        if (comments.isEmpty()) {
            throw new AdNotFoundException("Не найдено комментариев к объявлению с id: " + adId);
        }
        List<CommentDto> commentDtos = comments.stream()
                .map(commentMapper::commentToCommentDTO)
                .collect(Collectors.toList());
        return new Comments(commentDtos.size(), commentDtos);
    }


    @Transactional
    public CommentDto addComment(Integer adId, CreateOrUpdateComment CreateOrUpdateComment) {

        Optional<Ad> optionalAd = adRepository.findById(adId);
        Ad ad = optionalAd.orElseThrow(() -> new AdNotFoundException("Объявление с id: " + adId + " не найдено"));
        Comment comment = new Comment();
        comment.setAd(ad);
        comment.setText(CreateOrUpdateComment.getText());
        comment.setCreatedAt(System.currentTimeMillis());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            User author = userRepository.findById(securityUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден с id: " + securityUser.getId()));
            comment.setAuthor(author);
        }

        comment = commentRepository.save(comment);

        return commentMapper.commentToCommentDTO(comment);
    }

    @Transactional
    public ResponseEntity<?> deleteComment(Integer commentId, Authentication authentication) {

        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElseThrow(() -> new CommentNotFoundException("Комментарий не найден с id: " + commentId));

        if (!isCommentOwner(authentication, commentId) && !hasAdminRole(authentication)) {
            throw new ForbiddenException("Доступ запрещен для обновления этого объявления.");
        }

        commentRepository.deleteById(commentId);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public CommentDto updateComment(Integer commentId, CreateOrUpdateComment CreateOrUpdateComment, Authentication authentication) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Комментарий не найден с id: " + commentId));
        if (!isCommentOwner(authentication, commentId) && !hasAdminRole(authentication)) {
            throw new ForbiddenException("Доступ запрещен для обновления этого объявления.");
        }

        comment.setText(CreateOrUpdateComment.getText());

        comment = commentRepository.save(comment);

        return commentMapper.commentToCommentDTO(comment);
    }

    private boolean isCommentOwner(Authentication authentication, Integer commentId) {
        if (authentication != null && authentication.isAuthenticated()) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return commentRepository.existsByIdAndAd_User_Id(commentId, securityUser.getId());
        }
        return false;
    }

    private boolean hasAdminRole(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }
}
