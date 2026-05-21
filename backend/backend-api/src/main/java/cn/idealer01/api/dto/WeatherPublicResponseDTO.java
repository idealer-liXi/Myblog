package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherPublicResponseDTO {
    private String city;
    private String temp;
    private String text;
    private String icon;
    private String windDir;
    private String windScale;
    private String humidity;
    private String feelsLike;
    private String updateTime;
}
