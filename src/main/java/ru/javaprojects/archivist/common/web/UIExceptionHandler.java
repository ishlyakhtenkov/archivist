package ru.javaprojects.archivist.common.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import ru.javaprojects.archivist.common.error.Constants;
import ru.javaprojects.archivist.common.util.validation.ValidationUtil;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class UIExceptionHandler {

    //https://stackoverflow.com/questions/45080119/spring-rest-exception-handling-fileuploadbasesizelimitexceededexception
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public @ResponseBody ProblemDetail maxUploadSizeException(HttpServletRequest req, MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException at request {}", req.getRequestURI());
        ErrorResponse.Builder builder = ErrorResponse.builder(e, HttpStatus.UNPROCESSABLE_ENTITY,
                ValidationUtil.getRootCause(e).getMessage());
        return builder.build().getBody();
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) {
        log.error("Exception at request {}: {}", req.getRequestURL(), e.toString());
        Throwable rootCause = ValidationUtil.getRootCause(e);
        log.error(HttpStatus.INTERNAL_SERVER_ERROR + " at request {}: {}", req.getRequestURL(), rootCause.toString());
        String message = rootCause.getMessage();
        if (e.getClass().isAssignableFrom(DataIntegrityViolationException.class)) {
            message = Constants.getDbConstraintMessage(message).orElse(message);
        }
        ModelAndView mav = new ModelAndView("error/exception",
                Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "typeMessage", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "message", message));
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }
}
