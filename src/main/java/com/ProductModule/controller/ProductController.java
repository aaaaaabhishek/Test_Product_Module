package com.ProductModule.controller;

import com.ProductModule.Exception.InvalidProductIdException;
import com.ProductModule.Exception.ProductNotFoundException;
import com.ProductModule.payLoad.ProductDto;
import com.ProductModule.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private static Logger logger = LoggerFactory.getLogger(ProductController.class);
    public final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/createUser")
     public ResponseEntity<Object> saveProduct(@RequestBody @Valid ProductDto productDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = getErrorMessages(bindingResult);
            logger.error(" validation error occurred ProductDto:{} ,error:{}", productDto, errors);
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        ProductDto addproduct = productService.createProduct(productDto);
        if (addproduct != null) {
            logger.info("crete product is Successful with productDto:{}", productDto);
            return new ResponseEntity<>(addproduct, HttpStatus.CREATED);
        }
        logger.error("Failed to crete product with productDto:{}", productDto);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to crete product");

    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) throws ProductNotFoundException, InvalidProductIdException {
        ProductDto productDto = productService.getProductById(id);
        if (productDto != null) {
            logger.info("Product  fetched by id:{}", id);
            return new ResponseEntity<>(productDto, HttpStatus.OK);
        }
        logger.error("Product is not found for id :{}" , id);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
//       return   ResponseEntity.badRequest().build();//it is good approach not usr null
//        return productService.getProductById(id)
//                .map(productDto -> {
//                    logger.info("Product fetched by id: {}", id);
//                    return new ResponseEntity<>(productDto, HttpStatus.OK);
//                })
//                .orElseGet(() -> {
//                    logger.error("Product is not found for id: {}", id);
//                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
//                });
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ProductDto>> getAllProduct() {
        List<ProductDto> productDtos = productService.getAllProduct();
        logger.info("Fetched ProductDto list  from database . Total count :{}", productDtos.size());
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        if (id == null || id < 0) {
            return new ResponseEntity<>("Product ID must not be null and cannot be negative: " + id, HttpStatus.BAD_REQUEST);
        }
        try {
            productService.deleteById(id);
            return new ResponseEntity<>("Product deleted successfully with product ID: " + id, HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@RequestBody @Valid ProductDto productDto, @PathVariable("id") Long productId, BindingResult bindingResult) throws InvalidProductIdException {
        if (bindingResult.hasErrors()) {
            List<String> errors =getErrorMessages(bindingResult);
            logger.error("Validation error occur with ProductDto:{},error:{} ", productDto, errors);
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        ProductDto updateProductdto = productService.updateRecord(productDto, productId);
        return new ResponseEntity<>(updateProductdto, HttpStatus.OK);
    }

    @PatchMapping("/partialUpdate/{id}")
    public ResponseEntity<Object> partialUpdate(@RequestBody ProductDto productDto, @PathVariable Long id) {
        try {
            ProductDto partialUpdateProductDto = productService.partialUpdate(productDto, id);
            logger.info("Partially update is successfully Done");
            return new ResponseEntity<>(partialUpdateProductDto, HttpStatus.OK);
        } catch (ProductNotFoundException productNotFoundException) {
            logger.error("Product is not Found:{}", productNotFoundException.getMessage());
            return new ResponseEntity<>(productNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private List<String> getErrorMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

    }

}
