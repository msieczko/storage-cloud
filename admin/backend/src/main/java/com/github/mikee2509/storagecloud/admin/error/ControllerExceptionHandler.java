package com.github.mikee2509.storagecloud.admin.error;

import com.github.mikee2509.storagecloud.admin.domain.exception.FileServiceException;
import com.github.mikee2509.storagecloud.admin.domain.exception.ParsingException;
import com.github.mikee2509.storagecloud.admin.domain.exception.SocketConnectionException;
import com.github.mikee2509.storagecloud.admin.domain.exception.UserServiceException;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { FileServiceException.class })
    protected ResponseEntity<Object> handleFileServiceException(final FileServiceException ex,
                                                                final WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = { UserServiceException.class })
    protected ResponseEntity<Object> handleUserServiceException(final UserServiceException ex,
                                                                final WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class, IllegalStateException.class,
            SocketConnectionException.class, ParsingException.class})
    public ResponseEntity<Object> handleInternal(final RuntimeException ex, final WebRequest request) {
        log.severe("500 Status Code");
        final String bodyOfResponse = "Something went wrong...";
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
