package ru.skypro.marketplace.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.marketplace.dto.*;
import ru.skypro.marketplace.service.impl.AdService;

import java.util.List;


@RestController
@RequestMapping("/ads")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Ads> getAllAds() {
        Ads ads = adService.getAds();
        return ResponseEntity.ok(ads);
    }

    @PostMapping(value = {"", "/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addAd(@RequestPart("image") MultipartFile imageFile,
                                   @RequestPart("properties") CreateOrUpdateAd createOrUpdateAd,
                                   Authentication authentication
    ) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        AdDto adDto = adService.createAd(createOrUpdateAd, securityUser.getId(), imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(adDto);
    }

    @GetMapping("/{adId}")
    @PreAuthorize("@adService.isAdOwner(authentication, #adId) or hasRole('ADMIN')")
    public ResponseEntity<?> getAds(@PathVariable Integer adId, Authentication authentication) {
        ExtendedAd extendedAd = adService.getExtendedAdById(adId, authentication);
        return ResponseEntity.status(HttpStatus.OK).body(extendedAd);
    }

    @DeleteMapping("/{adId}")
    @PreAuthorize("@adService.isAdOwner(authentication, #adId) or hasRole('ADMIN')")
    public ResponseEntity<Void> removeAd(@PathVariable Integer adId, Authentication authentication) {
        adService.deleteAd(adId, authentication);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{adId}")
    @PreAuthorize("@adService.isAdOwner(authentication, #adId) or hasRole('ADMIN')")
    public ResponseEntity<AdDto> updateAds(
            @PathVariable Integer adId, Authentication authentication,
            @RequestBody CreateOrUpdateAd createOrUpdateAd
    ) {

        AdDto updatedAd = adService.updateAd(adId, createOrUpdateAd, authentication);
        return ResponseEntity.status(HttpStatus.OK).body(updatedAd);
    }

    @GetMapping("/me")
    @PreAuthorize("@adService.isAdOwner(authentication, #adId) or hasRole('ADMIN')")
    public ResponseEntity<List<AdDto>> getAdsMe(Authentication authentication) {

        List<AdDto> ads = adService.getAdsForCurrentUser(authentication);
        return ResponseEntity.status(HttpStatus.OK).body(ads);
    }

    @PatchMapping(value = "/{adId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@adService.isAdOwner(authentication, #adId) or hasRole('ADMIN')")
    public ResponseEntity<String> updateImage(
            @PathVariable Integer adId, Authentication authentication,
            @RequestParam("image") MultipartFile imageFile
    ) {
        adService.updateAdImage(adId, imageFile, authentication);
        return ResponseEntity.status(HttpStatus.OK).body("Изображение успешно обновленно.");
    }

    @GetMapping("/ads/{adId}/images")
    public ResponseEntity<byte[]> getAdImage(@PathVariable Integer adId) {
        byte[] adImage = adService.getAdImage(adId);

        HttpHeaders headers = new HttpHeaders();
        if (adImage != null && adImage.length > 0) {
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(adImage, headers, HttpStatus.OK);
        } else {
            byte[] defaultImage = adService.getDefaultImageBytes();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(defaultImage, headers, HttpStatus.NOT_FOUND);
        }
    }
}
