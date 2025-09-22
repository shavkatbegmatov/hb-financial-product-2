package uz.hayotbank.hbfinancialproduct.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransferDto {

    @NotNull(message = "From User ID is required")
    private Long fromUserId;

    @NotNull(message = "To User ID is required")
    private Long toUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;

    // Constructors
    public TransferDto() {}

    public TransferDto(Long fromUserId, Long toUserId, BigDecimal amount, String description) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.description = description;
    }

    // Getters and Setters
    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}