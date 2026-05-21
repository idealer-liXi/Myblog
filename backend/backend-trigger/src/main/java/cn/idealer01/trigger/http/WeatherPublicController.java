package cn.idealer01.trigger.http;

import cn.idealer01.api.IWeatherPublicController;
import cn.idealer01.api.dto.WeatherPublicResponseDTO;
import cn.idealer01.trigger.service.IWeatherPublicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/public/weather")
public class WeatherPublicController implements IWeatherPublicController {

    @Resource
    private IWeatherPublicService weatherPublicService;

    @Override
    @GetMapping("/current")
    public WeatherPublicResponseDTO getCurrentWeather() {
        return weatherPublicService.getCurrentWeather();
    }
}
