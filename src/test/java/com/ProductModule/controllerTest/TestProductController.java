package com.ProductModule.controllerTest;
import com.ProductModule.Entity.Product;
import com.ProductModule.Exception.ProductNotFoundException;
import com.ProductModule.Repository.ProductRepository;
import com.ProductModule.controller.ProductController;
import com.ProductModule.payLoad.ProductDto;
import com.ProductModule.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class TestProductController {
    @Mock
    private ModelMapper mapper;
    @Mock
    public ProductRepository productRepository;
    @InjectMocks
    public ProductController productController;
    public MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Mock // Add this line to mock ProductService
    private ProductService productService;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

    }

    ProductDto productdto = ProductDto.builder().product_id(1L).product_name("Apple").price("45").build();
    Product product = Product.builder().product_id(1L).product_name("Apple").price("45").build();

//    @Test
//    @Transactional
//    public void testaddUser() throws Exception {
//        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(product);
//        mockMvc.perform(MockMvcRequestBuilders
//                        .post("/createUser").contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(product)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$.id", Matchers.is(1L)));
//    }

    @Test
    @Transactional
    public void testAddUser() throws Exception {
        // Mock the ProductService's behavior
        Mockito.when(productService.createProduct(Mockito.any(ProductDto.class))).thenReturn(productdto);

        // Perform the test
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/product/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productdto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.product_id").value(1L))
                .andExpect(jsonPath("$.product_name").value("Apple"))
                .andExpect(jsonPath("$.price").value("45"));
    }

    @Test
    @Transactional
    public void test_getProductById() throws ProductNotFoundException, JsonProcessingException, Exception {
        Mockito.when(productService.getProductById(productdto.getProduct_id())).thenReturn(productdto);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productdto)))
                .andExpect(jsonPath("$.product_id", Matchers.is(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product_name", Matchers.is("Apple")))
                .andReturn();
    }

    @Test
    @Transactional
    public void test_getAllProduct() throws JsonProcessingException, Exception {
        List<ProductDto> productList = List.of(productdto); // Wrap productdto in a list

        Mockito.when(productService.getAllProduct()).thenReturn(productList);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/product/getAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productdto)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].product_id", Matchers.is(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product_name", Matchers.is("Apple")))
                .andReturn();
    }

    @Test
    @Transactional
    public void test_deleteProductById() throws JsonProcessingException, Exception {
        Mockito.doNothing().when(productService).deleteById(1L);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productdto)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @Transactional
    public void test_deleteProductById_ProductNotFound() throws Exception {
        Mockito.doThrow(new ProductNotFoundException("Product with ID " + 1 + " not found"))
                .when(productService).deleteById(1L);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product with ID 1 not found"));
        // Optionally, verify that the service method was called
        Mockito.verify(productService).deleteById(1L);

    }

    /**
     * MockMvc will automatically replace {id} in the URL with the value of productId, ensuring the test matches the intended endpoint setup.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    public void test_partialUpdate() throws Exception {
        Long productId = 2L;
        ProductDto productDto = new ProductDto();
        productDto.setProduct_name("Apple");
        Mockito.when(productService.partialUpdate(productDto, productId)).thenReturn(productDto);
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/product/partialUpdate/{id}", productId)
                        .content(objectMapper.writeValueAsString(productDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.product_name", Matchers.is("Apple")))
                .andReturn();
    }

    @Test
    public void testPartialUpdate_ProductNotFound() throws Exception {
        Long productId = 1L;
        ProductDto productDto = new ProductDto();

        Mockito.when(productService.partialUpdate(productDto, productId)).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/product/partialUpdate/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Product not found"));
    }

    @Test
    public void testPartialUpdate_InternalServerError() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        Long productId = 1L;
        ProductDto productDto = new ProductDto();

        Mockito.when(productService.partialUpdate(productDto, productId)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/product/partialUpdate/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Unexpected error"));
    }


}
