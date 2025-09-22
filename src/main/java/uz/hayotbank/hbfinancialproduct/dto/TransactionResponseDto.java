package uz.hayotbank.hbfinancialproduct.dto;

import uz.hayotbank.hbfinancialproduct.entity.TransactionStatus;
import uz.hayotbank.hbfinancialproduct.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long toUserId;
    private String toUserName;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    // Constructors
    public TransactionResponseDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public String getToUserName() { return toUserName; }
    public void setToUserName(String toUserName) { this.toUserName = toUserName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}