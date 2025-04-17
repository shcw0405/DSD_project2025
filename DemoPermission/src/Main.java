//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        //权限用户控制
        PermissionEntityManagement.Instance.INTI();
        User user = new User("张三",PermissionEntityManagement.Instance.GetPat());
        //页面初始化
        PageManagement pageManagement = PageManagement.Instance.GetSingleton();
        pageManagement.INTI(user);
        
    }
}