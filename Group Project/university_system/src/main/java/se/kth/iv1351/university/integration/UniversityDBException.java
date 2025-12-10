package se.kth.iv1351.university.integration;

public class UniversityDBException extends Exception {
    public UniversityDBException(String reason) {
        super(reason);
    }
    public UniversityDBException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}