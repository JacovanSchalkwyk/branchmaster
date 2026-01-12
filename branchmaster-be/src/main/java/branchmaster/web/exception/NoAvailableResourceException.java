package branchmaster.web.exception;

public class NoAvailableResourceException extends RuntimeException {

  public NoAvailableResourceException() {
    super("No available resource for the selected time slot.");
  }

  public NoAvailableResourceException(String message) {
    super(message);
  }
}
