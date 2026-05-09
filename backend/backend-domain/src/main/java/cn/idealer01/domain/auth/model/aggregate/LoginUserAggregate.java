package cn.idealer01.domain.auth.model.aggregate;

import cn.idealer01.domain.auth.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginUserAggregate implements UserDetails {

    private UserEntity userEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_" + userEntity.getRoleCode());
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }

    @Override // 账户是否未过期
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override // 账户是否未锁单
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override // 凭证是否未过期
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override // 账户是否可用
    public boolean isEnabled() {
        return true;
    }
}
