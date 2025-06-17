package com.devsuperior.dscommerce.tests;

import static io.restassured.RestAssured.given;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Fazer requisição de login e pegar token.
 */
public class TokenUtil {


    @SuppressWarnings("unused")
    public static String obtainAccessToken(String username, String password) {
        Response response = authRequest(username, password);
        JsonPath jsonBody = response.jsonPath();
        return jsonBody.getString("access_token");
    }

    private static Response authRequest(String username, String password) {
        return given()
            .auth()
            .preemptive()
            .basic("myclientid", "myclientsecret")
            .contentType("application/x-www-form-urlencoded")
            .formParam("username", username)
            .formParam("password", password)
            .formParam("grant_type", "password")
        .when()
            .post("/oauth2/token");

            
    }
    
}
