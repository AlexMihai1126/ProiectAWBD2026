package ro.fmi.awbd.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ro.fmi.awbd.exception.BadRequestException;
import ro.fmi.awbd.exception.DuplicateResourceException;
import ro.fmi.awbd.exception.ResourceNotFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return "forward:/access_denied";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Not found: {}", ex.getMessage());
        return render(model, 404, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicate(DuplicateResourceException ex, Model model) {
        log.warn("Conflict: {}", ex.getMessage());
        return render(model, 409, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(BadRequestException ex, Model model) {
        log.warn("Bad request: {}", ex.getMessage());
        return render(model, 400, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleIntegrity(DataIntegrityViolationException ex, Model model) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return render(model, 409, "Conflict",
                "The operation conflicts with existing data (it may still be referenced elsewhere).");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) throws Exception {
        if (ex instanceof AccessDeniedException accessDenied) {
            throw accessDenied;
        }
        log.error("Unhandled exception", ex);
        return render(model, 500, "Internal Server Error", "Something went wrong. Please try again later.");
    }

    private String render(Model model, int status, String error, String message) {
        model.addAttribute("status", status);
        model.addAttribute("error", error);
        model.addAttribute("message", message);
        return "error/error";
    }
}
