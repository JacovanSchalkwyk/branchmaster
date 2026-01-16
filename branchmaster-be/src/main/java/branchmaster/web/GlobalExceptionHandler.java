package branchmaster.web;

import branchmaster.web.exception.NoAvailableResourceException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(NoAvailableResourceException.class)
  public ResponseEntity<Object> handleNoAvailableResource(
      final NoAvailableResourceException ex, final WebRequest request) {
    log.warn("No available resource: {}", ex.getMessage());
    Map<String, Object> body = baseBody(HttpStatus.CONFLICT, request);
    body.put("error", "NO_AVAILABLE_RESOURCE");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> badRequest(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrityViolation(
      final DataIntegrityViolationException ex, final WebRequest request) {
    log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
    Map<String, Object> body = baseBody(HttpStatus.CONFLICT, request);
    body.put("error", "CONFLICT");
    body.put("message", "Request could not be completed due to a conflict.");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<Object> handleIllegalArgument(
      final IllegalArgumentException ex, final WebRequest request) {
    log.warn("Bad request: {}", ex.getMessage());
    Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, request);
    body.put("error", "BAD_REQUEST");
    body.put("message", ex.getMessage());
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleUnhandled(final Exception ex, final WebRequest request) {
    log.error("Unhandled error: {}", ex.getMessage(), ex);
    Map<String, Object> body = baseBody(HttpStatus.INTERNAL_SERVER_ERROR, request);
    body.put("error", "INTERNAL_SERVER_ERROR");
    body.put("message", "Unexpected error occurred.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private Map<String, Object> baseBody(HttpStatus status, WebRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", status.value());
    body.put("path", extractPath(request));
    return body;
  }

  private String extractPath(WebRequest request) {
    String desc = request.getDescription(false);
    if (desc.startsWith("uri=")) return desc.substring("uri=".length());
    return desc;
  }
}
