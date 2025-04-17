public class User {
    private String uName;
    private PermissionNode permission;
    public User(String n, PermissionNode p){
        uName = n;
        permission = p;
    }
    public PermissionBase GetPermission(){
        return this.permission;
    }
}
