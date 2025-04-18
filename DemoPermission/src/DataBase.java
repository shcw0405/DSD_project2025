public class DataBase extends Base{
    private int data;
    public DataBase(int x){
        data = x;
    }
    @Override
    public int GETDATE() {return data;}

    @Override
    public int GET(PermissionBase p) {
        return 0;
    }
}
