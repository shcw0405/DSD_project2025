package com.example.dsd.repository;

import com.example.dsd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * User 实体的 JPA Repository
 */
@Repository // 标记为 Spring 管理的 Repository Bean
public interface UserRepository extends JpaRepository<User, String> { // 泛型参数：实体类型, 主键类型
    // JpaRepository 已经提供了常用的 CRUD 方法，如 findById, save, deleteById 等
    // 如果需要自定义查询，可以在这里添加方法，例如：
    // Optional<User> findByUsername(String username); // Spring Data JPA 会根据方法名自动生成查询
}
