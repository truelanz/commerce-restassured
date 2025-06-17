package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;

public class ProductControllerRA {

    private String clientUsername, adminUsername, userPassword;
    private String clientToken, adminToken, invalidToken;
    private Long existingId, nonExistingId, dependentId;
    private Map<String, Object> postProductInstance;

    @BeforeEach
    public void setUp() {
        
        baseURI = "http://localhost:8080"; // base URI for the application
        nonExistingId = 100L;
        dependentId = 3L;
        
        // Validando Tokens de login dos usuários 
        clientUsername = "maria@gmail.com";
        adminUsername = "alex@gmail.com";
        userPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, userPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, userPassword);
        invalidToken = adminToken + "789";

        // Criando Map para representar as categories
        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);
        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);
        
        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(category1);
        categories.add(category2);

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu produto");
        postProductInstance.put("description", "uiuiuiuiuiuiui");
        postProductInstance.put("imgUrl", "img/image");
        postProductInstance.put("price", 50.0);
        postProductInstance.put("categories", categories);
    }

    /* findById */
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

    /* findAll */
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

    /* insert */
    @Test
    public void insertShouldReturnProductWhenAdminLogged() {

        // Converter objeto Map para JSON
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(201)
            .body("name", equalTo("Meu produto"))
            .body("description", equalTo("uiuiuiuiuiuiui"))
            .body("price", is(50.0F))
            .body("imgUrl", equalTo("img/image"))
            .body("categories.id", hasItems(2,3));
    }

    @Test
    public void insertShouldReturn403WhenClientLogged() {

        // Converter objeto Map para JSON
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + clientToken)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(403);
    }

    @Test
    public void insertShouldReturn401WhenNotLogged() {

        // Converter objeto Map para JSON
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + invalidToken)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(401);
    }

    @Test
    public void insertShouldReturn422WhenAdminLoggedAndProductInvalidName() {

        postProductInstance.put("name", "Me"); // invalid name

        // Converter objeto Map para JSON
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors.message[0]", equalTo("Nome precisar ter de 3 a 80 caracteres"));
    }

    @Test
    public void insertShouldReturn422WhenAdminLoggedAndInvalidDescription() {

        postProductInstance.put("description", "short"); // invalid description

        // Converter objeto Map para JSON
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
    }

    @Test
    public void insertShouldReturn422WhenAdminLoggedAndNegativePrice() {

        postProductInstance.put("price", -10);

        // Converter objeto Map para JSON
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturn422WhenAdminLoggedAndPriceIsZero() {

        postProductInstance.put("price", 0);
        // Converter objeto Map para JSON
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturn422WhenAdminLoggedAndProductHasNoCategory() {

        postProductInstance.remove("categories");

        // Converter objeto Map para JSON
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
    }

}
