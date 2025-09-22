package uz.hayotbank.hbfinancialproduct.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import uz.hayotbank.hbfinancialproduct.dto.TransactionCreateDto;
import uz.hayotbank.hbfinancialproduct.entity.Transaction;
import uz.hayotbank.hbfinancialproduct.entity.TransactionStatus;
import uz.hayotbank.hbfinancialproduct.entity.TransactionType;
import uz.hayotbank.hbfinancialproduct.entity.User;
import uz.hayotbank.hbfinancialproduct.repository.TransactionRepository;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // Create a completed credit transaction to give user balance
        testTransaction = new Transaction();
        testTransaction.setUser(testUser);
        testTransaction.setAmount(new BigDecimal("1000.00"));
        testTransaction.setType(TransactionType.CREDIT);
        testTransaction.setDescription("Initial deposit");
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction = transactionRepository.save(testTransaction);
    }

    @Test
    @WithMockUser
    void createTransaction_Credit_Success() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setUserId(testUser.getId());
        createDto.setAmount(new BigDecimal("500.00"));
        createDto.setType(TransactionType.CREDIT);
        createDto.setDescription("Test credit transaction");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser
    void createTransaction_Debit_Success() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setUserId(testUser.getId());
        createDto.setAmount(new BigDecimal("200.00"));
        createDto.setType(TransactionType.DEBIT);
        createDto.setDescription("Test debit transaction");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser
    void createTransaction_InsufficientBalance() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setUserId(testUser.getId());
        createDto.setAmount(new BigDecimal("2000.00")); // More than available balance
        createDto.setType(TransactionType.DEBIT);
        createDto.setDescription("Test debit transaction");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    @WithMockUser
    void getTransactionById_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", testTransaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTransaction.getId()))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.type").value("CREDIT"));
    }

    @Test
    @WithMockUser
    void getTransactionById_NotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: 999"));
    }

    @Test
    @WithMockUser
    void getAllTransactions_Success() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void getTransactionsByUserId_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/user/{userId}", testUser.getId())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void getTransactionsByType_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/type/{type}", "CREDIT")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser
    void getTransactionsWithFilters_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/filter")
                .param("userId", testUser.getId().toString())
                .param("type", "CREDIT")
                .param("status", "COMPLETED")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}