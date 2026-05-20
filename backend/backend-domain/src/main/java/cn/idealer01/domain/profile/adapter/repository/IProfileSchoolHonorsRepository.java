package cn.idealer01.domain.profile.adapter.repository;

import cn.idealer01.domain.profile.model.entity.ProfileSchoolHonorsEntity;

import java.util.List;

public interface IProfileSchoolHonorsRepository {
    List<ProfileSchoolHonorsEntity> findAll();

    void saveOrUpdate(ProfileSchoolHonorsEntity entity);
}
