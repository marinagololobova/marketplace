package ru.skypro.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.marketplace.entity.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByAdId(Integer adId);

    boolean existsByIdAndAd_User_Id(Integer commentId, Integer userId);
}
