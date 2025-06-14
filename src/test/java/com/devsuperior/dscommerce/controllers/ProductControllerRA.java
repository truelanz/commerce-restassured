package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.ResponseAwareMatcher.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

public class ProductControllerRA {

    private Long existingId, nonExistingId;

    @BeforeEach
    public void setUp() {
        baseURI = "http://localhost:8080"; // base URI for the application
    }

    @Test
    public void findByIdShouldReturnProductWhenExistingId() {
        existingId = 2L;

        given()
            .get("/products/{id}", existingId)
        .then()
            .statusCode(200)
            .body("id", is(2))
            .body("name", equalTo("Smart TV"))
            .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
            .body("price", is(2190.0F))
            .body("categories.id", hasItems(2,3))
            .body("categories.name", hasItems("Eletronicos", "Computadores"));      
    }
}
