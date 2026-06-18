package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.entity.SysUser;
import com.heatstation.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    public List<SysUser> listByRole(String roleCode) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRoleCode, roleCode);
        wrapper.eq(SysUser::getStatus, 1);
        wrapper.orderByAsc(SysUser::getId);
        return this.list(wrapper);
    }

    public SysUser login(String userCode, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserCode, userCode);
        wrapper.eq(SysUser::getPassword, password);
        wrapper.eq(SysUser::getStatus, 1);
        return this.getOne(wrapper);
    }
}
