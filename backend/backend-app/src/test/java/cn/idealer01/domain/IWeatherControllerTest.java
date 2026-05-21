package cn.idealer01.domain;

import cn.idealer01.api.dto.WeatherPublicResponseDTO;
import cn.idealer01.trigger.http.WeatherPublicController;
import cn.idealer01.trigger.service.IWeatherPublicService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IWeatherControllerTest {

    private MockMvc publicMockMvc;
    private IWeatherPublicService weatherPublicService;

    @Before
    public void setUp() {
        WeatherPublicController controller = new WeatherPublicController();
        weatherPublicService = mock(IWeatherPublicService.class);
        ReflectionTestUtils.setField(controller, "weatherPublicService", weatherPublicService);
        publicMockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void getCurrentWeather_shouldReturnPlainWeatherPayload() throws Exception {
        when(weatherPublicService.getCurrentWeather()).thenReturn(WeatherPublicResponseDTO.builder()
                .city("沈阳")
                .temp("25")
                .text("晴")
                .icon("100")
                .windDir("西南风")
                .windScale("3")
                .humidity("30")
                .feelsLike("26")
                .updateTime("2026-05-21T10:00:00Z")
                .build());

        publicMockMvc.perform(get("/api/public/weather/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("沈阳"))
                .andExpect(jsonPath("$.temp").value("25"))
                .andExpect(jsonPath("$.text").value("晴"));
    }
}
