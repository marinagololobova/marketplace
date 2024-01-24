package ru.skypro.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.skypro.marketplace.entity.Ad;

import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Integer> {

    @Query("SELECT a FROM Ad a WHERE a.user.id = :userId")
    List<Ad> findByUserId(Integer userId);

    boolean existsByIdAndUser_Id(Integer adId, Integer userId);
}
