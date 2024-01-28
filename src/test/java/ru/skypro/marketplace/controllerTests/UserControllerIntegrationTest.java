package ru.skypro.marketplace.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.skypro.marketplace.dto.NewPassword;
import ru.skypro.marketplace.dto.Register;
import ru.skypro.marketplace.dto.UpdateUser;
import ru.skypro.marketplace.entity.Role;
import ru.skypro.marketplace.entity.User;
import ru.skypro.marketplace.repository.UserRepository;
import ru.skypro.marketplace.service.impl.AuthServiceImpl;
import ru.skypro.marketplace.service.impl.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthServiceImpl authServiceImpl;
    @Autowired
    private UserRepository userRepository;

    private User createdUser;


    @BeforeEach
    public void setUp() {
        Register register = new Register("testUsername", "testPassword", "testFirstName", "testLastName", "testPhone", Role.USER);
        authServiceImpl.register(register);
        createdUser = userRepository.findByEmail(register.getUsername()).orElse(null);
    }

    @AfterEach
    public void tearDown() {
        if (createdUser != null) {
            userRepository.delete(createdUser);
        }
    }

    @Test
    public void testSetPassword() throws Exception {
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("testPassword");
        newPassword.setNewPassword("newPassword");

        mockMvc.perform(put("/users/setPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + getBase64Credentials())
                        .content(objectMapper.writeValueAsString(newPassword))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testGetUser() throws Exception {

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + getBase64Credentials())
                )
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateUser() throws Exception {

        UpdateUser updatedUser = new UpdateUser("UpdatedFirstName", "UpdatedLastName", "UpdatedPhone");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + getBase64Credentials())
                        .content(objectMapper.writeValueAsString(updatedUser))
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("UpdatedFirstName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("UpdatedLastName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value("UpdatedPhone"));
    }

    private String getBase64Credentials() {
        String credentials = "testUsername" + ":" + "testPassword";
        byte[] credentialsBytes = credentials.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(credentialsBytes);
    }
}
