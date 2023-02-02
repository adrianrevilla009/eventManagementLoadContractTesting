package es.codeurjc.mca.practica_1_pruebas_ordinaria.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;

import java.io.File;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import es.codeurjc.mca.practica_1_pruebas_ordinaria.event.Event;

@DisplayName("REST tests - Event Controller")
public class EventRestControllerTest extends ControllerRestTest{

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
    }

    @Test
    @DisplayName("Create new event as Organizer")
    public void createEventTest() throws Exception {

        // CREATE NEW EVENT

        Event createdEvent = 
            given()
                .auth()
                    .basic(organizer.getName(), "pass")
                .request()
                    .contentType("multipart/form-data")
                    .multiPart("name","Obra de teatro")
                    .multiPart("description", "Obra ofrecido por ...")
                    .multiPart("date", "2020-11-22T19:00:00+0000")
                    .multiPart("price", 19.99)
                    .multiPart("max_capacity", 5)
                    .multiPart("multiparImage", new File("files/example.png"))
            .when()
                .post("/api/events/")
            .then()
                .assertThat().statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Obra de teatro"))
            .extract().as(Event.class);


        // CHECK THAT EVENT PERSIST
        when()
            .get("/api/events/"+ createdEvent.getId())
        .then()
            .assertThat().statusCode(HttpStatus.SC_OK)
            .body("name", equalTo("Obra de teatro"))
            .body("image", matchesRegex(".*/image_([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})_example.png"))
        ;

    }

   
    
}
