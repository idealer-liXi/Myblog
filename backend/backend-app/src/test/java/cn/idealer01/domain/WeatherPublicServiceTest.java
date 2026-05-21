package cn.idealer01.domain;

import cn.idealer01.api.dto.WeatherPublicResponseDTO;
import cn.idealer01.trigger.service.WeatherPublicService;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeatherPublicServiceTest {

    @Test
    public void getCurrentWeather_shouldMapIpAndOpenMeteoPayload() {
        WeatherPublicService service = new WeatherPublicService();
        RestTemplate restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);

        Map<String, Object> location = new HashMap<>();
        location.put("success", true);
        location.put("city", "沈阳");
        location.put("latitude", 41.8057d);
        location.put("longitude", 123.4315d);

        Map<String, Object> current = new HashMap<>();
        current.put("temperature_2m", 25);
        current.put("relative_humidity_2m", 30);
        current.put("apparent_temperature", 26);
        current.put("weather_code", 0);
        current.put("wind_speed_10m", 12);
        current.put("wind_direction_10m", 225);
        current.put("is_day", 1);
        current.put("time", "2026-05-21T10:00");

        Map<String, Object> weather = new HashMap<>();
        weather.put("current", current);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(location)
                .thenReturn(weather);

        WeatherPublicResponseDTO result = service.getCurrentWeather();

        assertEquals("沈阳", result.getCity());
        assertEquals("25", result.getTemp());
        assertEquals("晴", result.getText());
        assertEquals("100", result.getIcon());
        assertEquals("西南风", result.getWindDir());
        assertEquals("3", result.getWindScale());
        assertEquals("30", result.getHumidity());
        assertEquals("26", result.getFeelsLike());
        assertEquals("2026-05-21T10:00", result.getUpdateTime());
    }

    @Test
    public void getCurrentWeather_shouldReturnNullWhenIpLookupFails() {
        WeatherPublicService service = new WeatherPublicService();
        RestTemplate restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);

        Map<String, Object> location = new HashMap<>();
        location.put("success", false);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(location);

        assertNull(service.getCurrentWeather());
    }
}
