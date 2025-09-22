package uz.hayotbank.hbfinancialproduct.service;

import uz.hayotbank.hbfinancialproduct.dto.UserCreateDto;
import uz.hayotbank.hbfinancialproduct.dto.UserResponseDto;
import uz.hayotbank.hbfinancialproduct.entity.Transaction;
import uz.hayotbank.hbfinancialproduct.entity.TransactionStatus;
import uz.hayotbank.hbfinancialproduct.entity.TransactionType;
import uz.hayotbank.hbfinancialproduct.entity.User;
import uz.hayotbank.hbfinancialproduct.exception.UserNotFoundException;
import uz.hayotbank.hbfinancialproduct.repository.TransactionRepository;
import uz.hayotbank.hbfinancialproduct.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserCreateDto userCreateDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPassword("password123");

        userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("newuser");
        userCreateDto.setEmail("newuser@example.com");
        userCreateDto.setFullName("New User");
        userCreateDto.setPassword("password123");
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername(userCreateDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userCreateDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(transactionRepository.findByUserIdAndStatus(1L, TransactionStatus.COMPLETED))
                .thenReturn(Arrays.asList());

        UserResponseDto result = userService.createUser(userCreateDto);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists() {
        when(userRepository.existsByUsername(userCreateDto.getUsername())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userCreateDto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists() {
        when(userRepository.existsByUsername(userCreateDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userCreateDto.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userCreateDto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdAndStatus(1L, TransactionStatus.COMPLETED))
                .thenReturn(Arrays.asList());

        Optional<UserResponseDto> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getUsername(), result.get().getUsername());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserResponseDto> result = userService.getUserById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void calculateBalance_Success() {
        Transaction creditTransaction = new Transaction();
        creditTransaction.setAmount(new BigDecimal("1000.00"));
        creditTransaction.setType(TransactionType.CREDIT);

        Transaction debitTransaction = new Transaction();
        debitTransaction.setAmount(new BigDecimal("300.00"));
        debitTransaction.setType(TransactionType.DEBIT);

        List<Transaction> transactions = Arrays.asList(creditTransaction, debitTransaction);
        when(transactionRepository.findByUserIdAndStatus(1L, TransactionStatus.COMPLETED))
                .thenReturn(transactions);

        BigDecimal balance = userService.calculateBalance(1L);

        assertEquals(new BigDecimal("700.00"), balance);
    }

    @Test
    void calculateBalance_NoTransactions() {
        when(transactionRepository.findByUserIdAndStatus(1L, TransactionStatus.COMPLETED))
                .thenReturn(Arrays.asList());

        BigDecimal balance = userService.calculateBalance(1L);

        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void findEntityById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.findEntityById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    @Test
    void findEntityById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.findEntityById(1L);
        });
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });

        verify(userRepository, never()).deleteById(1L);
    }
}