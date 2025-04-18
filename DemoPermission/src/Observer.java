import static java.lang.Thread.sleep;

public class Observer {
    private SwitchState obj;
    private boolean State;
    public Observer(SwitchState s){
        obj = s;
        State = obj.GetState();
        s.ObjAdd(this);
    }
    public void SetState(){
        State = obj.GetState();
    }
    public boolean GetState(){
        return State;
    }
    public void fun() {
        while(State){
            System.out.println("1");
        }
    }
}
