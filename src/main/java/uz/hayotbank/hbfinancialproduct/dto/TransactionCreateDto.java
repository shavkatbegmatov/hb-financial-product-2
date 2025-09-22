package uz.hayotbank.hbfinancialproduct.dto;

import uz.hayotbank.hbfinancialproduct.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransactionCreateDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    private String description;

    // Constructors
    public TransactionCreateDto() {}

    public TransactionCreateDto(Long userId, BigDecimal amount, TransactionType type, String description) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}