import javax.swing.*;
import javax.xml.stream.FactoryConfigurationError;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
public class Main {
    public static void main(String[] args) {
        //权限树初始化
        PermissionEntityManagement.GetSingleton().INTI();
        //登录验证后返回用户类型，以创建该用户的权限类型
        User user = new User("张三",PermissionEntityManagement.GetSingleton().GetPat());


        //页面初始化,获得了属于该用户权限类型的Pages
        PageManagement.GetSingleton().INTI(user);
        ArrayList<Facade> pages = PageManagement.GetSingleton().GetPages();


        //算法组要求，Observer模式
        SwitchState switchState = new SwitchState();
        SensorManagement.GetSingleton().INTI(switchState);
        switchState.Switch();
        //25.4.18
        //数据相关问题
        //数据按传感器分类后，形成怎样的文件格式
        //同一时间戳是否满足同时有六个传感器的数据
        //EN
        // Data-related questions
        // After classifying the data by sensor, what file format should be formed?
        // Does the same timestamp ensure that data from all six sensors is available simultaneously?

        //此变量为根据设备id判断是什么位置的传感器
        int var=3;
        //var变量的初始化由数据/算法组完成
        switch (var){
            case 1:SensorManagement.GetSingleton().l1_fun();
            case 2:SensorManagement.GetSingleton().l2_fun();
            case 3:SensorManagement.GetSingleton().l3_fun();
            case 4:SensorManagement.GetSingleton().r1_fun();
            case 5:SensorManagement.GetSingleton().r2_fun();
            case 6:SensorManagement.GetSingleton().r3_fun();

        }
        //Test GUI
        /*
        JFrame frame = new JFrame("按钮示例");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new FlowLayout());
        JButton button = new JButton("点击我");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchState.Switch();
            }
        });
        frame.add(button);
        frame.setVisible(true);*/
        //

    }
}