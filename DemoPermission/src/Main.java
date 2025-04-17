//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        SafeProxy safeProxy = new SafeProxy(25,"Permission2");
        //第一个参数是代理的类的初始化，第二个参数为该代理实例需要的权限
        PermissionBase example1 = new PermissionLeaf("Permission1");
        PermissionBase example2 = new PermissionLeaf("Permission2");
        PermissionBase example3 = new PermissionLeaf("Permission3");
        PermissionNode example = new PermissionNode();
        example.add(example1);
        example.add(example2);
        example3.add(example3);
        //这里的example模拟当前登录用户具有的权限


        //System.out.println(safeProxy.GET(example));

        User user = new User("张三",PermissionEntityManagement.Instance.GetPat());
    }
}