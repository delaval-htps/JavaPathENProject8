package tourGuide.exception;

public class UserPreferencesException extends RuntimeException {
    
    private String message;

    public UserPreferencesException(String message) {
        super(message);
    }

}
