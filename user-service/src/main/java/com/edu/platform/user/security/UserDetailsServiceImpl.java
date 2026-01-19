package com.edu.platform.user.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.platform.user.entity.UserAccount;
import com.edu.platform.user.entity.UserRelRole;
import com.edu.platform.user.entity.UserRole;
import com.edu.platform.user.mapper.UserAccountMapper;
import com.edu.platform.user.mapper.UserRelRoleMapper;
import com.edu.platform.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 *
 * @author Education Platform
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserAccountMapper userAccountMapper;
    private final UserRelRoleMapper userRelRoleMapper;
    private final UserRoleMapper userRoleMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getUsername, username);
        UserAccount user = userAccountMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 查询用户角色
        List<GrantedAuthority> authorities = getUserAuthorities(user.getId());
        
        // 构建UserDetails
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(user.getStatus() == 0)
                .authorities(authorities)
                .build();
    }
    
    /**
     * 获取用户权限列表
     */
    private List<GrantedAuthority> getUserAuthorities(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<UserRelRole> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(UserRelRole::getUserId, userId);
        List<UserRelRole> userRoles = userRelRoleMapper.selectList(relWrapper);
        
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询角色信息
        List<Long> roleIds = userRoles.stream()
                .map(UserRelRole::getRoleId)
                .collect(Collectors.toList());
        
        List<UserRole> roles = userRoleMapper.selectBatchIds(roleIds);
        
        // 转换为权限
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()))
                .collect(Collectors.toList());
    }
    
}
