import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        //权限用户控制
        PermissionEntityManagement.GetSingleton().INTI();
        User user = new User("张三",PermissionEntityManagement.GetSingleton().GetPat());
        //页面初始化
        PageManagement.GetSingleton().INTI(user);
        //算法组要求，Observer模式
        SwitchState sw = new SwitchState();
        Observer o = new Observer(sw);
        sw.Switch();
        new Thread(o::fun).start();
        //Test GUI
        JFrame frame = new JFrame("按钮示例");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new FlowLayout());
        JButton button = new JButton("点击我");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sw.Switch();
            }
        });
        frame.add(button);
        frame.setVisible(true);
        //

    }
}