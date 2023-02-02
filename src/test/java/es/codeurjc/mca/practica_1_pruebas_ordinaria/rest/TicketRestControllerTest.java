package es.codeurjc.mca.practica_1_pruebas_ordinaria.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import es.codeurjc.mca.practica_1_pruebas_ordinaria.event.Event;
import es.codeurjc.mca.practica_1_pruebas_ordinaria.ticket.Ticket;

@DisplayName("REST tests - Ticket Controller")
public class TicketRestControllerTest extends ControllerRestTest{

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
    }
    
    @Test
    @DisplayName("Create new ticket as Customer")
    public void createTicket() throws Exception {

        // GET FIRST EVENT
        
        Event event = createSampleEvent();

        int eventCurrentCapacity = event.getCurrent_capacity();

        // CREATE A TICKET FOR EVENT

        given()
            .auth()
                .basic(customer.getName(), "pass")
            // .queryParam("eventId", event.getId())
        .when()
            .post("/api/tickets/?eventId={eventId}", event.getId())
            // .post("/api/tickets/")
        .then()
            .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("event.id", equalTo(event.getId().intValue()));
        
        // CHECK THAT EVENT CURRENT CAPACITY WAS INCREASED

        Event e = when()
            .get("/api/events/{id}", event.getId())
        .then()
            .assertThat()
                // .body("current_capacity", greaterThan(eventCurrentCapacity))
            .extract().as(Event.class);
        assertEquals(e.getCurrent_capacity(), eventCurrentCapacity + 1);
    }

    @Test
    @DisplayName("Delete ticket as Customer")
    public void deleteTicket() throws Exception {

        // GET FIRST EVENT

        Event event = createSampleEvent();

        // CREATE A TICKET FOR EVENT

        Ticket ticket =
            given()
                .auth()
                    .basic(customer.getName(), "pass")
            .when()
                .post("/api/tickets/?eventId={eventId}", event.getId())
            .then()
                .extract().as(Ticket.class);

        int eventCurrentCapacity = ticket.getEvent().getCurrent_capacity();
        
        // DELETE TICKET

        given()
            .auth()
                .basic(customer.getName(), "pass")
        .when()
            .delete("/api/tickets/{id}", ticket.getId())
        .then()
            .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // CHECK THAT EVENT CURRENT CAPACITY WAS REDUCED

        when()
            .get("/api/events/{id}", event.getId())
        .then()
            .assertThat()
                .body("current_capacity", lessThanOrEqualTo(eventCurrentCapacity));

    }
}
