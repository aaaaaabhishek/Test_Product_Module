package com.ProductModule.controllerTest;

import com.ProductModule.Entity.Product;
import com.ProductModule.Exception.InvalidProductDataException;
import com.ProductModule.Exception.InvalidProductIdException;
import com.ProductModule.Exception.ProductNotFoundException;
import com.ProductModule.Repository.ProductRepository;
import com.ProductModule.payLoad.ProductDto;
import com.ProductModule.service.ProductService;
import jakarta.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class TestProductService {
    private static final Logger logger = LoggerFactory.getLogger(TestProductService.class);
    @Mock
    public ProductRepository productRepository;
    @InjectMocks
    @Spy
    public ProductService productService;
    public MockMvc mockMvc;
    @Mock
    public ModelMapper modelMapper;

    @Before
    public void setup() {
        logger.info("Setting up test environment");
        this.mockMvc = MockMvcBuilders.standaloneSetup(productService).build();
    }

    ProductDto productdto = ProductDto.builder().product_id(1L).product_name("Apple").price("45").build();
    Product product = Product.builder().product_id(1L).product_name("Apple").price("45").build();

    @Test
    @Transactional
    public void test_adduser() {
        logger.info("Starting test for adding a user product");

        Mockito.when(modelMapper.map(productdto, Product.class)).thenReturn(product);
        Mockito.when(productRepository.save(product)).thenReturn(product);
        Mockito.when(modelMapper.map(product, ProductDto.class)).thenReturn(productdto);

        ProductDto result = productService.createProduct(productdto);
        logger.info("Product creation returned: {}", result);

        assertNotNull(result, "The returned ProductDto should not be null");
        assertEquals(result.getProduct_id(), productdto.getProduct_id());
        verify(modelMapper).map(productdto, Product.class);
        verify(productRepository).save(product);
        verify(modelMapper).map(product, ProductDto.class);
        assertEquals(productdto, result, "The returned ProductDto should match the expected one");
        logger.info("Test for adding a user product completed successfully");

    }

    @Test
    @Transactional
    public void test_getProductById() throws ProductNotFoundException, InvalidProductIdException {
        logger.info("Starting test getting user product by id");
        Mockito.when(productRepository.findById(product.getProduct_id())).thenReturn(Optional.ofNullable(product));
        Mockito.when(modelMapper.map(product, ProductDto.class)).thenReturn(productdto);

        ProductDto result = productService.getProductById(product.getProduct_id());
        logger.info("");
        assertNotNull(result, "Product result can't be null");
        assertEquals(product.getProduct_name(), result.getProduct_name(), "product name should be same");
        verify(modelMapper).map(product, ProductDto.class);
        verify(productRepository).findById(product.getProduct_id());
    }

    @Test
    @Transactional
    public void test_convertToEntity() {
        logger.info("Starting test convert  ProductDto to Product");
        Mockito.when(modelMapper.map(productdto, Product.class)).thenReturn(product);
        Product actualProduct = productService.convertToEntity(productdto);
        logger.info("converting ProductDto is successful");
        assertNotNull(actualProduct, "Product result can't be null");
        assertEquals(product.getProduct_id(), actualProduct.getProduct_id(), "The returned Product ID should match the expected one");
        assertEquals(product.getProduct_name(), actualProduct.getProduct_name(), "The returned Product name should match the expected one");
        verify(modelMapper).map(productdto, Product.class);
        verify(productService, times(1)).convertToEntity(productdto);
    }

    @Test
    @Transactional
    public void test_convertToEntity_nullDto() {
        ProductDto newproductDto = null;
        InvalidProductDataException exception = assertThrows(InvalidProductDataException.class, () -> productService.convertToEntity(null)
        );
        assertEquals("Cannot convert null ProductDto to Product", exception.getMessage());
    }

    @Test
    @Transactional
    public void test_getAllProducts() {
        logger.info("Starting  test to get getAllProducts");
        Product product2 = new Product();
        product2.setProduct_id(2L);
        product2.setProduct_name("Banana");
        product2.setPrice("30");

        ProductDto productDto2 = new ProductDto();
        productDto2.setProduct_id(2L);
        productDto2.setProduct_name("Banana");
        productDto2.setPrice("30");
        List<Product> productList = List.of(product, product2);
        List<ProductDto> productDtoList = List.of(productdto, productDto2);

        Mockito.when(productRepository.findAll()).thenReturn(productList);
        for (int i = 0; i < productList.size(); i++) {
            when(modelMapper.map(productList.get(i), ProductDto.class)).thenReturn(productDtoList.get(i));
        }
        List<ProductDto> actualProductlist = productService.getAllProduct();
        assertEquals(productdto.product_id, actualProductlist.get(0).product_id, "The return Product Id should match to expected one ");
        verify(productService).getAllProduct();//by using spy we can check this method was called or not
    }

    @Test
    @Transactional
    public void test_deleteById() throws ProductNotFoundException {
        logger.info("Starting test for deleting product by using Product ID");
        Mockito.when(productRepository.existsById(productdto.getProduct_id())).thenReturn(true);
        productService.deleteById(productdto.getProduct_id());
        logger.info("Product is successfully deleted by using Product Id");
        verify(productRepository).deleteById(productdto.getProduct_id());
    }

    @Test
    @Transactional
    public void test_deleteById_IfProduct_not_Exits() throws ProductNotFoundException {
        logger.info("Starting test for deleting product by using Product ID");
        Mockito.when(productRepository.existsById(productdto.getProduct_id())).thenReturn(false);
        ProductNotFoundException productNotFoundException = assertThrows(ProductNotFoundException.class, () -> productService.deleteById(productdto.getProduct_id()));
        assertEquals("Product with ID " + productdto.getProduct_id() + " not found.", productNotFoundException.getMessage());
        // Verify deleteById was never called because the product doesn't exist
        verify(productRepository, Mockito.never()).deleteById(productdto.getProduct_id());
    }

    @Test
    @Transactional
    public void test_updateRecord_whenProductDtoIsNull() {
        logger.info("Starting test for updating product with null ProductDto");
        ProductDto productDto = null; // Simulating a null ProductDto
        Long productId = 2L;

        InvalidProductDataException exception = assertThrows(
                InvalidProductDataException.class,
                () -> productService.updateRecord(productDto, productId)
        );

        assertEquals("Product data not contain anything", exception.getMessage());
        verify(productRepository, Mockito.never()).findById(2L);

    }

    @Test
    @Transactional
    public void test_updateRecord_whenProductDto_not_existsById() {
        logger.info("Starting test for updating product with not existId");
        InvalidProductIdException invalidProductIdException = assertThrows(InvalidProductIdException.class, () -> productService.updateRecord(productdto, 3L));
        assertEquals("Product with ID 3 not found.", invalidProductIdException.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, invalidProductIdException.getHttpStatus());
        verify(productRepository).existsById(3L);
        verify(productRepository, Mockito.never()).save(any(Product.class));
    }

    @Test
    @Transactional
    public void test_updateRecord() throws InvalidProductIdException {
        logger.info("Starting  test for Update");
        Mockito.when(productRepository.existsById(1l)).thenReturn(true);
        Mockito.when(modelMapper.map(productdto, Product.class)).thenReturn(product);
        Mockito.when(productRepository.save(product)).thenReturn(product); // Mock repository save behavior
        Mockito.when(modelMapper.map(product, ProductDto.class)).thenReturn(productdto);
        ProductDto updateProduct = productService.updateRecord(productdto, 1l);
        assertEquals(updateProduct.getProduct_id(), productdto.getProduct_id());
        logger.info("Product  is updated");
        assertNotNull(updateProduct, "Update project can't be null");
        verify(productRepository).existsById(1l);
        verify(modelMapper).map(productdto, Product.class);
        verify(productRepository).save(product);
        verify(modelMapper).map(product, ProductDto.class);

    }

    @Test
    @Transactional
    public void test_partialUpdate_Null_Id() throws InvalidProductIdException, ProductNotFoundException {
        logger.info("Starting of the Partial update with null Product Id");
        Long product_id = null;
        InvalidProductIdException invalidProductIdException = assertThrows(InvalidProductIdException.class, () -> productService.partialUpdate(productdto, product_id));
        assertEquals("Invalid productID it can't null and negative" + product_id, invalidProductIdException.getMessage());
        verify(productService).partialUpdate(productdto, product_id);

    }

    @Test
    @Transactional
    public void test_partialUpdate_Null_Product() throws InvalidProductIdException, ProductNotFoundException {
        logger.info("Starting of the Partial Update with Null Product");
        ProductDto productDto = null;
//        Mockito.when(productRepository.save(product)).thenReturn(product);
        InvalidProductDataException invalidProductDataException = assertThrows(InvalidProductDataException.class, () -> productService.partialUpdate(productDto, 2l));
        assertEquals("Invalid productDto .It is Empty:{}", invalidProductDataException.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, invalidProductDataException.getHttpStatus());
    }

    @Test
    @Transactional
    public void test_partialUpdate_Product_Not_FoundBy_Id() throws InvalidProductIdException {
        Long productId = 1L;
        logger.info("Starting test for partial update with non-existent product ID");
        // Arrange: Set up the mock to simulate that the product does not exist
//        Mockito.when(productRepository.findById(productId)).thenReturn(Optional.empty());
        // Act & Assert: Verify that calling partialUpdate throws ProductNotFoundException
        ProductNotFoundException productNotFoundException = assertThrows(ProductNotFoundException.class,
                () -> productService.partialUpdate(productdto, productId));

        // Assert: Check the exception message
        assertEquals("Product is not Found with productId:" + productId, productNotFoundException.getMessage());

        // Optionally, verify that no save operation was attempted
        verify(productRepository, Mockito.never()).save(any(Product.class));
    }


    @Test
    @Transactional
    public void test_PartialUpadete() throws ProductNotFoundException, InvalidProductIdException {
        Product product = Product.builder()
                .product_id(1L)
                .product_name("Banana")
                .price("30")
                .build();
        ProductDto productdto = ProductDto.builder()
                .product_id(1L)
                .product_name("Orange")
                .price("89")
                .build();

        Long productId = 1L;
        Mockito.when(productRepository.existsById(productId)).thenReturn(true); //You Can use This also by mocking This id is present
//        Mockito.when(modelMapper.map(productdto, Product.class)).thenReturn(product);
        Mockito.when(productRepository.findById(productdto.product_id)).thenReturn(Optional.ofNullable(product));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product)); // Mock repository fetch
        when(productRepository.save(product)).thenReturn(product); // Mock save operation
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productdto); // Convert back to dto
        ProductDto updatedProduct = productService.partialUpdate(productdto, productId);
        assertEquals(productdto.getProduct_name(), updatedProduct.getProduct_name(), "Product name Should be same");
        assertEquals(productdto.getPrice(), updatedProduct.getPrice(), "Product price Should be same");
        verify(productService).partialUpdate(productdto,productId);
    }
    @Test
    public void testPartialUpdate_SuccessfulUpdate() throws ProductNotFoundException, InvalidProductIdException {
        // Prepare DTO with partial updates
        ProductDto productDto = ProductDto.builder()
                .product_id(1L)
                .product_name("Orange") // New name
                .build();

        // Existing product to be partially updated
        Product existingProduct = Product.builder()
                .product_id(1L)
                .product_name("Banana")
                .price("30")
                .build();
        // Prepare DTO with partial updates
        ProductDto newProductDto = ProductDto.builder()
                .product_id(1L)
                .product_name("Orange") // New name
                .price("30")
                .build();

        Long productId = 1L;
        Product updatedProduct = new Product();
        // Mock repository behavior
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        // Mock the save method to return the updated product (reflecting the partial updates)
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
             // Create a new product instance for the update
            // Set the updated fields from productDto or keep the existing ones
            if (productDto.getProduct_name() != null) {
                updatedProduct.setProduct_name(productDto.getProduct_name());  // Update the name if present in the dto
            } else {
                updatedProduct.setProduct_name(existingProduct.getProduct_name());  // Otherwise, keep the existing one
            }

            if (productDto.getPrice() != null) {
                updatedProduct.setPrice(productDto.getPrice());  // Update the price if present in the dto
            } else {
                updatedProduct.setPrice(existingProduct.getPrice());  // Otherwise, keep the existing one
            }

            // Set product_id and other relevant fields
            updatedProduct.setProduct_id(existingProduct.getProduct_id());
            // Optionally set other fields here (if any)
            return updatedProduct;  // Return the updated product entity
        });

        // Mock modelMapper to return the ProductDto
        when(modelMapper.map(updatedProduct,ProductDto.class)).thenReturn(newProductDto);

        // Call the service method
        ProductDto updatedProductDto = productService.partialUpdate(productDto, productId);

        // Assertions
        assertEquals("Orange", updatedProductDto.getProduct_name(), "Product name should be updated");
        assertEquals("30", updatedProductDto.getPrice(), "Product price should remain the same");

        // Verify that the repository methods were called correctly
        verify(productRepository).existsById(productId);
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class)); // Verify save is called with the updated product

        // Verify that the product's price has not been updated (still "30")
        assertEquals("30", existingProduct.getPrice(), "Price should remain unchanged");
    }
    @Test
    public void testProductNotFoundById() {
        Long productId = 1L; // use any ID that would not exist in your mock data
        // Mock the findById method to return an empty Optional
//        Mockito.when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Verify that the exception is thrown when calling the method
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.partialUpdate(productdto,productId); // Call the service method
        });

        // Assert the exception message
        assertEquals("Product is not Found with productId:"+productId, exception.getMessage());
    }

}