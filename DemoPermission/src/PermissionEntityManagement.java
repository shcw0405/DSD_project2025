public class PermissionEntityManagement {
    private PermissionEntityManagement(){

    }
    static PermissionEntityManagement Instance;
    public PermissionEntityManagement GetSingleton(){
        if(Instance==null){
            Instance = new PermissionEntityManagement();
        }
        return Instance;
    }
    public PermissionNode GetDoc(){
        return Doctor;
    }
    public PermissionNode GetPat(){
        return patinent;
    }
    public PermissionNode GetAdm(){
        return Admin;
    }
    public PermissionNode GetAll(){
        return all;
    }

    //手动设置权限
    //添加权限树的儿子节点
    PermissionNode Doctor;
    PermissionNode patinent;
    PermissionNode Admin;
    PermissionNode all;
    //通过如下写法完成初始化
    PermissionBase Del =new PermissionLeaf("delete");
    public void INTI(){
        Doctor.add(patinent);
        all.add(Doctor);
        all.add(Admin);

        Admin.add(Del);
    }

}
