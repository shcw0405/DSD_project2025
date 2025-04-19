package com.example.factorial.src;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        //权限树初始化
        PermissionEntityManagement.GetSingleton().INTI();
        //登录验证后返回用户类型，以创建该用户的权限类型
        User user = new User("张三",PermissionEntityManagement.GetSingleton().GetPat());
        //页面初始化,获得了属于该用户权限类型的Pages
        PageManagement.GetSingleton().INTI(user);


        ArrayList<Facade> pages = PageManagement.GetSingleton().GetPages();
        SwitchState switchState = new SwitchState();

        switchState.Switch();
        ArrayList<Float> data = new ArrayList<Float>();


        SpringApplication.run(Main.class, args);
        System.out.println("后端已启动，访问 http://localhost:12345");

        //权限树初始化
        PermissionEntityManagement.GetSingleton().INTI();

    }
}