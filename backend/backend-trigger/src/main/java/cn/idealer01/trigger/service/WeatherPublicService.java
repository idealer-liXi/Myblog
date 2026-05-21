package cn.idealer01.trigger.service;

import cn.idealer01.api.dto.WeatherPublicResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WeatherPublicService implements IWeatherPublicService {

    private static final String OPEN_METEO_API_BASE = "https://api.open-meteo.com/v1/forecast";
    private static final String IPWHO_API_BASE = "https://ipwho.is/";

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public WeatherPublicResponseDTO getCurrentWeather() {
        try {
            Map<String, Object> locationData = restTemplate.getForObject(IPWHO_API_BASE, Map.class);
            if (locationData == null || !Boolean.TRUE.equals(locationData.get("success"))) {
                return null;
            }

            String city = stringValue(locationData.get("city"));
            String latitude = stringValue(locationData.get("latitude"));
            String longitude = stringValue(locationData.get("longitude"));
            if (latitude.isEmpty() || longitude.isEmpty()) {
                return null;
            }

            Map<String, Object> weatherData = restTemplate.getForObject(
                    OPEN_METEO_API_BASE + "?latitude=" + latitude + "&longitude=" + longitude
                            + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m,is_day"
                            + "&timezone=auto&forecast_days=1",
                    Map.class
            );
            if (weatherData == null) {
                return null;
            }

            Map<String, Object> current = (Map<String, Object>) weatherData.get("current");
            if (current == null) {
                return null;
            }

            int weatherCode = intValue(current.get("weather_code"));
            boolean isDay = intValue(current.get("is_day")) == 1;
            double windSpeed = doubleValue(current.get("wind_speed_10m"));
            double windDirection = doubleValue(current.get("wind_direction_10m"));

            return WeatherPublicResponseDTO.builder()
                    .city(city)
                    .temp(stringValue(current.get("temperature_2m")))
                    .text(mapWeatherText(weatherCode))
                    .icon(mapWeatherIcon(weatherCode, isDay))
                    .windDir(mapWindDirection(windDirection))
                    .windScale(String.valueOf(mapWindScale(windSpeed)))
                    .humidity(stringValue(current.get("relative_humidity_2m")))
                    .feelsLike(stringValue(current.get("apparent_temperature")))
                    .updateTime(stringValue(current.get("time")))
                    .build();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String stringValue(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);
        if (text.endsWith(".0")) {
            return text.substring(0, text.length() - 2);
        }
        return text;
    }

    private int intValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private double doubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return 0D;
        }
    }

    private String mapWeatherText(int code) {
        switch (code) {
            case 0:
                return "晴";
            case 1:
            case 2:
                return "多云";
            case 3:
                return "阴";
            case 45:
            case 48:
                return "雾";
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
                return "毛毛雨";
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
            case 80:
            case 81:
            case 82:
                return "雨";
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return "雪";
            case 95:
            case 96:
            case 99:
                return "雷暴";
            default:
                return "未知";
        }
    }

    private String mapWeatherIcon(int code, boolean isDay) {
        if (code == 0) {
            return isDay ? "100" : "150";
        }
        if (code == 1 || code == 2) {
            return isDay ? "101" : "151";
        }
        if (code == 3) {
            return "104";
        }
        if (code == 45 || code == 48) {
            return "500";
        }
        if ((code >= 51 && code <= 57) || (code >= 61 && code <= 67) || (code >= 80 && code <= 82)) {
            return "305";
        }
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) {
            return "400";
        }
        if (code == 95 || code == 96 || code == 99) {
            return "302";
        }
        return isDay ? "101" : "151";
    }

    private String mapWindDirection(double degree) {
        String[] directions = {"北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"};
        int index = (int) Math.round((((degree % 360) + 360) % 360) / 45.0) % 8;
        return directions[index];
    }

    private int mapWindScale(double speedKmh) {
        double[] thresholds = {1, 6, 12, 20, 29, 39, 50, 62, 75, 89, 103, 118};
        for (int i = 0; i < thresholds.length; i++) {
            if (speedKmh < thresholds[i]) {
                return i;
            }
        }
        return 12;
    }
}
