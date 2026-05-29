package com.microservice.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.productservice.dto.ProductDtos.*;
import com.microservice.productservice.dto.ProductSearchRequest;
import com.microservice.productservice.entity.ProductStatus;
import com.microservice.productservice.exception.ProductNotFoundException;
import com.microservice.productservice.response.PagedResponse;
import com.microservice.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;
    @MockBean  private ProductService productService;

    private ProductDto sampleProductDto;

    @BeforeEach
    void setUp() {
        sampleProductDto = ProductDto.builder()
                .id(1L).name("Test Product").slug("test-product").sku("SKU-001")
                .price(new BigDecimal("99.99")).status(ProductStatus.ACTIVE)
                .categoryId(1L).categoryName("Electronics")
                .stockQuantity(50).availableQuantity(50).inStock(true)
                .build();
    }

    @Test
    @DisplayName("GET /products - should return 200 with paged products")
    void getAllProducts_returns200() throws Exception {
        PagedResponse<ProductDto> paged = PagedResponse.<ProductDto>builder()
                .content(List.of(sampleProductDto)).page(0).size(20)
                .totalElements(1).totalPages(1).first(true).last(true).build();

        when(productService.getAllProducts(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paged);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /products/{id} - should return 200 when found")
    void getProductById_returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleProductDto);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("SKU-001"));
    }

    @Test
    @DisplayName("GET /products/{id} - should return 404 when not found")
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /products - should return 201 for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createProduct_asAdmin_returns201() throws Exception {
        CreateProductRequest req = CreateProductRequest.builder()
                .name("New Product").price(new BigDecimal("29.99")).build();

        when(productService.createProduct(any(), any())).thenReturn(sampleProductDto);

        mockMvc.perform(post("/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /products - should return 403 for non-ADMIN")
    @WithMockUser(roles = "USER")
    void createProduct_asUser_returns403() throws Exception {
        CreateProductRequest req = CreateProductRequest.builder()
                .name("New Product").price(new BigDecimal("29.99")).build();

        mockMvc.perform(post("/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /products/{id} - should return 200 for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_asAdmin_returns200() throws Exception {
        doNothing().when(productService).deleteProduct(anyLong(), anyLong());

        mockMvc.perform(delete("/products/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /products/search - should return filtered products")
    void searchProducts_returns200() throws Exception {
        PagedResponse<ProductDto> paged = PagedResponse.<ProductDto>builder()
                .content(List.of(sampleProductDto)).page(0).size(20)
                .totalElements(1).totalPages(1).first(true).last(true).build();

        when(productService.searchProducts(any(ProductSearchRequest.class))).thenReturn(paged);

        mockMvc.perform(get("/products/search")
                        .param("query", "test")
                        .param("minPrice", "10")
                        .param("maxPrice", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
