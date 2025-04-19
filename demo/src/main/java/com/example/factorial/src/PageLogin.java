package com.example.factorial.src;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PageLogin extends Facade {
    //初始化，为每一个页面实例添加权限用户
    //此页面作为初始界面，不需要赋予权限
    @Override
    public void INTI(User user){
        this.user = user;
    }
    //登录方法
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        if(username.equals(password)){//验证方法
            return ResponseEntity.ok("登录成功");
        }
        else {
            return ResponseEntity.status(401).body("用户名或密码错误");
        }
    }
    @PostMapping("/Signup")
    public ResponseEntity<String>  Signup(@RequestBody SignupRequest request){
        String username = request.getUsername();
        String password = request.getPassword();
        /*检验是否是患者账号类型*/
        boolean ex = true;
        if(ex){
            return ResponseEntity.ok("成功注册患者账号");
        }
        else{
            return ResponseEntity.status(401).body("用户名格式不对");
        }
    }
    //静态添加若干方法
}
