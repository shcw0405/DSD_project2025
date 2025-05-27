package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户角色管理控制器
 * 用于测试阶段修改用户角色
 */
@RestController
@RequestMapping("/test/users")
public class AdminUserController {

    private final UserRepository userRepository;

    @Autowired
    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 设置用户角色
     * @param phone 用户手机号
     * @param roleRequest 角色信息
     * @return 更新结果
     */
    @PutMapping("/role/{phone}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setUserRole(
            @PathVariable String phone,
            @RequestBody Map<String, Boolean> roleRequest) {
        
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("用户不存在"));
        }
        
        User user = userOpt.get();
        
        if (roleRequest.containsKey("isAdmin")) {
            user.setAdmin(roleRequest.get("isAdmin"));
        }
        
        if (roleRequest.containsKey("isDoctor")) {
            user.setDoctor(roleRequest.get("isDoctor"));
        }
        
        if (roleRequest.containsKey("isPatient")) {
            user.setPatient(roleRequest.get("isPatient"));
        }
        
        User savedUser = userRepository.save(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedUser.getId());
        result.put("name", savedUser.getName());
        result.put("phone", savedUser.getPhone());
        result.put("isAdmin", savedUser.isAdmin());
        result.put("isDoctor", savedUser.isDoctor());
        result.put("isPatient", savedUser.isPatient());
        
        return ResponseEntity.ok(ApiResponse.success(result, "用户角色更新成功"));
    }
}
