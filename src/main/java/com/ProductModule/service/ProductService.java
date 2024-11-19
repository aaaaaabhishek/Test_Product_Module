package com.ProductModule.service;

import com.ProductModule.Entity.Product;
import com.ProductModule.Exception.InvalidProductDataException;
import com.ProductModule.Exception.InvalidProductIdException;
import com.ProductModule.Exception.ProductNotFoundException;
import com.ProductModule.Repository.ProductRepository;
import com.ProductModule.payLoad.ProductDto;
import jakarta.transaction.Transactional;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private ModelMapper modelMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        if (productDto == null) {
            logger.error("Failed to create product, productDto is null.");
            throw new IllegalArgumentException("ProductDto must not be null");
        }
        Product product = convertToEntity(productDto);
        long min = 1000L;
        long max = 10000L;

        long randomLong = ThreadLocalRandom.current().nextLong(min, max);
        product.setProduct_id(randomLong);
        Product savedProduct = productRepository.save(product);
        logger.info("Product saved in the database: {}", savedProduct);
        return convertToDto(savedProduct);
    }

    public ProductDto getProductById(Long product_id) throws ProductNotFoundException, InvalidProductIdException {
        if (product_id == null && product_id <= -1) {
            logger.error("Invalid product id:{}" , product_id);
            throw new InvalidProductIdException("Invalid productId it can't be null and negative", HttpStatus.BAD_REQUEST);
        }
        Product product = productRepository.findById(product_id)
                .orElseThrow(() -> {
                    logger.error("Product id is not found: {}", product_id);
                    return new ProductNotFoundException("Product with ID " + product_id + " not found");
                });
        return convertToDto(product);
    }

    /**
     * Converting Dto  to Entity
     *
     * @param product
     * @return
     */
    public ProductDto convertToDto(Product product) {
        if (product == null) {
            logger.error("Cannot convert null Product to ProductDto");
            return null; // Alternatively, you could throw an exception here
        }
        try {
            ProductDto productDto = modelMapper.map(product, ProductDto.class);
            logger.info("Converted Product to ProductDto: {}", productDto);
            return productDto;
        } catch (MappingException e) {
            logger.error("Mapping failed: {}", e.getMessage());
            throw new com.ProductModule.Exception.MappingException("Failed to map Product to ProductDto " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during mapping: {}", e.getMessage());
            throw new RuntimeException("Unexpected error during mapping", e);
        }

    }

    /**
     * Converting Entity to Dto
     *
     * @param productDto
     * @return
     */

    public Product convertToEntity(ProductDto productDto) {
        if (productDto == null) {
            logger.error("Cannot convert null ProductDto to Product");
           throw new InvalidProductDataException("Cannot convert null ProductDto to Product",HttpStatus.BAD_REQUEST); // Alternatively, you could throw an exception here
        }
        try {
            Product product = modelMapper.map(productDto, Product.class);
            logger.info("Converted ProductDto to Product: {}", product);
            return product;

        } catch (MappingException e) {
            logger.error("Mapping failed: {}", e.getMessage());
            throw new com.ProductModule.Exception.MappingException("Failed to map Product to ProductDto" + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during mapping: {}", e.getMessage());
            throw new RuntimeException("Unexpected error during mapping", e);
        }
    }

    public List<ProductDto> getAllProduct() {
        List<Product> productsList = productRepository.findAll();
        List<ProductDto> productDtoList = productsList
                .stream().map(this::convertToDto)
                .collect(Collectors.toList());
        logger.info("Converted all products to ProductDto. Total count: {}", productDtoList.size());
        return productDtoList;
    }

    public void deleteById(Long id) throws ProductNotFoundException {
        if (!productRepository.existsById(id)) {
            logger.error("Product is not found for this productId:{}", id);
            throw new ProductNotFoundException("Product with ID " + id + " not found.");
        }
        productRepository.deleteById(id);
        logger.info("Product is deleted for this productId:{}", id);
    }

    public ProductDto updateRecord(ProductDto productDto, Long productId) throws InvalidProductIdException {
        if (productId == null || productId < 0) {
            logger.error("ProductId can't be null and Negative:{}", productId);
            throw new InvalidProductIdException("ProductId can't be null and Negative" + productId, HttpStatus.BAD_REQUEST);
        }
        if (productDto==null ){
            throw new InvalidProductDataException("Product data not contain anything",HttpStatus.BAD_REQUEST);
        }
        if (!productRepository.existsById(productId)) {
            logger.error("Product  not found for productId:{}" , productId);
            throw new InvalidProductIdException("Product with ID " + productId + " not found.", HttpStatus.NOT_FOUND);
        }
        Product product = convertToEntity(productDto);
        product.setProduct_id(productId);
        Product updateProduct = productRepository.save(product);
        if (updateProduct == null) {
            logger.error("Product is can't be null with this product:{}", updateProduct);
        }
        return convertToDto(updateProduct);
    }

    public ProductDto partialUpdate(ProductDto productDto, Long id) throws InvalidProductIdException, ProductNotFoundException {
        if (id == null || id < 0) {
            logger.error("Invalid productID it can't null and negative :{}", id);
            throw new InvalidProductIdException("Invalid productID it can't null and negative" + id, HttpStatus.BAD_REQUEST);
        }
        if (productDto == null) {
            logger.error("Invalid productDto .It is Empty:{}");
            throw new InvalidProductDataException("Invalid productDto .It is Empty:{}", HttpStatus.BAD_REQUEST);
        }
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product is not Found with productId:"+id);
        }

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    // Log the error before throwing the exception
                    logger.error("Product not found with ID: {}", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });
        Optional.ofNullable(productDto.getProduct_id()).ifPresent(newId -> {
            logger.info("Updating product ID from {} to {}", existingProduct.getProduct_id(), newId);
            existingProduct.setProduct_id(newId);
        });

        Optional.ofNullable(productDto.getProduct_name()).ifPresent(newName -> {
            logger.info("Updating product name from '{}' to '{}'", existingProduct.getProduct_name(), newName);
            existingProduct.setProduct_name(newName);
        });

        Optional.ofNullable(productDto.getPrice()).ifPresent(newPrice -> {
            logger.info("Updating product price from {} to {}", existingProduct.getPrice(), newPrice);
            existingProduct.setPrice(newPrice);
        });
        Product partialUpdateDto = productRepository.save(existingProduct);
        logger.info("Partially Update data with productDto:{}", productDto);
        return convertToDto(partialUpdateDto);
    }
}
