package com.mathffreitas.app.appws.restasure;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.Alphanumeric.class) //will run methods by their names
class UsersWebServiceEndpointTest {

    private final String CONTEXT_PATH = "/app-ws";
    private final String EMAIL_ADDRESS = "matheus@filipe.com";
    private final String JSON = "application/json";
    private static String authorizationHeader;
    private static String userIdHeader;
    private static List<Map<String, String>> addresses;

    @BeforeEach
    void setUp() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

    /*
     *    testUserLogin()
     */

    @Test
    final void a() {
        Map<String, String> loginDetails = new HashMap<>();
        loginDetails.put("email", EMAIL_ADDRESS);
        loginDetails.put("password", "123");

        Response response = given()
                .contentType(JSON).accept(JSON)
                .body(loginDetails)
                .when().post(CONTEXT_PATH + "/users/login")
                .then().statusCode(200).extract().response();

        authorizationHeader = response.header("Authorization");
        userIdHeader = response.header("UserID");

        assertNotNull(authorizationHeader);
        assertNotNull(userIdHeader);
    }

    /*
     *    testGetUserDetails()
     */
    @Test
    final void b() {
        Response response = given()
                .pathParam("userId", userIdHeader)
                .header("Authorization", authorizationHeader)
                .accept(JSON)
                .when()
                .get(CONTEXT_PATH + "/users/{userId}")
                .then().statusCode(200)
                .extract().response();

        String userPublicId = response.jsonPath().getString("userId");
        String userEmail = response.jsonPath().getString("email");
        String userFirstName = response.jsonPath().getString("firstName");
        String userLastName = response.jsonPath().getString("lastName");
        addresses = response.jsonPath().getList("addresses");
        String addressPublicId = addresses.get(0).get("addressId");

        assertNotNull(userPublicId);
        assertNotNull(userEmail);
        assertNotNull(userFirstName);
        assertNotNull(userLastName);
        assertEquals(EMAIL_ADDRESS, userEmail);

        assertTrue(addresses.size() == 2);
        assertTrue(addressPublicId.length() == 30);
    }

    /*
     *    testUpdateUserDetails()
     */
    @Test
    final void c() {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("firstName", "Matheus");
        userDetails.put("lastName", "Filipe");
        //userDetails.put("addresses", userAddresses);

        Response response = given().contentType(JSON)
                .accept(JSON)
                .header("Authorization",authorizationHeader)
                .pathParam("userId", userIdHeader)
                .body(userDetails)
                .when().patch(CONTEXT_PATH + "/users/{userId}")
                .then().statusCode(200)
                .contentType(JSON)
                .extract()
                .response();

        String firstName = response.jsonPath().getString("firstName");
        String lastName = response.jsonPath().getString("lastName");

        List<Map<String, String>>  storeAddresses = response.jsonPath().getList("addresses");

        assertEquals("Matheus", firstName);
        assertEquals("Filipe", lastName);
        assertNotNull(storeAddresses);
        assertTrue(addresses.size() == storeAddresses.size());
        assertEquals(addresses.get(0).get("streetName"), storeAddresses.get(0).get("streetName"));
    }

    /*
     *    testDeleteUserDetails()
     */
    @Test
    //@Disabled
    final void d() {
        Response response = given().header("Authorization", authorizationHeader)
                .accept(JSON).pathParam("userId", userIdHeader)
                .when().delete(CONTEXT_PATH + "/users/{userId}")
                .then().statusCode(200)
                .contentType(JSON)
                .extract().response();

        String operationResult = response.jsonPath().getString("operationResult");
        assertEquals("SUCCESS", operationResult);
    }
}
