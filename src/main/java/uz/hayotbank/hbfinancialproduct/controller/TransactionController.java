package uz.hayotbank.hbfinancialproduct.controller;

import uz.hayotbank.hbfinancialproduct.dto.TransactionCreateDto;
import uz.hayotbank.hbfinancialproduct.dto.TransactionResponseDto;
import uz.hayotbank.hbfinancialproduct.dto.TransferDto;
import uz.hayotbank.hbfinancialproduct.entity.TransactionStatus;
import uz.hayotbank.hbfinancialproduct.entity.TransactionType;
import uz.hayotbank.hbfinancialproduct.exception.TransactionNotFoundException;
import uz.hayotbank.hbfinancialproduct.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(
        @Valid @RequestBody TransactionCreateDto transactionCreateDto) {
        TransactionResponseDto transaction = transactionService.createTransaction(transactionCreateDto);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable Long id) {
        TransactionResponseDto transaction = transactionService.getTransactionById(id)
            .orElseThrow(() -> new TransactionNotFoundException(id));
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponseDto>> getAllTransactions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponseDto> transactions = transactionService.getAllTransactions(pageable);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByUserId(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponseDto> transactions = transactionService.getTransactionsByUserId(userId, pageable);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByStatus(
        @PathVariable TransactionStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponseDto> transactions = transactionService.getTransactionsByStatus(status, pageable);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByUserIdAndDateRange(
        @PathVariable Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponseDto> transactions = transactionService
            .getTransactionsByUserIdAndDateRange(userId, startDate, endDate, pageable);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByType(
        @PathVariable TransactionType type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponseDto> transactions = transactionService.getTransactionsByType(type, pageable);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByUserIdAndType(
        @PathVariable Long userId,
        @PathVariable TransactionType type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponseDto> transactions = transactionService
            .getTransactionsByUserIdAndType(userId, type, pageable);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsWithFilters(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) TransactionType type,
        @RequestParam(required = false) TransactionStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponseDto> transactions = transactionService
            .getTransactionsWithFilters(userId, type, status, startDate, endDate, pageable);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transferMoney(
        @Valid @RequestBody TransferDto transferDto) {
        TransactionResponseDto transaction = transactionService.transferMoney(transferDto);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }
}