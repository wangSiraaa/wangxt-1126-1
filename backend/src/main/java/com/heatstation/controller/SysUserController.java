package com.heatstation.controller;

import com.heatstation.common.Result;
import com.heatstation.entity.SysUser;
import com.heatstation.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String userCode = params.get("userCode");
        String password = params.get("password");

        SysUser user = userService.login(userCode, password);
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("userCode", user.getUserCode());
        data.put("userName", user.getUserName());
        data.put("roleCode", user.getRoleCode());
        data.put("token", "token_" + user.getId() + "_" + System.currentTimeMillis());

        return Result.success(data);
    }

    @GetMapping("/listByRole")
    public Result<List<SysUser>> listByRole(@RequestParam String roleCode) {
        List<SysUser> users = userService.listByRole(roleCode);
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        List<SysUser> users = userService.list();
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }
}
