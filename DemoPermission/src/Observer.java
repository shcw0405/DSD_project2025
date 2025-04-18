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
    //算法组实际承担职能
    //需求沟通后，需要六个Observer类
    //fun()方法支持自命名重构
    public void fun() {
        while(State){
            System.out.println("1");
        }
    }
}
