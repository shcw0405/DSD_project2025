package com.example.patientmanagementsystem.repository;

import com.example.patientmanagementsystem.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByPhone_Success() {
        // 准备测试数据
        User user = new User();
        user.setPhone("13800000001");
        user.setPassword("encodedPassword");
        user.setDoctor(true);
        user.setPatient(false);
        user.setAdmin(false);
        
        entityManager.persist(user);
        entityManager.flush();
        
        // 执行
        Optional<User> found = userRepository.findByPhone("13800000001");
        
        // 验证
        assertTrue(found.isPresent());
        assertEquals("13800000001", found.get().getPhone());
        assertEquals("encodedPassword", found.get().getPassword());
        assertTrue(found.get().isDoctor());
        assertFalse(found.get().isPatient());
        assertFalse(found.get().isAdmin());
    }
    
    @Test
    void testFindByPhone_NotFound() {
        // 执行
        Optional<User> found = userRepository.findByPhone("nonexistent");
        
        // 验证
        assertFalse(found.isPresent());
    }
    
    @Test
    void testExistsByPhone_True() {
        // 准备测试数据
        User user = new User();
        user.setPhone("13800000001");
        user.setPassword("encodedPassword");
        
        entityManager.persist(user);
        entityManager.flush();
        
        // 执行
        boolean exists = userRepository.existsByPhone("13800000001");
        
        // 验证
        assertTrue(exists);
    }
    
    @Test
    void testExistsByPhone_False() {
        // 执行
        boolean exists = userRepository.existsByPhone("nonexistent");
        
        // 验证
        assertFalse(exists);
    }
}
