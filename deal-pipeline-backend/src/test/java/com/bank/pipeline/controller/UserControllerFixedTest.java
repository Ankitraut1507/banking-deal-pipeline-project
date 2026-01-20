package com.bank.pipeline.controller;

import com.bank.pipeline.dto.CreateUserRequest;
import com.bank.pipeline.model.User;
import com.bank.pipeline.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerFixedTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void initializeAdmin_shouldReturnCreated() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setPassword("admin123");

        User adminUser = new User();
        adminUser.setId("1");
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");

        when(userService.createUser(any(User.class))).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    @Test
    void initializeAdmin_invalidRequest_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initializeAdmin_nullUsername_shouldReturnBadRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(null);
        request.setEmail("admin@example.com");
        request.setPassword("admin123");

        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initializeAdmin_nullEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setEmail(null);
        request.setPassword("admin123");

        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initializeAdmin_nullPassword_shouldReturnBadRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initializeAdmin_shortUsername_shouldReturnBadRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("ab");
        request.setEmail("admin@example.com");
        request.setPassword("admin123");

        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initializeAdmin_shortPassword_shouldReturnBadRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setPassword("123");

        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initializeAdmin_invalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setEmail("invalid-email");
        request.setPassword("admin123");

        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initializeAdmin_emptyRequest_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initializeAdmin_invalidContentType_shouldReturnUnsupportedMediaType() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setPassword("admin123");

        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void initializeAdmin_malformedJson_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }
}
