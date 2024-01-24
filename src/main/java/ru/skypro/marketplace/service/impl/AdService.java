package ru.skypro.marketplace.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.marketplace.dto.*;
import ru.skypro.marketplace.entity.Ad;
import ru.skypro.marketplace.entity.User;
import ru.skypro.marketplace.exception.AdNotFoundException;
import ru.skypro.marketplace.exception.ForbiddenException;
import ru.skypro.marketplace.repository.AdRepository;
import ru.skypro.marketplace.repository.UserRepository;
import ru.skypro.marketplace.service.mapper.AdMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdService {
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(AdService.class);

    @Value("${image.upload.path}")
    private String imagePath;

    public AdService(AdRepository adRepository, AdMapper adMapper, UserRepository userRepository) {
        this.adRepository = adRepository;
        this.adMapper = adMapper;
        this.userRepository = userRepository;
    }

    public Ads getAds() {
        List<AdDto> adsList;
        List<Ad> ads = adRepository.findAll();
        adsList = ads.stream().map(adMapper::adToAdDTO).collect(Collectors.toList());

        int count = adsList.size();
        return new Ads(count, adsList);
    }

    @Transactional
    public AdDto createAd(CreateOrUpdateAd createOrUpdateAd, Integer userId, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с  id: " + userId + " не найден"));

        Ad ad = new Ad();
        ad.setTitle(createOrUpdateAd.getTitle());
        ad.setPrice(createOrUpdateAd.getPrice());
        ad.setDescription(createOrUpdateAd.getDescription());
        ad.setUser(user);

        ad = adRepository.save(ad);

        if (!imageFile.isEmpty()) {
            String imageName = userId + "_" + imageFile.getOriginalFilename();

            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    Path filePath = Paths.get(imagePath, imageName);
                    Files.write(filePath, imageFile.getBytes());
                    ad.setImage(imageName);
                    adRepository.save(ad);
                } catch (IOException e) {
                    logger.error("При обработке изображения произошла ошибка: {}", e.getMessage());
                }
            } else {
                logger.error("Путь загрузки изображения не настроен.");
            }
        }

        return adMapper.adToAdDTO(ad);
    }

    @Transactional
    public ExtendedAd getExtendedAdById(Integer adId, Authentication authentication) {

        Optional<Ad> optionalAd = adRepository.findById(adId);
        Ad ad = optionalAd.orElseThrow(() -> new AdNotFoundException("Объявление с id: " + adId + " не найдено"));

        if (!isAdOwner(authentication, adId) && !hasAdminRole(authentication)) {
            throw new ForbiddenException("Доступ запрещен для обновления этого объявления.");
        }
        User user = ad.getUser();

        return new ExtendedAd(
                ad.getId(),
                user.getFirstName(),
                user.getLastName(),
                ad.getDescription(),
                user.getEmail(),
                ad.getImage(),
                user.getPhone(),
                ad.getPrice(),
                ad.getTitle()
        );

    }

    @Transactional
    public void deleteAd(Integer adId, Authentication authentication) {

        Optional<Ad> optionalAd = adRepository.findById(adId);
        Ad ad = optionalAd.orElseThrow(() -> new AdNotFoundException("Объявление с id: " + adId + " не найдено"));
        if (!isAdOwner(authentication, adId) && !hasAdminRole(authentication)) {
            throw new ForbiddenException("Доступ запрещен для обновления этого объявления.");
        }
        adRepository.deleteById(adId);

    }

    @Transactional
    public AdDto updateAd(Integer adId, CreateOrUpdateAd createOrUpdateAd, Authentication authentication) {

        Optional<Ad> optionalAd = adRepository.findById(adId);
        Ad ad = optionalAd.orElseThrow(() -> new AdNotFoundException("Объявление с id: " + adId + " не найдено"));

        if (!isAdOwner(authentication, adId) && !hasAdminRole(authentication)) {
            throw new ForbiddenException("Доступ запрещен для обновления этого объявления.");
        }

        ad.setTitle(createOrUpdateAd.getTitle());
        ad.setPrice(createOrUpdateAd.getPrice());
        ad.setDescription(createOrUpdateAd.getDescription());

        ad = adRepository.save(ad);

        return adMapper.adToAdDTO(ad);
    }

    @Transactional
    public List<AdDto> getAdsForCurrentUser(Authentication authentication) {

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        List<Ad> ads = adRepository.findByUserId(securityUser.getId());

        return ads.stream().map(adMapper::adToAdDTO).collect(Collectors.toList());
    }

    @Transactional
    public void updateAdImage(Integer adId, MultipartFile imageData, Authentication authentication) {
        Optional<Ad> optionalAd = adRepository.findById(adId);
        Ad ad = optionalAd.orElseThrow(() -> new AdNotFoundException("Объявление с id: " + adId + " не найдено"));
        if (!isAdOwner(authentication, adId) && !hasAdminRole(authentication)) {
            throw new ForbiddenException("Доступ запрещен для обновления этого объявления.");
        }

        if (imageData != null && !imageData.isEmpty()) {
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    String imageName = adId + "_" + imageData.getOriginalFilename();
                    Path filePath = Paths.get(imagePath, imageName);

                    Files.write(filePath, imageData.getBytes());
                    ad.setImage(imageName);
                    adRepository.save(ad);
                    logger.info("Изображение с id {} было успешно обновлено.", adId);
                } else {
                    logger.error("Путь загрузки изображения не настроен.");
                }
            } catch (IOException e) {
                logger.error("При обработке изображения произошла ошибка: {}", e.getMessage());
            }
        } else {
            logger.warn("Для идентификатора объявления {} не предоставлены данные изображения. Изображение не обновлено.", adId);
        }
    }

    private boolean isAdOwner(Authentication authentication, Integer adId) {
        if (authentication != null && authentication.isAuthenticated()) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return adRepository.existsByIdAndUser_Id(adId, securityUser.getId());
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


    public byte[] getAdImage(Integer adId) {

        Optional<Ad> optionalAd = adRepository.findById(adId);
        Ad ad = optionalAd.orElseThrow(() -> new AdNotFoundException("Объявление с id: " + adId + " не найдено"));

        String imageString = ad.getImage();
        if (imageString != null && !imageString.isEmpty()) {
            return imageString.getBytes(StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }


    public byte[] getDefaultImageBytes() {
        return null;
    }
}
