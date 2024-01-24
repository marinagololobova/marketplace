package ru.skypro.marketplace.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.marketplace.dto.NewPassword;
import ru.skypro.marketplace.dto.SecurityUser;
import ru.skypro.marketplace.dto.UpdateUser;
import ru.skypro.marketplace.dto.UserDto;
import ru.skypro.marketplace.entity.User;
import ru.skypro.marketplace.exception.UserNotFoundException;
import ru.skypro.marketplace.repository.UserRepository;
import ru.skypro.marketplace.service.mapper.UserMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${image.upload.path}")
    private String imagePath;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public boolean changePassword(Integer userId, NewPassword newPassword) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (passwordEncoder.matches(newPassword.getCurrentPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword.getNewPassword()));
                userRepository.save(user);
                return true;
            } else {
                throw new IllegalArgumentException("Текущий пароль неверен");
            }
        } else {
            throw new UsernameNotFoundException("Пользователь не найден с id: " + userId);
        }
    }

    public UserDto getUserByUsername(String username) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден с таким именем: " + username));
        return userMapper.userToUserDTO(user);
    }

    @Transactional
    public UpdateUser updateUserProfile(Integer userId, UpdateUser updateUser) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден с id: " + userId));

        user.setFirstName(updateUser.getFirstName());
        user.setLastName(updateUser.getLastName());
        user.setPhone(updateUser.getPhone());

        user = userRepository.save(user);

        UpdateUser updatedUserProfile = new UpdateUser();
        updatedUserProfile.setFirstName(user.getFirstName());
        updatedUserProfile.setLastName(user.getLastName());
        updatedUserProfile.setPhone(user.getPhone());

        return updatedUserProfile;
    }

    @Transactional
    public UserDto updateProfileImage(Integer userId, MultipartFile image) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с id: " + userId));

        if (imagePath != null && !imagePath.isEmpty()) {
            String fileName = userId + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(imagePath, fileName);

            try {
                Files.copy(image.getInputStream(), Paths.get(imagePath, image.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
                user.setImage(fileName);
                userRepository.save(user);
                return userMapper.userToUserDTO(user);
            } catch (IOException e) {
                logger.error("Ошибка при записи файла изображения для пользователя с id {}: {}", userId, e.getMessage());
                throw new RuntimeException("Не удалось записать файл изображения.", e);
            }
        } else {
            throw new RuntimeException("Путь загрузки изображения не настроен.");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(email)
                .map(SecurityUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public byte[] getUserImage(Integer userId, String imageName) {

        Path imageFilePath = Paths.get(imagePath, userId + "_" + imageName);

        if (!Files.exists(imageFilePath)) {
            return getDefaultImageBytes();
        }

        try (InputStream inputStream = Files.newInputStream(imageFilePath)) {
            return StreamUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            logger.error("Ошибка при чтении изображения пользователя: {}", e.getMessage());
            return getDefaultImageBytes();
        }
    }

    public byte[] getDefaultImageBytes() {
        return null;
    }
}
