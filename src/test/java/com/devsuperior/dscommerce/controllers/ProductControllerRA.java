package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

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

    @Test
    public void findAllShouldReturnProductPageWhenNameIsEmpty() {
        
        given()
            .get("/products?sort=name")
        .then()
            .statusCode(200)
            .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));
    }

    @Test
    public void findAllShouldReturnProductWhenProductNameIsNotEmpty() {

        String productName = "Macbook";

        given()
            .get("/products?name={productName}", productName)
        .then()
            .statusCode(200)
            .body("content.id[0]", is(3))
            .body("content.name[0]", equalTo("Macbook Pro"))
            .body("content.price[0]", is(1250.0F));
    }

    @Test // Busca todos os produtos que tem o preço maior que 2000.0
    public void findAllShouldReturnProductWhenPriceGreatherThan2000() {

        given()
            .get("/products?size=25")
        .then()
            .statusCode(200)
            .body("content.findAll {it.price > 2000.0}.name", hasItems("PC Gamer Foo", "PC Gamer Weed"))
            .body("content.findAll {it.price > 2000.0}.size()", equalTo(7));
    }
}
