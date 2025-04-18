import java.security.PrivateKey;

public class SensorManagement {
    private LeftSensor1 l1;
    private LeftSensor2 l2;
    private LeftSensor3 l3;
    private RightSensor1 r1;
    private RightSensor2 r2;
    private RightSensor3 r3;

    static private SensorManagement Instance;
    private SensorManagement(){}
    static public SensorManagement GetSingleton(){
        if(Instance==null){
            Instance = new SensorManagement();
        }
        return Instance;
    }
    public void INTI(SwitchState s){
        l1 = new LeftSensor1(s);
        l2 = new LeftSensor2(s);
        l3 = new LeftSensor3(s);
        r1 = new RightSensor1(s);
        r2 = new RightSensor2(s);
        r3 = new RightSensor3(s);
    }
    public void l1_fun(){
        new Thread(l1::fun).start();
    }
    public void l2_fun(){
        new Thread(l1::fun).start();
    }
    public void l3_fun(){
        new Thread(l1::fun).start();
    }
    public void r1_fun(){
        new Thread(l1::fun).start();
    }
    public void r2_fun(){
        new Thread(l1::fun).start();
    }
    public void r3_fun(){
        new Thread(l1::fun).start();
    }
}
