package ru.skypro.marketplace.service;

import ru.skypro.marketplace.dto.Register;

public interface AuthService {
    boolean login(String userName, String password);

    boolean register(Register register);
}
