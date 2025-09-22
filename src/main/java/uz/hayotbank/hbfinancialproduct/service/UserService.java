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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                      TransactionRepository transactionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setFullName(userCreateDto.getFullName());
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));

        User savedUser = userRepository.save(user);
        return convertToResponseDtoWithCalculatedBalance(savedUser);
    }

    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
            .map(this::convertToResponseDtoWithCalculatedBalance);
    }

    public Optional<UserResponseDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(this::convertToResponseDtoWithCalculatedBalance);
    }

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(this::convertToResponseDtoWithCalculatedBalance);
    }

    public Page<UserResponseDto> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.findBySearchTerm(searchTerm, pageable)
            .map(this::convertToResponseDtoWithCalculatedBalance);
    }

    public UserResponseDto updateUser(Long id, UserCreateDto userCreateDto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        // Check if username is being changed and if new username already exists
        if (!user.getUsername().equals(userCreateDto.getUsername()) &&
            userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(userCreateDto.getEmail()) &&
            userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setFullName(userCreateDto.getFullName());

        User updatedUser = userRepository.save(user);
        return convertToResponseDtoWithCalculatedBalance(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }



    public User findEntityById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User findEntityByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("username", username));
    }

    public BigDecimal calculateBalance(Long userId) {
        List<Transaction> completedTransactions = transactionRepository
                .findByUserIdAndStatus(userId, TransactionStatus.COMPLETED);

        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction transaction : completedTransactions) {
            if (transaction.getType() == TransactionType.CREDIT) {
                balance = balance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.DEBIT) {
                balance = balance.subtract(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.TRANSFER) {
                // For TRANSFER: if user is sender - subtract, if receiver - add
                if (transaction.getUser().getId().equals(userId)) {
                    // User is sender (fromUser) - subtract amount
                    balance = balance.subtract(transaction.getAmount());
                }
                // If user is receiver (toUser), amount is added through the paired transaction
                // where the receiver is the main user of that transaction
            }
        }

        // Also check transactions where this user is the receiver (toUser)
        List<Transaction> receivedTransactions = transactionRepository
                .findByToUserIdAndStatusAndType(userId, TransactionStatus.COMPLETED, TransactionType.TRANSFER);

        for (Transaction transaction : receivedTransactions) {
            // User is receiver - add amount
            balance = balance.add(transaction.getAmount());
        }

        return balance;
    }

    private UserResponseDto convertToResponseDtoWithCalculatedBalance(User user) {
        BigDecimal calculatedBalance = calculateBalance(user.getId());
        return new UserResponseDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            calculatedBalance,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}