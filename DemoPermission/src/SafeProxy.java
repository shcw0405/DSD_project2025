/*
* 安全代理，使用组合实例的方式进行访问控制
* */
/*
* 安全代理
* 通过该类的接口调用方法
* */
public class SafeProxy{
    private DataBase dataBase;
    private String permission;
    public SafeProxy(int d, String p){
        dataBase = new DataBase(d);
        permission = p;
    }
    public int GET(PermissionBase p){
        if(p.check(permission)){
            System.out.println("当前权限可以访问");
            return dataBase.GETDATE();
        }
        else{
            System.out.println("权限不足");
            return 0;
        }
    }
}
