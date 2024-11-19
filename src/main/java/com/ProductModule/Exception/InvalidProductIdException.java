package com.ProductModule.Exception;

import org.springframework.http.HttpStatus;

public class InvalidProductIdException extends Exception{
    public HttpStatus httpStatus;
    public InvalidProductIdException(String message,HttpStatus httpStatus){
        super(message);
        this.httpStatus=httpStatus;
    }
    public HttpStatus getHttpStatus(){
        return httpStatus;
    }

}
