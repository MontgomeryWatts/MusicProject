package spring.controllers;


import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import spring.services.DatabaseService;

import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ArtistController.class)
public class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseService service;

    private final String ARTIST_URI = "/artists/";

    @Test
    public void noArtistsInDatabase() throws Exception {
        Mockito.when(service.getArtists()).thenReturn(new ArrayList<>());
        mockMvc.perform(MockMvcRequestBuilders.get(ARTIST_URI)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("artists"))
                .andExpect(model().attribute("artists", isA(ArrayList.class)))
                .andExpect(model().attribute("artists", empty()))
                .andExpect(model().attribute("title", is("Displays random artists")))
                .andExpect(content().string(containsString(("NO ARTISTS FOUND"))));
    }


    @Test
    public void someArtistsInDatabase() throws Exception {
        ArrayList<Document> artists = new ArrayList<>();
        artists.add(new Document());

        Mockito.when(service.getArtists()).thenReturn(artists);
        mockMvc.perform(MockMvcRequestBuilders.get(ARTIST_URI)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("artists"))
                .andExpect(model().attribute("artists", isA(ArrayList.class)))
                .andExpect(model().attribute("artists", is(not(empty()))))
                .andExpect(model().attribute("title", is("Displays random artists")));
    }

}
