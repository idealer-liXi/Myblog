package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDTO {
    private String avatar;
    private String name;
    private String bio;
    private String email;
    private String githubName;
    private String githubUrl;
    private String location;
    private String introduction;
    private List<String> hobbies;
    private List<SchoolProfileDTO> schools;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchoolProfileDTO {
        private String schoolKey;
        private String name;
        private List<String> tags;
        private String description;
        private String image;
        private List<String> honors;
    }
}
