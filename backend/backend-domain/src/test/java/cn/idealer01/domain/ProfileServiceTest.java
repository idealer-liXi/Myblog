package cn.idealer01.domain;

import cn.idealer01.api.dto.ProfileRequestDTO;
import cn.idealer01.domain.profile.adapter.repository.IProfileSchoolHonorsRepository;
import cn.idealer01.domain.profile.adapter.repository.IProfileSettingsRepository;
import cn.idealer01.domain.profile.model.entity.ProfileSchoolHonorsEntity;
import cn.idealer01.domain.profile.model.entity.ProfileSettingsEntity;
import cn.idealer01.domain.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private IProfileSettingsRepository profileSettingsRepository;

    @Mock
    private IProfileSchoolHonorsRepository profileSchoolHonorsRepository;

    @Test
    public void getPublicProfile_shouldReturnDefaultInfoWhenDatabaseIsEmpty() {
        ProfileService profileService = new ProfileService();
        ReflectionTestUtils.setField(profileService, "profileSettingsRepository", profileSettingsRepository);
        ReflectionTestUtils.setField(profileService, "profileSchoolHonorsRepository", profileSchoolHonorsRepository);

        when(profileSettingsRepository.getCurrent()).thenReturn(null);
        when(profileSchoolHonorsRepository.findAll()).thenReturn(Collections.emptyList());

        org.junit.jupiter.api.Assertions.assertEquals("Idealer", profileService.getPublicProfile().getName());
        org.junit.jupiter.api.Assertions.assertEquals("安徽理工大学", profileService.getPublicProfile().getSchools().get(0).getName());
        org.junit.jupiter.api.Assertions.assertFalse(profileService.getPublicProfile().getSchools().get(0).getHonors().isEmpty());
    }

    @Test
    public void getPublicProfile_shouldMergeStoredLeftSideFieldsAndStoredHonors() {
        ProfileService profileService = new ProfileService();
        ReflectionTestUtils.setField(profileService, "profileSettingsRepository", profileSettingsRepository);
        ReflectionTestUtils.setField(profileService, "profileSchoolHonorsRepository", profileSchoolHonorsRepository);

        when(profileSettingsRepository.getCurrent()).thenReturn(ProfileSettingsEntity.builder()
                .name("New Owner")
                .bio("AI Engineer")
                .location("辽宁-沈阳")
                .hobbies("写代码\n散步")
                .build());
        when(profileSchoolHonorsRepository.findAll()).thenReturn(Arrays.asList(
                ProfileSchoolHonorsEntity.builder().schoolKey("undergraduate").honors("国家级大学生创新创业训练计划\n全国大学生数学建模竞赛一等奖").build(),
                ProfileSchoolHonorsEntity.builder().schoolKey("graduate").honors("研究生学业一等奖学金").build()
        ));

        org.junit.jupiter.api.Assertions.assertEquals("New Owner", profileService.getPublicProfile().getName());
        org.junit.jupiter.api.Assertions.assertEquals("AI Engineer", profileService.getPublicProfile().getBio());
        org.junit.jupiter.api.Assertions.assertEquals(2, profileService.getPublicProfile().getHobbies().size());
        org.junit.jupiter.api.Assertions.assertEquals("研究生学业一等奖学金", profileService.getPublicProfile().getSchools().get(1).getHonors().get(0));
    }

    @Test
    public void updateProfile_shouldPersistSettingsAndTwoFixedSchoolHonorRecords() {
        ProfileService profileService = new ProfileService();
        ReflectionTestUtils.setField(profileService, "profileSettingsRepository", profileSettingsRepository);
        ReflectionTestUtils.setField(profileService, "profileSchoolHonorsRepository", profileSchoolHonorsRepository);

        ProfileRequestDTO request = ProfileRequestDTO.builder()
                .avatar("https://cdn.example.com/avatar.jpg")
                .name("Idealer")
                .bio("Java开发工程师")
                .email("2755027635@qq.com")
                .githubName("idealer-liXi")
                .githubUrl("https://github.com/idealer-liXi")
                .location("辽宁-沈阳")
                .introduction("新的个人简介")
                .hobbies(Arrays.asList("编程与算法", "开源社区"))
                .undergraduateHonors(Arrays.asList("奖项A", "奖项B"))
                .graduateHonors(Collections.singletonList("奖项C"))
                .build();

        profileService.updateProfile(request);

        verify(profileSettingsRepository).saveOrUpdate(any(ProfileSettingsEntity.class));
        verify(profileSchoolHonorsRepository, times(2)).saveOrUpdate(any(ProfileSchoolHonorsEntity.class));
    }
}
