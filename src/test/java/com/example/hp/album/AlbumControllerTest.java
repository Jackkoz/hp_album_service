package com.example.hp.album;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @Value("classpath:itunes_response.json")
    private Resource itunesResponse;


    @Test
    void returnsData() throws Exception {
        when(restTemplate.getForEntity(any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(itunesResponse.getContentAsString(StandardCharsets.UTF_8)));
        var response = mockMvc.perform(get("/album/search?artist=ozzy%20osbourne"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var results = new ObjectMapper().readValue(response, new TypeReference<List<Album>>(){});

        assertThat(results).hasSize(10);
        assertThat(results.getFirst().name()).isEqualTo("Blizzard of Ozz (40th Anniversary Expanded Edition)");
        assertThat(results.getFirst().thumbnailUrl())
                .isEqualTo("https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/92/06/bd/9206bdb3-453a-db69-44f4-c4b5bfe33510/886448748045.jpg/100x100bb.jpg");
    }
}
