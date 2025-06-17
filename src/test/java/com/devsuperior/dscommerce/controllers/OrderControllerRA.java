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

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;

public class OrderControllerRA {

    private String clientUsername, adminUsername, adminOnlyUsername, userPassword;
    private String clientToken, adminToken, adminOnlyToken, invalidToken;
    private Long existingId, nonExistingId;
    private Map<String, Object> postOrderInstance;

    @BeforeEach
    public void setUp() {
        
        baseURI = "http://localhost:8080"; // base URI for the application
        nonExistingId = 100L;
        
        // Validando Tokens de login dos usuários 
        clientUsername = "maria@gmail.com";
        adminUsername = "alex@gmail.com";
        adminOnlyUsername = "ana@gmail.com";
        userPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, userPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, userPassword);
        adminOnlyToken = TokenUtil.obtainAccessToken(adminOnlyUsername, userPassword);
        invalidToken = adminToken + "789";

        // Criando Map para representar as categories
        Map<String, Object> category1 = new HashMap<>();
        category1.put("productId", 1);
        category1.put("quantity", 2);
        Map<String, Object> category2 = new HashMap<>();
        category2.put("productId", 5);
        category2.put("quantity", 1);
        
        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(category1);
        categories.add(category2);

        postOrderInstance = new HashMap<>();
        postOrderInstance.put("items", categories);
    }

    /* findById */
    @Test
    public void findByIdShouldReturnOrderWhenExistingIdAndAdminLogged() {

        existingId = 2L;

        given()
            .header("Authorization", "Bearer " + adminToken)
            .get("/orders/{id}", existingId)
        .then()
            .statusCode(200)
            .body("id", is(2))
            .body("status", equalTo("DELIVERED"))
            .body("client.id", is(2))
            .body("client.name", equalTo("Alex Green"))
            .body("payment.id", is(2))
            .body("items.name[0]", equalTo("Macbook Pro"))
            .body("total", is(1250.0F));      
    }

    @Test
    public void findByIdShouldReturnOrderWhenBelongClientAndClientLogged() {
        
        existingId = 1L;

        given()
            .header("Authorization", "Bearer " + clientToken)
            .get("/orders/{id}", existingId)
        .then()
            .statusCode(200)
            .body("id", is(1))
            .body("status", equalTo("PAID"))
            .body("client.id", is(1))
            .body("client.name", equalTo("Maria Brown"))
            .body("payment.id", is(1))
            .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
            .body("total", is(1431.0F));  
    }

    @Test
    public void findByIdShouldReturn403ForbiddenWhenNDoesotBelongClientAndClientLogged() {
        
        existingId = 2L;

        given()
            .header("Authorization", "Bearer " + clientToken)
            .get("/orders/{id}", existingId)
        .then()
            .statusCode(403)
            .body("status", is(403));

    }

    @Test
    public void findByIdShouldReturn404NotFoundWhenNonExistingOrderIdAndAdminLogged() {

        given()
            .header("Authorization", "Bearer " + adminToken)
            .get("/orders/{id}", nonExistingId)
        .then()
            .statusCode(404)
            .body("status", is(404));
    }

    @Test
    public void findByIdShouldReturn404NotFoundWhenNonExistingOrderIdAndClientLogged() {

        given()
            .header("Authorization", "Bearer " + clientToken)
            .get("/orders/{id}", nonExistingId)
        .then()
            .statusCode(404)
            .body("status", is(404));
    }

    @Test
    public void findByIdShouldReturn401UnauthorizedWhenNotLogged() {

        existingId = 1L;

        given()
            .header("Authorization", "Bearer " + invalidToken)
            .get("/orders/{id}", existingId)
        .then()
            .statusCode(401);
    }

    /* insert */
    @Test
    public void insertShouldReturnOrderWhenClientLogged() {

        // Converter objeto Map para JSON
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + clientToken)
            .body(newOrder)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
            .body("status", equalTo("WAITING_PAYMENT"))
            .body("client.id", is(1))
            .body("client.name", equalTo("Maria Brown"))
            .body("items.productId", hasItems(1,5))
            .body("total", is(281.99F));
    }

    @Test
    public void insertShouldReturn422UnprocessableEntityWhenInvalidDataAdminLogged() {

        postOrderInstance.remove("items");

        // Converter objeto Map para JSON
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + clientToken)
            .body(newOrder)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/orders")
        .then()
            .statusCode(422)
            .body("status", is(422));
    }

    @Test
    public void insertShouldReturn401UnauthorizedWhenNotLogged() {

        postOrderInstance.remove("items");

        // Converter objeto Map para JSON
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + invalidToken)
            .body(newOrder)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/orders")
        .then()
            .statusCode(401);
    }

    @Test
	public void insertShouldReturnForbiddenWhenAdminLogged() throws JSONException {
		JSONObject newOrder = new JSONObject(postOrderInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminOnlyToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newOrder)
		.when()
			.post("/orders")
		.then()
			.statusCode(403);
	}
}
