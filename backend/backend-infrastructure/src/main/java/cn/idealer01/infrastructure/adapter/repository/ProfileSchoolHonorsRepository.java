package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.profile.adapter.repository.IProfileSchoolHonorsRepository;
import cn.idealer01.domain.profile.model.entity.ProfileSchoolHonorsEntity;
import cn.idealer01.infrastructure.dao.IProfileSchoolHonorsDao;
import cn.idealer01.infrastructure.dao.po.ProfileSchoolHonorsRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileSchoolHonorsRepository implements IProfileSchoolHonorsRepository {

    @Resource
    private IProfileSchoolHonorsDao profileSchoolHonorsDao;

    @Override
    public List<ProfileSchoolHonorsEntity> findAll() {
        return profileSchoolHonorsDao.queryAll().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveOrUpdate(ProfileSchoolHonorsEntity entity) {
        ProfileSchoolHonorsRecord current = profileSchoolHonorsDao.queryBySchoolKey(entity.getSchoolKey());
        if (current == null) {
            profileSchoolHonorsDao.insert(ProfileSchoolHonorsRecord.builder()
                    .schoolKey(entity.getSchoolKey())
                    .honors(entity.getHonors())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build());
            return;
        }
        current.setHonors(entity.getHonors());
        current.setUpdateTime(new Date());
        profileSchoolHonorsDao.update(current);
    }

    private ProfileSchoolHonorsEntity toEntity(ProfileSchoolHonorsRecord record) {
        if (record == null) {
            return null;
        }
        return ProfileSchoolHonorsEntity.builder()
                .id(record.getId())
                .schoolKey(record.getSchoolKey())
                .honors(record.getHonors())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .build();
    }
}
