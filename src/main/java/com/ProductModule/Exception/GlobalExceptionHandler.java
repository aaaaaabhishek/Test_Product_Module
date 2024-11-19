package com.ProductModule.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> getProductNotFoundException(ProductNotFoundException productNotFoundException){
        logger.error("Product not found: {}", productNotFoundException.getMessage());
        return new ResponseEntity<>(productNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(MappingException.class)
    public ResponseEntity<String> getProductNotFoundException(MappingException mappingException){
        logger.error("Mapping  conversion is failed: {}", mappingException.getMessage());
        return new ResponseEntity<>(mappingException.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(InvalidProductIdException.class)
    public ResponseEntity<String> getInvalidProductIdException(InvalidProductIdException invalidProductIdException){
        logger.error("Product id can't null and negative:{}"+invalidProductIdException.getMessage());
        return new ResponseEntity<>(invalidProductIdException.getMessage(),HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidProductDataException.class)
    public ResponseEntity<String> getInvalidProductDataException(InvalidProductDataException invalidProductDataException){
        logger.error("ProductDto is empty:{}",invalidProductDataException.getMessage());
        return new ResponseEntity<>(invalidProductDataException.getMessage(),HttpStatus.BAD_REQUEST);
    }
}
