package com.ProductModule.Exception;

import org.springframework.http.HttpStatus;

public class InvalidProductDataException extends RuntimeException{
    public HttpStatus httpStatus;
    public InvalidProductDataException(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus=httpStatus;
    }
    public HttpStatus getHttpStatus(){
        return httpStatus;
    }
}
