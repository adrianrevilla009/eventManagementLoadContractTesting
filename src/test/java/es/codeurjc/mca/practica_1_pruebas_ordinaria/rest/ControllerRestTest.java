package es.codeurjc.mca.practica_1_pruebas_ordinaria.rest;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.codeurjc.mca.practica_1_pruebas_ordinaria.DatabaseInitializer;
import es.codeurjc.mca.practica_1_pruebas_ordinaria.event.Event;
import es.codeurjc.mca.practica_1_pruebas_ordinaria.user.User;
import es.codeurjc.mca.practica_1_pruebas_ordinaria.user.UserRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class ControllerRestTest {

    @LocalServerPort
    int port;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected DatabaseInitializer databaseInitializer;

    @Autowired
    private UserRepository userRepository;

    @Container
    public static MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.0.24")
      .withDatabaseName("test")
      .withUsername("user")
      .withPassword("pass");

    protected static User admin;
    protected static User organizer;
    protected static User customer;

    @BeforeEach
    protected void setUp() {
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:" + port;

        // CREATE ADMIN
        admin = new User("admin", "admin@urjc.es", "pass", User.ROLE_ADMIN);
        userRepository.save(admin);
        // CREATE ORGANIZER
        organizer = new User("Patxi", "francisco.gortazar@urjc.es", "pass", User.ROLE_ORGANIZER);
        userRepository.save(organizer);
        // CREATE CUSTOMER
        customer = new User("Michel", "michel.maes@urjc.es", "pass", User.ROLE_CUSTOMER);
        userRepository.save(customer);
    }

    @AfterEach
    protected void tearDown() {
        userRepository.deleteAll();
    }

    protected Event createSampleEvent(){
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
                    .multiPart("max_capacity", 50)
                    .multiPart("multiparImage", new File("files/example.png"))
            .when()
                .post("/api/events/")
            .then()
                .assertThat().statusCode(HttpStatus.SC_CREATED)
            .extract().as(Event.class);
        return createdEvent;
    }

    @AfterAll
    public static void tearDownAll() throws IOException {
        Files.walk(Paths.get("./files/events/"))
                .filter(Files::isRegularFile)
                .filter((file) -> file.getFileName().toString().endsWith(".png"))
                .map(Path::toFile)
                .forEach(File::delete);
    }
    
}
