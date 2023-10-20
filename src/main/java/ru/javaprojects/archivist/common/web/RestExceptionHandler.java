package ru.javaprojects.archivist.common.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.javaprojects.archivist.common.error.Constants;
import ru.javaprojects.archivist.common.error.DataConflictException;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.common.util.validation.ValidationUtil;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(annotations = RestController.class)
@Slf4j
public class RestExceptionHandler {
    private static final Map<Class<?>, HttpStatus> HTTP_STATUS_MAP = Map.of(
            EntityNotFoundException.class, HttpStatus.CONFLICT,
            DataIntegrityViolationException.class, HttpStatus.CONFLICT,
            IllegalRequestDataException.class, HttpStatus.UNPROCESSABLE_ENTITY,
            ConstraintViolationException.class, HttpStatus.UNPROCESSABLE_ENTITY,
            IllegalArgumentException.class, HttpStatus.UNPROCESSABLE_ENTITY,
            NotFoundException.class, HttpStatus.NOT_FOUND,
            DataConflictException.class, HttpStatus.CONFLICT
    );

    @ExceptionHandler(BindException.class)
    public ProblemDetail bindException(BindException ex, HttpServletRequest request) {
        Map<String, String> invalidParams = new LinkedHashMap<>();
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            invalidParams.put(error.getObjectName(), error.getDefaultMessage());
        }
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            invalidParams.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("BindingException: {} at request {}", invalidParams, request.getRequestURI());
        ProblemDetail problemDetail = createProblemDetail(ex, HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setProperty("invalid_params", invalidParams);
        return problemDetail;
    }


    //   https://howtodoinjava.com/spring-mvc/spring-problemdetail-errorresponse/#5-adding-problemdetail-to-custom-exceptions
    @ExceptionHandler(Exception.class)
    public ProblemDetail exception(Exception ex, HttpServletRequest request) {
        HttpStatus status = HTTP_STATUS_MAP.get(ex.getClass());
        if (status != null) {
            log.error("Exception: {} at request {}", ex, request.getRequestURI());
            String message = ex.getMessage();
            if (status == HttpStatus.CONFLICT) {
                message = Constants.getDbConstraintMessage(message).orElse(message);
            }
            return createProblemDetail(ex, status, message);
        } else {
            Throwable root = ValidationUtil.getRootCause(ex);
            log.error("Exception " + root + " at request " + request.getRequestURI(), root);
            return createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, root.getClass().getName());
        }
    }

    private ProblemDetail createProblemDetail(Exception ex, HttpStatusCode statusCode, String detail) {
        ErrorResponse.Builder builder = ErrorResponse.builder(ex, statusCode, detail);
        return builder.build().getBody();
    }
}
