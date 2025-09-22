package uz.hayotbank.hbfinancialproduct.exception;

public class InvalidTransferException extends RuntimeException {
    public InvalidTransferException(String message) {
        super(message);
    }
}