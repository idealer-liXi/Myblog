package cn.idealer01.domain.profile.service;

import cn.idealer01.api.dto.ProfileRequestDTO;
import cn.idealer01.api.dto.ProfileResponseDTO;
import cn.idealer01.domain.profile.adapter.repository.IProfileSchoolHonorsRepository;
import cn.idealer01.domain.profile.adapter.repository.IProfileSettingsRepository;
import cn.idealer01.domain.profile.model.entity.ProfileSchoolHonorsEntity;
import cn.idealer01.domain.profile.model.entity.ProfileSettingsEntity;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService implements IProfileService {

    private static final String SCHOOL_KEY_UNDERGRADUATE = "undergraduate";
    private static final String SCHOOL_KEY_GRADUATE = "graduate";

    @Resource
    private IProfileSettingsRepository profileSettingsRepository;

    @Resource
    private IProfileSchoolHonorsRepository profileSchoolHonorsRepository;

    @Override
    public ProfileResponseDTO getPublicProfile() {
        return buildProfileResponse();
    }

    @Override
    public ProfileResponseDTO getAdminProfile() {
        return buildProfileResponse();
    }

    @Override
    public ProfileResponseDTO updateProfile(ProfileRequestDTO request) {
        validateRequest(request);

        profileSettingsRepository.saveOrUpdate(ProfileSettingsEntity.builder()
                .avatar(defaultString(request.getAvatar()))
                .name(defaultString(request.getName()))
                .bio(defaultString(request.getBio()))
                .email(defaultString(request.getEmail()))
                .githubName(defaultString(request.getGithubName()))
                .githubUrl(defaultString(request.getGithubUrl()))
                .location(defaultString(request.getLocation()))
                .introduction(defaultString(request.getIntroduction()))
                .hobbies(joinLines(request.getHobbies()))
                .updateTime(new Date())
                .build());

        profileSchoolHonorsRepository.saveOrUpdate(ProfileSchoolHonorsEntity.builder()
                .schoolKey(SCHOOL_KEY_UNDERGRADUATE)
                .honors(joinLines(request.getUndergraduateHonors()))
                .updateTime(new Date())
                .build());

        profileSchoolHonorsRepository.saveOrUpdate(ProfileSchoolHonorsEntity.builder()
                .schoolKey(SCHOOL_KEY_GRADUATE)
                .honors(joinLines(request.getGraduateHonors()))
                .updateTime(new Date())
                .build());

        return buildProfileResponse();
    }

    private ProfileResponseDTO buildProfileResponse() {
        ProfileSettingsEntity settings = profileSettingsRepository.getCurrent();
        List<ProfileSchoolHonorsEntity> honorsEntities = profileSchoolHonorsRepository.findAll();
        ProfileResponseDTO defaults = buildDefaultProfile();

        return ProfileResponseDTO.builder()
                .avatar(hasText(settings == null ? null : settings.getAvatar()) ? settings.getAvatar() : defaults.getAvatar())
                .name(hasText(settings == null ? null : settings.getName()) ? settings.getName() : defaults.getName())
                .bio(hasText(settings == null ? null : settings.getBio()) ? settings.getBio() : defaults.getBio())
                .email(hasText(settings == null ? null : settings.getEmail()) ? settings.getEmail() : defaults.getEmail())
                .githubName(hasText(settings == null ? null : settings.getGithubName()) ? settings.getGithubName() : defaults.getGithubName())
                .githubUrl(hasText(settings == null ? null : settings.getGithubUrl()) ? settings.getGithubUrl() : defaults.getGithubUrl())
                .location(hasText(settings == null ? null : settings.getLocation()) ? settings.getLocation() : defaults.getLocation())
                .introduction(hasText(settings == null ? null : settings.getIntroduction()) ? settings.getIntroduction() : defaults.getIntroduction())
                .hobbies(resolveHobbies(settings, defaults))
                .schools(resolveSchools(honorsEntities, defaults.getSchools()))
                .build();
    }

    private List<String> resolveHobbies(ProfileSettingsEntity settings, ProfileResponseDTO defaults) {
        List<String> hobbies = splitLines(settings == null ? null : settings.getHobbies());
        return hobbies.isEmpty() ? defaults.getHobbies() : hobbies;
    }

    private List<ProfileResponseDTO.SchoolProfileDTO> resolveSchools(List<ProfileSchoolHonorsEntity> honorsEntities,
                                                                     List<ProfileResponseDTO.SchoolProfileDTO> defaultSchools) {
        ProfileResponseDTO.SchoolProfileDTO undergraduate = defaultSchools.get(0);
        ProfileResponseDTO.SchoolProfileDTO graduate = defaultSchools.get(1);

        for (ProfileSchoolHonorsEntity entity : honorsEntities) {
            if (SCHOOL_KEY_UNDERGRADUATE.equals(entity.getSchoolKey())) {
                undergraduate = mergeSchoolHonors(undergraduate, entity.getHonors());
            }
            if (SCHOOL_KEY_GRADUATE.equals(entity.getSchoolKey())) {
                graduate = mergeSchoolHonors(graduate, entity.getHonors());
            }
        }

        return Arrays.asList(undergraduate, graduate);
    }

    private ProfileResponseDTO.SchoolProfileDTO mergeSchoolHonors(ProfileResponseDTO.SchoolProfileDTO base, String honorsText) {
        List<String> honors = splitLines(honorsText);
        return ProfileResponseDTO.SchoolProfileDTO.builder()
                .schoolKey(base.getSchoolKey())
                .name(base.getName())
                .tags(base.getTags())
                .description(base.getDescription())
                .image(base.getImage())
                .honors(honors.isEmpty() ? base.getHonors() : honors)
                .build();
    }

    private void validateRequest(ProfileRequestDTO request) {
        if (request == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "站主介绍配置不能为空");
        }
        if (!hasText(request.getName())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "姓名不能为空");
        }
        if (!hasText(request.getBio())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "职业描述不能为空");
        }
    }

    private ProfileResponseDTO buildDefaultProfile() {
        return ProfileResponseDTO.builder()
                .avatar("@/assets/images/avatar.jpg")
                .name("Idealer")
                .bio("Java开发工程师 | 大模型应用开发工程师 | 大模型算法工程师")
                .email("2755027635@qq.com")
                .githubName("idealer-liXi")
                .githubUrl("https://github.com/idealer-liXi")
                .location("辽宁-沈阳")
                .introduction("具备扎实的前端、后端开发能力，熟悉Vue、SpringBoot等主流框架，热爱开源和技术分享。喜欢设计美观、体验流畅的Web应用，追求代码的优雅与高效。乐于团队协作，善于沟通，持续学习新技术。")
                .hobbies(Arrays.asList("编程与算法", "开源社区", "技术分享", "音乐与吉他", "美食探索"))
                .schools(Arrays.asList(
                        ProfileResponseDTO.SchoolProfileDTO.builder()
                                .schoolKey(SCHOOL_KEY_UNDERGRADUATE)
                                .name("安徽理工大学")
                                .image("@/assets/images/school1.jpeg")
                                .tags(Arrays.asList("省部共建", "中西部高校基础能力建设工程"))
                                .description("坐落于安徽省淮南市，是安徽省高等教育振兴计划‘地方特色高水平大学建设’项目立项建设高校。")
                                .honors(Arrays.asList("国家级大学生创新创业训练计划", "全国大学生数学建模竞赛一等奖"))
                                .build(),
                        ProfileResponseDTO.SchoolProfileDTO.builder()
                                .schoolKey(SCHOOL_KEY_GRADUATE)
                                .name("东北大学")
                                .image("@/assets/images/school2.jpeg")
                                .tags(Arrays.asList("985", "211", "双一流"))
                                .description("坐落于辽宁省沈阳市，是教育部直属的全国重点大学，由教育部、国防科工局、辽宁省、沈阳市共建。")
                                .honors(Collections.singletonList("暂无~~"))
                                .build()
                ))
                .build();
    }

    private String joinLines(List<String> values) {
        if (values == null) {
            return "";
        }
        return values.stream().map(String::trim).filter(this::hasText).collect(Collectors.joining("\n"));
    }

    private List<String> splitLines(String value) {
        if (!hasText(value)) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split("\\r?\\n")).map(String::trim).filter(this::hasText).collect(Collectors.toList());
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
