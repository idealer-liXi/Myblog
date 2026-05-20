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
public class ProfileRequestDTO {
    private String avatar;
    private String name;
    private String bio;
    private String email;
    private String githubName;
    private String githubUrl;
    private String location;
    private String introduction;
    private List<String> hobbies;
    private List<String> undergraduateHonors;
    private List<String> graduateHonors;
}
