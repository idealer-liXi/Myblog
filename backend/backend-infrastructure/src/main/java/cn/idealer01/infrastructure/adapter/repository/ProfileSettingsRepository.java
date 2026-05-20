package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.profile.adapter.repository.IProfileSettingsRepository;
import cn.idealer01.domain.profile.model.entity.ProfileSettingsEntity;
import cn.idealer01.infrastructure.dao.IProfileSettingsDao;
import cn.idealer01.infrastructure.dao.po.ProfileSettingsRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class ProfileSettingsRepository implements IProfileSettingsRepository {

    @Resource
    private IProfileSettingsDao profileSettingsDao;

    @Override
    public ProfileSettingsEntity getCurrent() {
        return toEntity(profileSettingsDao.queryCurrent());
    }

    @Override
    public void saveOrUpdate(ProfileSettingsEntity entity) {
        ProfileSettingsRecord current = profileSettingsDao.queryCurrent();
        if (current == null) {
            ProfileSettingsRecord record = ProfileSettingsRecord.builder()
                    .avatar(entity.getAvatar())
                    .name(entity.getName())
                    .bio(entity.getBio())
                    .email(entity.getEmail())
                    .githubName(entity.getGithubName())
                    .githubUrl(entity.getGithubUrl())
                    .location(entity.getLocation())
                    .introduction(entity.getIntroduction())
                    .hobbies(entity.getHobbies())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            profileSettingsDao.insert(record);
            return;
        }

        current.setAvatar(entity.getAvatar());
        current.setName(entity.getName());
        current.setBio(entity.getBio());
        current.setEmail(entity.getEmail());
        current.setGithubName(entity.getGithubName());
        current.setGithubUrl(entity.getGithubUrl());
        current.setLocation(entity.getLocation());
        current.setIntroduction(entity.getIntroduction());
        current.setHobbies(entity.getHobbies());
        current.setUpdateTime(new Date());
        profileSettingsDao.update(current);
    }

    private ProfileSettingsEntity toEntity(ProfileSettingsRecord record) {
        if (record == null) {
            return null;
        }
        return ProfileSettingsEntity.builder()
                .id(record.getId())
                .avatar(record.getAvatar())
                .name(record.getName())
                .bio(record.getBio())
                .email(record.getEmail())
                .githubName(record.getGithubName())
                .githubUrl(record.getGithubUrl())
                .location(record.getLocation())
                .introduction(record.getIntroduction())
                .hobbies(record.getHobbies())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .build();
    }
}
