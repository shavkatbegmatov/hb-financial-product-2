package uz.hayotbank.hbfinancialproduct.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import uz.hayotbank.hbfinancialproduct.dto.UserCreateDto;
import uz.hayotbank.hbfinancialproduct.entity.User;
import uz.hayotbank.hbfinancialproduct.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);
    }

    @Test
    void createUser_Success() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername("newuser");
        createDto.setEmail("newuser@example.com");
        createDto.setFullName("New User");
        createDto.setPassword("password123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("New User"))
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    void createUser_DuplicateUsername() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername("testuser"); // Already exists
        createDto.setEmail("different@example.com");
        createDto.setFullName("Different User");
        createDto.setPassword("password123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @WithMockUser
    void getUserById_Success() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void getUserById_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    @WithMockUser
    void getAllUsers_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void updateUser_Success() throws Exception {
        UserCreateDto updateDto = new UserCreateDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@example.com");
        updateDto.setFullName("Updated User");
        updateDto.setPassword("newpassword123");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNoContent());
    }
}