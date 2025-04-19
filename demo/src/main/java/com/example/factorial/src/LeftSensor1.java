package com.example.factorial.src;

import java.util.ArrayList;
import java.util.HashMap;

public class LeftSensor1 extends Observer{
    public LeftSensor1(SwitchState s){
        super(s);
    }
    private  ArrayList<Double> a;

    private HashMap<String, Double> map;

    /*public void fun_c(ArrayList<Float> data){
        a.add(fun_v(data));
    }
    public HashMap<String,Double> fun_s(){

        Double a_max = a.max();
        map.put("前屈",a_max);

        return map;
    }*/
}
