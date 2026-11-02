package dev.sahilpreet.commercecore.merchant.api;

import dev.sahilpreet.commercecore.merchant.application.MerchantNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MerchantExceptionHandler {

    @ExceptionHandler(MerchantNotFoundException.class)
    public ProblemDetail handleMerchantNotFoundException(MerchantNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
