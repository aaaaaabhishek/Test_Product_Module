package com.ProductModule.IntegrationTest;

import com.ProductModule.Exception.InvalidProductDataException;
import com.ProductModule.Exception.InvalidProductIdException;
import com.ProductModule.Exception.ProductNotFoundException;
import com.ProductModule.ProductApplication;
import com.ProductModule.payLoad.ProductDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * .bodyToMono() is great for reactive workflows where you only care about the body of the response, and you can work with it asynchronously.
 * .toEntity() is useful when you need full access to the HTTP response, including status and headers, in addition to the response body.
 */
@SpringBootTest(classes = ProductApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.config.location=classpath:application-test.properties")
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProductIntegrationTest {
    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private String baseUrl;

    private String getBaseUrl() {
        if (baseUrl == null) {
            baseUrl = "http://localhost:" + port;
        }
        return baseUrl;
    }

    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    public JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void logCurrentProducts() {
        List<Map<String, Object>> products = jdbcTemplate.queryForList("SELECT * FROM Product");
        products.forEach(System.out::println);
    }

    public WebClient webClient;

    @Test
    void manualCheckDatabase() {
        logCurrentProducts();  // Manually call the logging method
        assertEquals(2, 1 + 1);
    }

    //    @Test
//    public void setUp() {
//        webClient = WebClient.builder().baseUrl("http://localhost:8080").build();
//
//    }
    @Test
    void test_getById() {
        ResponseEntity<ProductDto> response = restTemplate.exchange(getBaseUrl() + "/api/product/1"
                , HttpMethod.GET
                , null
                , ProductDto.class
        );
        assertNotNull(response);
        assertEquals(1, response.getBody().getProduct_id(), "Product Id must be same");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_Post() {
        // Create a ProductDto object with the necessary data
        ProductDto newProduct = new ProductDto();
        newProduct.setProduct_name("Test Product 1");
        newProduct.setPrice("100.0"); // Use Double instead of String

        // Send the POST request with the ProductDto object as the body
        ResponseEntity<ProductDto> response = restTemplate.postForEntity(getBaseUrl() + "/api/product/createUser", newProduct, ProductDto.class);

        // Assert the response status is OK
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Assert that the response body is not null
        assertNotNull(response.getBody());

        // Assert that the product name in the response matches the expected product name
        assertEquals("Test Product 1", response.getBody().getProduct_name());
    }

    @Test
    void testGetAllProducts_success() {
//        ResponseEntity<ProductDto[]> response = restTemplate.getForEntity(getBaseUrl() + "/api/product/getAll", ProductDto[].class);
        ResponseEntity<List<ProductDto>> response = restTemplate.exchange(
                getBaseUrl() + "/api/product/getAll",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ProductDto>>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println(response);
        assertEquals(2, response.getBody().size()); // Expecting 2 products based on the setup
    }

//    @Test
//    public void test_getById() {
//        ResponseEntity<ProductDto> response = restTemplate.exchange(getBaseUrl() + "/api/product/1"
//                , HttpMethod.GET
//                , null
//                , new ParameterizedTypeReference<ProductDto>() {
//                });
//        assertNotNull(response);
//        assertEquals(1, response.getBody().getProduct_id(), "Product Id must be same");
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//    }

    @Test
    void test_deleteById() {
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/product/1",
                HttpMethod.DELETE,
                null,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product deleted successfully with product ID: 1", response.getBody());
    }

    @Test
    void test_updateProduct() {
        ProductDto productDto = ProductDto.builder().product_name("Iqoo").price("23333").product_id(1L).build();
        HttpEntity<ProductDto> requestEntity = new HttpEntity<>(productDto);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                getBaseUrl() + "/api/product/{id}",
                HttpMethod.PUT,
                requestEntity,
                ProductDto.class
                , 1L);

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code should be OK");
        assertEquals("Iqoo", response.getBody().getProduct_name(), "Product name should be updated to Iqoo");
        assertEquals("23333", response.getBody().getPrice(), "Product price should be updated to 23333");

    }
    @Test
    void test_updateProductId() {
        // Create the product DTO to be updated
        ProductDto productDto = ProductDto.builder()
                .product_name("Iqoo")
                .price("23333")
                .product_id(1L)
                .build();

        // Perform a PUT request using WebTestClient
        webTestClient.put()
                .uri(getBaseUrl() + "/api/product/{id}", 1L)  // Use the product id in URL
                .bodyValue(productDto)  // Set the body of the request
                .exchange()  // Execute the request
                .expectStatus().isOk()  // Assert that the status is OK (200)
                .expectBody(ProductDto.class)  // Expect the response body to be of type ProductDto
                .value(responseBody -> {
                    // Assertions on the response
                    assertNotNull(responseBody, "Response body should not be null");
                    assertEquals("Iqoo", responseBody.getProduct_name(), "Product name should be updated to Iqoo");
                    assertEquals("23333", responseBody.getPrice(), "Product price should be updated to 23333");
                });
    }



    @Test
    void test_updateProductp() {
        // Arrange: Create a ProductDto for the test
        ProductDto productDto = ProductDto.builder()
                .product_name("Iqoo")
                .price("23333")
                .product_id(1L)
                .build();
//
//        // Create HttpEntity containing the ProductDto, which will be sent in the body of the request
//        HttpEntity<ProductDto> requestEntity = new HttpEntity<>(productDto); //  it is use TestRestTemplate responseEntity need to Change to HttpEntity
        webClient = WebClient.builder().baseUrl("http://localhost:" + port).build();

        // Act: Make the PUT request to update the product using WebClient
        ResponseEntity<ProductDto> response = webClient.put()
                .uri("/api/product/{id}", 1L)  // Use path variable for dynamic id
                .bodyValue(productDto)  // Set the body of the request
                .retrieve()  // Initiate the request
                .toEntity(ProductDto.class)  // Convert the response body to a ProductDto
                .block();  // Block until the response is received

        // Assert: Check that the response body is not null and validate the response status and content
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code should be OK");
        assertEquals("Iqoo", response.getBody().getProduct_name(), "Product name should be updated to Iqoo");
        assertEquals("23333", response.getBody().getPrice(), "Product price should be updated to 23333");
    }


    // @Test
//    public void test_patchProduct() {
//        ProductDto productDto = ProductDto.builder().product_name("Samsung").build();
//        HttpEntity<ProductDto> requestEntity = new HttpEntity<>(productDto);
//        webClient = WebClient.builder().baseUrl("http://localhost:" + port).build();
//
//        // Send PATCH request using WebClient
//        ProductDto responseDto = webClient.patch()
//                .uri("/api/product/partialUpdate/1")  // Ensure the endpoint is correct
//                .bodyValue(productDto)
//                .retrieve()
//                .bodyToMono(ProductDto.class)
//                .block();  // Block to wait for the response
//
//        // Assert the result
//        assertNotNull(responseDto);
//        assertEquals("Samsung", responseDto.getProduct_name());
//    }

    /**
     * Understanding Mono.just()
     * Mono is a class that represents a single value or no value (like Optional or Future). It's the reactive equivalent of Optional or CompletableFuture in a synchronous context.
     * <p>
     * Mono.just() is used to create a Mono that emits a single value. This is typically used when you already have a value, and you want to wrap it into a Mono to be processed reactively.
     * You use Mono.just(value) when you have a value that you want to emit within a reactive stream.
     * This will create a Mono that wraps the provided value and emits it when subscribed to.
     * <p>
     * Mono.error() is used to generate a Mono that signals an error.
     * Instead of returning a successful value, it emits an error signal that can be handled downstream in your reactive flow.
     */
    @Test
    void testPartialUpdate_ProductNotFound8() {
        ProductDto productDto = new ProductDto(); // Provide valid DTO data for partial update
        Long invalidId = 99L;  // This is an invalid product ID
        webClient = WebClient.builder().baseUrl("http://localhost:" + port).build();

        String expectedErrorMessage = "Product is not Found with productId:" + invalidId;

        // Make the PATCH request using WebClient
        String errorResponse = webClient.patch()
                .uri("/api/product/partialUpdate/{id}", invalidId)
                .bodyValue(productDto)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .doOnTerminate(() -> System.out.println("Error occurred"))
                            .flatMap(errorMessage -> Mono.error(new ProductNotFoundException(errorMessage)));
                })
                .bodyToMono(String.class) // Expect the error message in the response body
                .onErrorResume(ProductNotFoundException.class, throwable ->
                        Mono.just(throwable.getMessage()) // Specific handling for ProductNotFoundException
                )
                .onErrorResume(throwable ->
                        Mono.just("An unexpected error occurred: " + throwable.getMessage()) // Generic handling for other exceptions
                )
                .block(); // Block to wait for the response synchronously
        // Verify the error message and status code

        assertThat(errorResponse).isEqualTo(expectedErrorMessage);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Product is not Found with productId:99", errorResponse, "Error message should match");
    }

    @Test
    void testSaveProduct_NullProductDto() throws JsonProcessingException {
        ProductDto productDto = null;
        webClient = WebClient.builder().baseUrl("http://localhost:" + port).build();

        String errorResponse = webClient.post()
                .uri("/api/product/createUser")
                .body(productDto == null ? Mono.empty() : Mono.just(productDto), ProductDto.class)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .doOnTerminate(() -> System.out.println("Error occurred"))
                            .flatMap(errorMessage -> Mono.error(new IllegalArgumentException(errorMessage)));
                })
                .bodyToMono(String.class)
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException) {
                        return Mono.just(((IllegalArgumentException) throwable).getMessage());
                    }
                    return Mono.empty();
                })
                .block(); // Block to wait for the response synchronously

        // Add assertions to verify the error response
        Map<String, Object> errorResponseMap = new ObjectMapper().readValue(errorResponse, new TypeReference<>() {
        });
        assertEquals(400, errorResponseMap.get("status"));
        assertEquals("Bad Request", errorResponseMap.get("error"));
        assertEquals("/api/product/createUser", errorResponseMap.get("path"));
    }


}
