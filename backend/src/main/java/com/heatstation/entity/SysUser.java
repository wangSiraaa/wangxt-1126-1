package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String userCode;

    private String userName;

    private String password;

    private String roleCode;

    private String phone;

    private Long orgId;

    private Integer status;
}
