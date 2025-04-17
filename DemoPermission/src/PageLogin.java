public class PageLogin extends Facade{
    //初始化，为每一个页面实例添加权限用户
    @Override
    public void INTI(User user){
        this.user = user;
    }
    //静态添加若干方法
}
