package cn.idealer01.api;

import cn.idealer01.api.dto.WeatherPublicResponseDTO;

public interface IWeatherPublicController {
    WeatherPublicResponseDTO getCurrentWeather();
}
