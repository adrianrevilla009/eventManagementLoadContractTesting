package es.codeurjc.mca.practica_1_pruebas_ordinaria.unitary;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.codeurjc.mca.practica_1_pruebas_ordinaria.DatabaseInitializer;
import es.codeurjc.mca.practica_1_pruebas_ordinaria.event.Event;
import es.codeurjc.mca.practica_1_pruebas_ordinaria.event.EventRepository;
import es.codeurjc.mca.practica_1_pruebas_ordinaria.image.ImageService;
import es.codeurjc.mca.practica_1_pruebas_ordinaria.user.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("EventController REST tests - MockMVC")
public class EventRestControllerUnitTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DatabaseInitializer databaseInitializer;
    
    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private ImageService imageService;

    private Event event1;
    private Event event2;

    @BeforeEach
    public void setUp() {
        Calendar c1 = Calendar.getInstance(); 
        c1.set(2023, Calendar.MAY, 2, 18, 30);
        event1 = new Event("Concierto municipal de Móstoles", "Concierto ofrecido por ...", c1.getTime(), 19.99, 50);
        event2 = new Event("Concierto municipal de Fuenlabrada", "Concierto ofrecido por ...", c1.getTime(), 29.99, 70);
    }

    @Test
	@DisplayName("Check that all events can be fetched (as a non-logged-in user).)")
	public void getEventsTest() throws Exception{

        List<Event> fakeEvents = Arrays.asList(event1, event2);

        when(eventRepository.findAll()).thenReturn(fakeEvents);

        mvc.perform(
			get("/api/events/")
				.contentType(MediaType.APPLICATION_JSON)
		  )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)));

        verify(eventRepository).findAll();
		
	}

    @Test
    @DisplayName("Add a new event (as a organizer user)")
    @WithMockUser(username = "Patxi", password = "pass", roles = "ORGANIZER")
	public void createEventTest() throws Exception {

        when(eventRepository.save(any(Event.class))).thenReturn(event1);

        mvc.perform(
            post("/api/events/")
                .content(objectMapper.writeValueAsString(event1))
                .contentType(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name", equalTo(event1.getName())));

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Delete an event (as admin user)")
    @WithMockUser(username = "admin", password = "pass", roles = "ADMIN")
	public void deleteEventTest() throws Exception {

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event1));
        when(userService.getIfIAdmin()).thenReturn(true);

        mvc.perform(
            delete("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk());

        verify(eventRepository).delete(any(Event.class));
        verify(imageService).deleteImage(null);
        verify(userService).getIfIAdmin();
    }
    
}
