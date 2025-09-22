package uz.hayotbank.hbfinancialproduct.service;

import uz.hayotbank.hbfinancialproduct.dto.TransactionCreateDto;
import uz.hayotbank.hbfinancialproduct.dto.TransactionResponseDto;
import uz.hayotbank.hbfinancialproduct.dto.TransferDto;
import uz.hayotbank.hbfinancialproduct.entity.Transaction;
import uz.hayotbank.hbfinancialproduct.entity.TransactionStatus;
import uz.hayotbank.hbfinancialproduct.entity.TransactionType;
import uz.hayotbank.hbfinancialproduct.entity.User;
import uz.hayotbank.hbfinancialproduct.exception.InsufficientBalanceException;
import uz.hayotbank.hbfinancialproduct.exception.InvalidTransferException;
import uz.hayotbank.hbfinancialproduct.exception.TransactionNotFoundException;
import uz.hayotbank.hbfinancialproduct.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public TransactionService(TransactionRepository transactionRepository,
                             UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    public TransactionResponseDto createTransaction(TransactionCreateDto transactionCreateDto) {
        User user = userService.findEntityById(transactionCreateDto.getUserId());

        // For DEBIT transactions, check if user has sufficient balance
        if (transactionCreateDto.getType() == TransactionType.DEBIT) {
            BigDecimal currentBalance = userService.calculateBalance(user.getId());
            if (currentBalance.compareTo(transactionCreateDto.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: %s, Required: %s",
                        currentBalance, transactionCreateDto.getAmount()));
            }
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(transactionCreateDto.getAmount());
        transaction.setType(transactionCreateDto.getType());
        transaction.setDescription(transactionCreateDto.getDescription());
        transaction.setStatus(TransactionStatus.PENDING);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Process the transaction immediately (in real app, this might be async)
        processTransaction(savedTransaction.getId());

        Transaction refreshedTransaction = transactionRepository.findById(savedTransaction.getId())
            .orElseThrow(() -> new TransactionNotFoundException(savedTransaction.getId()));
        return convertToResponseDto(refreshedTransaction);
    }

    public void processTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Transaction already processed");
        }

        try {
            User user = transaction.getUser();

            // For DEBIT transactions, check balance again (in case it changed)
            if (transaction.getType() == TransactionType.DEBIT) {
                BigDecimal currentBalance = userService.calculateBalance(user.getId());
                if (currentBalance.compareTo(transaction.getAmount()) < 0) {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setProcessedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    throw new InsufficientBalanceException(
                        String.format("Insufficient balance during processing. Available: %s, Required: %s",
                            currentBalance, transaction.getAmount()));
                }
            }

            // Mark transaction as completed
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            throw e;
        }
    }

    public Optional<TransactionResponseDto> getTransactionById(Long id) {
        return transactionRepository.findById(id)
            .map(this::convertToResponseDto);
    }

    public Page<TransactionResponseDto> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
            .map(this::convertToResponseDto);
    }

    public Page<TransactionResponseDto> getTransactionsByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
            .map(this::convertToResponseDto);
    }

    public Page<TransactionResponseDto> getTransactionsByStatus(TransactionStatus status, Pageable pageable) {
        return transactionRepository.findByStatus(status, pageable)
            .map(this::convertToResponseDto);
    }

    public Page<TransactionResponseDto> getTransactionsByUserIdAndDateRange(
        Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable)
            .map(this::convertToResponseDto);
    }

    public Page<TransactionResponseDto> getTransactionsByType(TransactionType type, Pageable pageable) {
        return transactionRepository.findByType(type, pageable)
            .map(this::convertToResponseDto);
    }

    public Page<TransactionResponseDto> getTransactionsByUserIdAndType(Long userId, TransactionType type, Pageable pageable) {
        return transactionRepository.findByUserIdAndType(userId, type, pageable)
            .map(this::convertToResponseDto);
    }

    public Page<TransactionResponseDto> getTransactionsWithFilters(
        Long userId, TransactionType type, TransactionStatus status,
        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findWithFilters(userId, type, status, startDate, endDate, pageable)
            .map(this::convertToResponseDto);
    }

    @Transactional
    public TransactionResponseDto transferMoney(TransferDto transferDto) {
        // Validate that fromUserId and toUserId are different
        if (transferDto.getFromUserId().equals(transferDto.getToUserId())) {
            throw new InvalidTransferException("Cannot transfer money to the same user");
        }

        // Validate transfer amount
        if (transferDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Transfer amount must be greater than zero");
        }

        // Find both users
        User fromUser = userService.findEntityById(transferDto.getFromUserId());
        User toUser = userService.findEntityById(transferDto.getToUserId());

        // Check if sender has sufficient balance
        BigDecimal senderBalance = userService.calculateBalance(fromUser.getId());
        if (senderBalance.compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                String.format("Insufficient balance for transfer. Available: %s, Required: %s",
                    senderBalance, transferDto.getAmount()));
        }

        // Create single TRANSFER transaction
        Transaction transferTransaction = new Transaction();
        transferTransaction.setUser(fromUser);
        transferTransaction.setToUser(toUser);
        transferTransaction.setAmount(transferDto.getAmount());
        transferTransaction.setType(TransactionType.TRANSFER);
        transferTransaction.setDescription("Transfer from " + fromUser.getFullName() + " to " + toUser.getFullName() +
            (transferDto.getDescription() != null ? ": " + transferDto.getDescription() : ""));
        transferTransaction.setStatus(TransactionStatus.PENDING);

        try {
            // Save transaction
            Transaction savedTransaction = transactionRepository.save(transferTransaction);

            // Process transaction
            processTransferTransaction(savedTransaction);

            // Return the completed transaction
            Transaction refreshedTransaction = transactionRepository.findById(savedTransaction.getId())
                .orElseThrow(() -> new TransactionNotFoundException(savedTransaction.getId()));

            return convertToResponseDto(refreshedTransaction);

        } catch (Exception e) {
            // Mark transaction as failed if any error occurs
            transferTransaction.setStatus(TransactionStatus.FAILED);
            transferTransaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transferTransaction);

            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }
    }

    private void processTransferTransaction(Transaction transferTransaction) {
        try {
            // Verify sender still has sufficient balance
            BigDecimal senderBalance = userService.calculateBalance(transferTransaction.getUser().getId());
            if (senderBalance.compareTo(transferTransaction.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                    String.format("Insufficient balance during transfer processing. Available: %s, Required: %s",
                        senderBalance, transferTransaction.getAmount()));
            }

            // Mark transaction as completed
            transferTransaction.setStatus(TransactionStatus.COMPLETED);
            transferTransaction.setProcessedAt(LocalDateTime.now());

            transactionRepository.save(transferTransaction);

        } catch (Exception e) {
            // Mark transaction as failed
            transferTransaction.setStatus(TransactionStatus.FAILED);
            transferTransaction.setProcessedAt(LocalDateTime.now());

            transactionRepository.save(transferTransaction);

            throw e;
        }
    }

    private TransactionResponseDto convertToResponseDto(Transaction transaction) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser().getId());
        dto.setUserName(transaction.getUser().getFullName());

        // Set toUser information if it exists (for TRANSFER transactions)
        if (transaction.getToUser() != null) {
            dto.setToUserId(transaction.getToUser().getId());
            dto.setToUserName(transaction.getToUser().getFullName());
        }

        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDescription(transaction.getDescription());
        dto.setStatus(transaction.getStatus());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setProcessedAt(transaction.getProcessedAt());
        return dto;
    }
}