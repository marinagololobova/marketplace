package ru.skypro.marketplace.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.marketplace.dto.NewPassword;
import ru.skypro.marketplace.dto.SecurityUser;
import ru.skypro.marketplace.dto.UpdateUser;
import ru.skypro.marketplace.dto.UserDto;
import ru.skypro.marketplace.service.impl.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/setPassword")
    public ResponseEntity<?> setPassword(@RequestBody NewPassword newPassword, Authentication authentication) {

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        boolean passwordChanged = userService.changePassword(securityUser.getId(), newPassword);
        return ResponseEntity.status(HttpStatus.OK).body(passwordChanged);

    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getUser(Authentication authentication) {

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        UserDto user = userService.getUserByUsername(securityUser.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(user);

    }

    @PatchMapping("/me")
    public ResponseEntity<UpdateUser> updateUser(@RequestBody UpdateUser updateUser, Authentication authentication) {

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        UpdateUser updatedUserProfile = userService.updateUserProfile(securityUser.getId(), updateUser);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUserProfile);

    }

    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateUserImage(@RequestParam("image") MultipartFile image, Authentication authentication) {

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        UserDto currentUser = userService.updateProfileImage(securityUser.getId(), image);
        return ResponseEntity.status(HttpStatus.OK).body(currentUser);
    }

    @GetMapping(value = "/me/images", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, "image/*"})
    public ResponseEntity<byte[]> getUserImage(@PathVariable String imageName, Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        byte[] imageBytes = userService.getUserImage(securityUser.getId(), imageName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        if (imageBytes.length > 0) {
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } else {
            byte[] defaultImageBytes = userService.getDefaultImageBytes();
            return new ResponseEntity<>(defaultImageBytes, headers, HttpStatus.NOT_FOUND);
        }
    }
}
