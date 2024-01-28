package ru.skypro.marketplace.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.skypro.marketplace.controller.AuthController;
import ru.skypro.marketplace.dto.Login;
import ru.skypro.marketplace.dto.Register;
import ru.skypro.marketplace.entity.Role;
import ru.skypro.marketplace.service.AuthService;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    public void testLoginSuccess() throws Exception {
        Login login = new Login("username", "password");

        when(authService.login("username", "password")).thenReturn(true);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(login)))
                .andExpect(status().isOk());

        verify(authService, times(1)).login("username", "password");
        verifyNoMoreInteractions(authService);
    }

    @Test
    public void testLoginUnauthorized() throws Exception {
        Login login = new Login("username", "password");

        when(authService.login("username", "password")).thenReturn(false);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(login)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login("username", "password");
        verifyNoMoreInteractions(authService);
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        Register register = new Register("username", "password", "Marina", "Gololobova", "+7 (111) 111-22-33", Role.USER);

        when(authService.register(register)).thenReturn(true);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(register)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(register);
        verifyNoMoreInteractions(authService);
    }

    @Test
    public void testRegisterBadRequest() throws Exception {
        Register register = new Register("username", "password", "Marina", "Gololobova", "+7 (111) 111-22-33", Role.USER);

        when(authService.register(register)).thenReturn(false);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(register)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).register(register);
        verifyNoMoreInteractions(authService);
    }

    private String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
