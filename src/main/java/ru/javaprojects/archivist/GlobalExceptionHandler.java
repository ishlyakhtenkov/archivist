package ru.javaprojects.archivist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import ru.javaprojects.archivist.util.validation.ValidationUtil;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) {
        log.error("Exception at request {}: {}", req.getRequestURL(), e.toString());
        Throwable rootCause = ValidationUtil.getRootCause(e);
        log.error(HttpStatus.INTERNAL_SERVER_ERROR + " at request {}: {}", req.getRequestURL(), rootCause.toString());
        ModelAndView mav = new ModelAndView("exception",
                Map.of("exception", rootCause, "message", rootCause.getMessage(),
                        "typeMessage", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "status", HttpStatus.INTERNAL_SERVER_ERROR));
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }
}
