package ru.skypro.marketplace.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.marketplace.dto.Register;
import ru.skypro.marketplace.dto.SecurityUser;
import ru.skypro.marketplace.exception.UserAlreadyExistsException;
import ru.skypro.marketplace.repository.UserRepository;
import ru.skypro.marketplace.service.AuthService;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;

    public AuthServiceImpl(
            PasswordEncoder passwordEncoder, UserRepository userRepository) {

        this.encoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public boolean login(String userName, String password) {
        UserDetails userDetails = userRepository.findByEmail(userName)
                .map(SecurityUser::from)
                .orElse(null);

        return userDetails != null && encoder.matches(password, userDetails.getPassword());
    }

    @Override
    public boolean register(Register register) {
        Optional<ru.skypro.marketplace.entity.User> existingUser = userRepository.findByEmail(register.getUsername());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException();
        }

        ru.skypro.marketplace.entity.User user = ru.skypro.marketplace.entity.User.builder()
                .email(register.getUsername())
                .password(encoder.encode(register.getPassword()))
                .firstName(register.getFirstName())
                .lastName(register.getLastName())
                .phone(register.getPhone())
                .role(register.getRole())
                .build();
        userRepository.save(user);

        return true;
    }
}
