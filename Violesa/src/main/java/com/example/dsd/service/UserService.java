package com.example.dsd.service;

import com.example.dsd.model.User;
import com.example.dsd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 推荐在修改操作上使用事务

import java.util.Optional;

/**
 * 处理用户相关业务逻辑的服务
 */
@Service // 标记为 Spring 管理的 Service Bean
public class UserService {

    @Autowired // 自动注入 UserRepository 实例
    private UserRepository userRepository;

    /**
     * 修改指定用户的密码
     * @param username 用户名
     * @param newPassword 新密码
     * @return 如果用户存在且密码修改成功，返回 true；否则返回 false。
     */
    @Transactional // 声明此方法需要事务支持
    public boolean updateUserPassword(String username, String newPassword) {
        // 1. 查找用户
        Optional<User> userOptional = userRepository.findById(username);

        // 2. 检查用户是否存在
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 3. 更新密码
            user.setPassword(newPassword);
            // 4. 保存更改 (在事务中，save 会执行更新操作)
            userRepository.save(user);
            System.out.println("用户 '" + username + "' 的密码已成功更新。");
            return true;
        } else {
            System.err.println("错误：未找到用户 '" + username + "'，无法更新密码。");
            return false;
        }
    }

    /**
     * 保存用户 (用于 CSV 导入等场景)
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}