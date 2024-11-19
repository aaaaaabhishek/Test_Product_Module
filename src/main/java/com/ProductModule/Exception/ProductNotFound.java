package com.ProductModule.Exception;

import org.springframework.http.HttpStatus;

public class ProductNotFound extends RuntimeException{
    public HttpStatus httpStatus;
    public ProductNotFound(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus=httpStatus;
    }
    public HttpStatus getHttpStatus(){
        return httpStatus;
    }
}
