import java.util.ArrayList;

public class PageManagement {
    private PageManagement(){

    }
    static PageManagement Instance;
    public PageManagement GetSingleton(){
        if(Instance==null){
            Instance = new PageManagement();
        }
        return Instance;
    }
    private ArrayList<Facade> List = new ArrayList<Facade>();
    public void INTI(User user){
        for(Facade f : List){
            f.INTI(user);
        }
    }
    public void add(Facade f){
        List.add(f);
    }
    public void remove(Facade f){
        List.remove(f);
    }
}
