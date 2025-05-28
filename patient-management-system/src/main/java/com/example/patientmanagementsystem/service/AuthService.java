package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.LoginDTO;
import com.example.patientmanagementsystem.dto.RegisterDTO;
import com.example.patientmanagementsystem.dto.UserDTO;
import com.example.patientmanagementsystem.dto.LoginResponseDTO;
import com.example.patientmanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.patientmanagementsystem.exception.BusinessException;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.PatientRepository;
import com.example.patientmanagementsystem.repository.UserRepository;
import com.example.patientmanagementsystem.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    // 管理员账号信息
    private static final String ADMIN_PHONE = "13000000000";
    private static final String ADMIN_PASSWORD = "123456";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PatientRepository patientRepository;

    @PostConstruct
    public void initAdminAccount() {
        logger.info("=== 开始初始化管理员账号 ===");
        try {
            // 检查管理员账号是否存在
            Optional<User> adminUser = userRepository.findByPhone(ADMIN_PHONE);
            
            if (adminUser.isPresent()) {
                User admin = adminUser.get();
                logger.info("找到管理员账号: id={}, name={}, isAdmin={}", 
                        admin.getId(), admin.getName(), admin.isAdmin());
                
                // 如果管理员账号存在但isAdmin不为true，则更新
                if (!admin.isAdmin()) {
                    logger.info("管理员账号isAdmin为false，更新为true");
                    admin.setAdmin(true);
                    admin.setDoctor(false);
                    admin.setPatient(false);
                    userRepository.save(admin);
                    logger.info("管理员账号已更新: isAdmin={}", admin.isAdmin());
                }
            } else {
                logger.info("管理员账号不存在，创建新管理员账号");
                
                // 创建管理员账号
                User admin = new User();
                admin.setName("管理员");
                admin.setPhone(ADMIN_PHONE);
                admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                admin.setAdmin(true);
                admin.setDoctor(false);
                admin.setPatient(false);
                
                User savedAdmin = userRepository.save(admin);
                logger.info("管理员账号创建成功: id={}, isAdmin={}", 
                        savedAdmin.getId(), savedAdmin.isAdmin());
            }
        } catch (Exception e) {
            logger.error("初始化管理员账号失败", e);
        }
        logger.info("=== 管理员账号初始化完成 ===");
    }

    public LoginResponseDTO login(LoginDTO loginDTO) {
        logger.info("用户登录: username={}", loginDTO.getUsername());
        
        // 检查是否是管理员登录
        if (ADMIN_PHONE.equals(loginDTO.getUsername())) {
            logger.info("检测到管理员登录，使用管理员专用登录方法");
            return loginAdmin(loginDTO.getUsername(), loginDTO.getPassword());
        }
        
        try {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
            
            // 认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // 设置认证上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成JWT令牌
            String token = tokenProvider.generateToken(authentication);
            logger.info("生成token: {}", token);
            
            // 获取用户信息
            User user = userRepository.findByPhone(loginDTO.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("用户名或密码错误"));
            
            logger.info("用户登录成功: id={}, isAdmin={}, isDoctor={}, isPatient={}", 
                    user.getId(), user.isAdmin(), user.isDoctor(), user.isPatient());
            
            // 构建返回DTO
            LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
            loginResponseDTO.setId(user.getId());
            loginResponseDTO.setName(user.getName());
            loginResponseDTO.setToken(token);
            loginResponseDTO.setAdmin(user.isAdmin());
            loginResponseDTO.setDoctor(user.isDoctor());
            loginResponseDTO.setPatient(user.isPatient());
            
            return loginResponseDTO;
        } catch (BadCredentialsException e) {
            logger.error("用户登录失败: 用户名或密码错误", e);
            throw e;
        } catch (Exception e) {
            logger.error("用户登录失败", e);
            throw e;
        }
    }
    
    /**
     * 管理员登录专用方法
     * 绕过Spring Security认证流程，直接生成带有ROLE_ADMIN权限的token
     */
    public LoginResponseDTO loginAdmin(String username, String password) {
        logger.info("管理员登录: {}", username);
        
        // 从数据库获取管理员用户信息
        User adminUser = userRepository.findByPhone(username)
                .orElseThrow(() -> {
                    logger.error("管理员登录失败: 管理员账号不存在");
                    return new BadCredentialsException("用户名或密码错误");
                });
        
        logger.info("找到管理员用户: id={}, isAdmin={}", adminUser.getId(), adminUser.isAdmin());
        
        // 验证密码
//        logger.info("管理员密码: {}", password);
//        logger.info("管理员数据库密码: {}", adminUser.getPassword());
        if (!passwordEncoder.matches(password, adminUser.getPassword())) {
            logger.error("管理员登录失败: 密码错误");
            throw new BadCredentialsException("用户名或密码错误");
        }
        
        // 确保用户有管理员角色
        if (!adminUser.isAdmin()) {
            logger.warn("管理员账号没有管理员角色标志，尝试更新");
            adminUser.setAdmin(true);
            adminUser.setDoctor(false);
            adminUser.setPatient(false);
            adminUser = userRepository.save(adminUser);
            logger.info("更新管理员角色标志: isAdmin={}", adminUser.isAdmin());
        }
        
        // 创建带有ROLE_ADMIN权限的认证对象
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        logger.info("创建管理员认证对象，权限: {}", authorities);
        
        // 创建认证令牌
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(username, null, authorities);
        
        // 设置认证上下文
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        // 生成JWT令牌
        String token = tokenProvider.generateToken(authToken);
        logger.info("生成管理员token: {}", token);
        
        // 构建返回DTO
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setId(adminUser.getId());
        loginResponseDTO.setName(adminUser.getName());
        loginResponseDTO.setToken(token);
        loginResponseDTO.setAdmin(true);
        loginResponseDTO.setDoctor(false);
        loginResponseDTO.setPatient(false);
        
        logger.info("管理员登录成功: userId={}, token={}", adminUser.getId(), token);
        return loginResponseDTO;
    }
    
    /**
     * 用户注册方法
     * 使用RegisterDTO作为参数
     */
    @Transactional
    public UserDTO register(RegisterDTO registerDTO) {
        logger.info("用户注册: phone={}, name={}", registerDTO.getPhone(), registerDTO.getName());
        
        if (userRepository.existsByPhone(registerDTO.getPhone())) {
            logger.error("用户注册失败: 手机号已被注册 {}", registerDTO.getPhone());
            throw new ResourceAlreadyExistsException("该手机号已被注册");
        }

        // Added check for existing idNumber before creating User or Patient
        if (patientRepository.existsByIdNumber(registerDTO.getIdNumber())) {
            logger.error("用户注册失败: 身份证号码已被注册 {}", registerDTO.getIdNumber());
            throw new ResourceAlreadyExistsException("该身份证号码已被注册");
        }
        
        User user = new User();
        user.setName(registerDTO.getName());
        user.setPhone(registerDTO.getPhone());
        user.setGender(registerDTO.getGender());
        user.setBirthDate(registerDTO.getBirthDate());
        user.setIdType(registerDTO.getIdType());
        user.setIdNumber(registerDTO.getIdNumber());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setAdmin(false);
        user.setDoctor(false);
        user.setPatient(true);
        
        User savedUser = userRepository.save(user);
        logger.info("用户基础账户创建成功: id={}", savedUser.getId());

        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setName(registerDTO.getName());
        patient.setPhone(registerDTO.getPhone());
        patient.setIdNumber(registerDTO.getIdNumber());

        try {
            if (registerDTO.getGender() != null && !registerDTO.getGender().isEmpty()) {
                patient.setGender(Patient.Gender.valueOf(registerDTO.getGender()));
            }
            if (registerDTO.getBirthDate() != null && !registerDTO.getBirthDate().isEmpty()) {
                patient.setBirthDate(LocalDate.parse(registerDTO.getBirthDate()));
            }
            if (registerDTO.getIdType() != null && !registerDTO.getIdType().isEmpty()) {
                patient.setIdType(Patient.IdType.valueOf(registerDTO.getIdType()));
            }
        } catch (IllegalArgumentException | DateTimeParseException e) {
            logger.error("创建Patient实体时，枚举或日期格式转换失败 for userId {}: {}", savedUser.getId(), e.getMessage());
            throw new BusinessException("患者信息格式不正确: " + e.getMessage());
        }
        
        patientRepository.save(patient);
        logger.info("患者详细信息创建成功: patientId={}, userId={}", patient.getId(), savedUser.getId());
        
        UserDTO userDTO = new UserDTO();
        userDTO.setId(savedUser.getId());
        userDTO.setName(savedUser.getName());
        userDTO.setPhone(savedUser.getPhone());
        userDTO.setGender(savedUser.getGender());
        userDTO.setBirthDate(savedUser.getBirthDate());
        userDTO.setIdType(savedUser.getIdType());
        userDTO.setIdNumber(savedUser.getIdNumber());
        userDTO.setAdmin(savedUser.isAdmin());
        userDTO.setDoctor(savedUser.isDoctor());
        userDTO.setPatient(savedUser.isPatient());
        
        return userDTO;
    }
    
    /**
     * 用户注册方法
     * 兼容旧接口，内部调用新的DTO参数方法
     */
    public UserDTO register(String name, String phone, String gender, String birthDate, 
                           String idType, String idNumber, String password) {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName(name);
        registerDTO.setPhone(phone);
        registerDTO.setGender(gender);
        registerDTO.setBirthDate(birthDate);
        registerDTO.setIdType(idType);
        registerDTO.setIdNumber(idNumber);
        registerDTO.setPassword(password);
        
        return register(registerDTO);
    }
}
