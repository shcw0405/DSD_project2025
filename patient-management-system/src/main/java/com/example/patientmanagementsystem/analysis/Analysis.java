package com.example.patientmanagementsystem.analysis;

import com.example.patientmanagementsystem.exception.CsvValidationException;
import java.io.StringReader;
import java.util.*;


class Sensor{
    String date,timestamp,ID,name;
    Double AccX,AccY,AccZ;
    Double GyroX,GyroY,GyroZ;
    Double Roll,Pitch,Yaw;
    Double Q0,Q1,Q2,Q3;
    Sensor(){}
    Sensor(double Roll,double Pitch,double Yaw){
        this.Roll = Roll;
        this.Pitch = Pitch;
        this.Yaw = Yaw;
    }
}
class Flexion{
    public static double Left(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = L1.Roll;
        return a;
    }
    public static double Right(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = R1.Roll;
        return a;
    }
}
class Extension{
    public static double Left(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = L1.Roll;
        return a;
    }
    public static double Right(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = R1.Roll;
        return a;
    }
}
class Abduction{
    public static double Left(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = L2.Roll;
        return a;
    }
    public static double Right(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = R2.Roll;
        return a;
    }
}
class Adduction{
    public static double Left(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = L2.Roll;
        return a;
    }
    public static double Right(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = R2.Roll;
        return a;
    }
}
class ExRotation{
    public static double Left(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = L3.Roll;
        return a;
    }
    public static double Right(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = R3.Roll;
        return a;
    }
}
class InRotation{
    public static double Left(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = L3.Roll;
        return a;
    }
    public static double Right(Sensor L1,Sensor L2,Sensor L3,Sensor R1,Sensor R2,Sensor R3){
        double a = R3.Roll;
        return a;
    }
}
public class Analysis {
    static ArrayList<Double> FlexionLeft = new ArrayList<>();
    static ArrayList<Double> FlexionRight = new ArrayList<>();

    static ArrayList<Double> ExtensionLeft = new ArrayList<>();
    static ArrayList<Double> ExtensionRight = new ArrayList<>();

    static ArrayList<Double> AbductionLeft = new ArrayList<>();
    static ArrayList<Double> AbductionRight = new ArrayList<>();

    static ArrayList<Double> AdductionLeft = new ArrayList<>();
    static ArrayList<Double> AdductionRight = new ArrayList<>();

    static ArrayList<Double> ExRotationLeft = new ArrayList<>();
    static ArrayList<Double> ExRotationRight = new ArrayList<>();

    static ArrayList<Double> InRotationLeft = new ArrayList<>();
    static ArrayList<Double> InRotationRight = new ArrayList<>();

    static double GetMaxMid(ArrayList<Double> Angle){
        Collections.sort(Angle);
        
        int n = Angle.size();
        int delta = n / 20;
        int MIN = 0 + delta;
        if(delta==0) delta=1;
        int MID = n / 2;
        int MAX = n - delta;
        double res = Angle.get(MAX) - Angle.get(MID);  
       //System.out.println("Angle values: " + Angle);
//        if(res < 0) res += 360;
    //    System.out.println("Angle values:"+Angle);
    //    System.out.println("Angle values:"+Angle.get(MID));
        return res;
    }
    static double GetMidMin(ArrayList<Double> Angle){
        Collections.sort(Angle);
        int n = Angle.size();
        int delta = n / 20;
        int MIN = 0 + delta;
        int MID = n / 2;
        int MAX = n - delta;
        double res = Angle.get(MID) - Angle.get(MIN);
      //  System.out.println("Angle values:"+Angle);
      //  System.out.println("Angle values:"+Angle.get(MID));
//        if(res < 0) res += 360;
        return res;
    }
    public static void Calculate(ArrayList<Double> Data,int jd){
        Sensor L1 = new Sensor(Data.get(6),Data.get(7),Data.get(8));
        Sensor L2 = new Sensor(Data.get(15),Data.get(16),Data.get(17));
        Sensor L3 = new Sensor(Data.get(24),Data.get(25),Data.get(26));
        Sensor R1 = new Sensor(Data.get(33),Data.get(34),Data.get(35));
        Sensor R2 = new Sensor(Data.get(42),Data.get(43),Data.get(44));
        Sensor R3 = new Sensor(Data.get(51),Data.get(52),Data.get(53));
        if(jd==1){
            FlexionLeft.add(Flexion.Left(L1,L2,L3,R1,R2,R3));
            FlexionRight.add(Flexion.Right(L1,L2,L3,R1,R2,R3));
        } 
        else if(jd==2){
            ExtensionLeft.add(Extension.Left(L1,L2,L3,R1,R2,R3));
            ExtensionRight.add(Extension.Right(L1,L2,L3,R1,R2,R3));
        }
        else if(jd==3){   
            AbductionLeft.add(Abduction.Left(L1,L2,L3,R1,R2,R3));
            AbductionRight.add(Abduction.Right(L1,L2,L3,R1,R2,R3));

            AdductionLeft.add(Adduction.Left(L1,L2,L3,R1,R2,R3));
            AdductionRight.add(Adduction.Right(L1,L2,L3,R1,R2,R3));
        }
        else{
            ExRotationLeft.add(ExRotation.Left(L1,L2,L3,R1,R2,R3));
            ExRotationRight.add(ExRotation.Right(L1,L2,L3,R1,R2,R3));

            InRotationLeft.add(InRotation.Left(L1,L2,L3,R1,R2,R3));
            InRotationRight.add(InRotation.Right(L1,L2,L3,R1,R2,R3));
        }
//      ExternalRotationLeft.add(ExternalRotation.Left(L1,L2,L3,R1,R2,R3));
    }
    public static void Calculate1(ArrayList<Sensor> Data){
        int rem=0, rem1 = 0,rem2=0,rem3=0,rem4=0,rem5=0,rem6=0;
        for (int i = 0; i < Data.size(); i++) {
            Sensor sensor = Data.get(i);
            if(i != 0){
                Sensor sensor2 = Data.get(i-1);       
                if(!sensor.name.equals(sensor2.name)) {
                    char aaa=sensor.name.charAt(3);
                    char bbb=sensor.name.charAt(2);
                    if(rem==0) rem=i;
                    if(aaa=='1'&&bbb=='L') rem1=i;
                    else if(aaa=='2'&&bbb=='L') rem2=i;
                    else if(aaa=='3'&&bbb=='L') rem3=i;
                    else if(aaa=='1'&&bbb=='R') rem4=i;
                    else if(aaa=='2'&&bbb=='R') rem5=i;
                    else if(aaa=='3'&&bbb=='R') rem6=i;
                }
            }
        }
        //System.out.println("out:"+rem5);
        for(int i = 0;i < rem; i++){
            ArrayList<Double> dataList = new ArrayList<>();
            Sensor sensor1 = Data.get(i+rem1);
            Sensor sensor2 = Data.get(i+rem2);
            Sensor sensor3 = Data.get(i+rem3);
            Sensor sensor4 = Data.get(i+rem4);
            Sensor sensor5 = Data.get(i+rem5);
            Sensor sensor6 = Data.get(i+rem6);
            extractSensorData(dataList, sensor1);
            extractSensorData(dataList, sensor2);
            extractSensorData(dataList, sensor3);
            extractSensorData(dataList, sensor4);
            extractSensorData(dataList, sensor5);
            extractSensorData(dataList, sensor6);
            Calculate(dataList,1);
        }
    }
    public static void Calculate2(ArrayList<Sensor> Data) {
        int rem=0, rem1 = 0,rem2=0,rem3=0,rem4=0,rem5=0,rem6=0;
        for (int i = 0; i < Data.size(); i++) {
            Sensor sensor = Data.get(i);
            if(i != 0){
                Sensor sensor2 = Data.get(i-1);       
                if(!sensor.name.equals(sensor2.name)) {
                    char aaa=sensor.name.charAt(3);
                    char bbb=sensor.name.charAt(2);
                    if(rem==0) rem=i;
                    if(aaa=='1'&&bbb=='L') rem1=i;
                    else if(aaa=='2'&&bbb=='L') rem2=i;
                    else if(aaa=='3'&&bbb=='L') rem3=i;
                    else if(aaa=='1'&&bbb=='R') rem4=i;
                    else if(aaa=='2'&&bbb=='R') rem5=i;
                    else if(aaa=='3'&&bbb=='R') rem6=i;
                }
            }
        }
       // System.out.println("out:"+rem2);
        for(int i = 0;i < rem; i++){
            ArrayList<Double> dataList = new ArrayList<>();
            Sensor sensor1 = Data.get(i+rem1);
            Sensor sensor2 = Data.get(i+rem2);
            Sensor sensor3 = Data.get(i+rem3);
            Sensor sensor4 = Data.get(i+rem4);
            Sensor sensor5 = Data.get(i+rem5);
            Sensor sensor6 = Data.get(i+rem6);
            extractSensorData(dataList, sensor1);
            extractSensorData(dataList, sensor2);
            extractSensorData(dataList, sensor3);
            extractSensorData(dataList, sensor4);
            extractSensorData(dataList, sensor5);
            extractSensorData(dataList, sensor6);
            Calculate(dataList,2);
        }
    }
    public static void Calculate3(ArrayList<Sensor> Data) {
        int rem=0, rem1 = 0,rem2=0,rem3=0,rem4=0,rem5=0,rem6=0;
        for (int i = 0; i < Data.size(); i++) {
            Sensor sensor = Data.get(i);
            if(i != 0){
                Sensor sensor2 = Data.get(i-1);       
                if(!sensor.name.equals(sensor2.name)) {
                    char aaa=sensor.name.charAt(3);
                    char bbb=sensor.name.charAt(2);
                    if(rem==0) rem=i;
                    if(aaa=='1'&&bbb=='L') rem1=i;
                    else if(aaa=='2'&&bbb=='L') rem2=i;
                    else if(aaa=='3'&&bbb=='L') rem3=i;
                    else if(aaa=='1'&&bbb=='R') rem4=i;
                    else if(aaa=='2'&&bbb=='R') rem5=i;
                    else if(aaa=='3'&&bbb=='R') rem6=i;
                }
            }
        }
        for(int i = 0;i < rem; i++){
            ArrayList<Double> dataList = new ArrayList<>();
            Sensor sensor1 = Data.get(i+rem1);
            Sensor sensor2 = Data.get(i+rem2);
            Sensor sensor3 = Data.get(i+rem3);
            Sensor sensor4 = Data.get(i+rem4);
            Sensor sensor5 = Data.get(i+rem5);
            Sensor sensor6 = Data.get(i+rem6);
            extractSensorData(dataList, sensor1);
            extractSensorData(dataList, sensor2);
            extractSensorData(dataList, sensor3);
            extractSensorData(dataList, sensor4);
            extractSensorData(dataList, sensor5);
            extractSensorData(dataList, sensor6);
            Calculate(dataList,3);
        }
    }
    public static void Calculate4(ArrayList<Sensor> Data) {
        int rem=0, rem1 = 0,rem2=0,rem3=0,rem4=0,rem5=0,rem6=0;
        for (int i = 0; i < Data.size(); i++) {
            Sensor sensor = Data.get(i);
            if(i != 0){
                Sensor sensor2 = Data.get(i-1);       
                if(!sensor.name.equals(sensor2.name)) {
                    char aaa=sensor.name.charAt(3);
                    char bbb=sensor.name.charAt(2);
                    if(rem==0) rem=i;
                    if(aaa=='1'&&bbb=='L') rem1=i;
                    else if(aaa=='2'&&bbb=='L') rem2=i;
                    else if(aaa=='3'&&bbb=='L') rem3=i;
                    else if(aaa=='1'&&bbb=='R') rem4=i;
                    else if(aaa=='2'&&bbb=='R') rem5=i;
                    else if(aaa=='3'&&bbb=='R') rem6=i;
                }
            }
        }
        for(int i = 0;i < rem; i++){
            ArrayList<Double> dataList = new ArrayList<>();
            Sensor sensor1 = Data.get(i+rem1);
            Sensor sensor2 = Data.get(i+rem2);
            Sensor sensor3 = Data.get(i+rem3);
            Sensor sensor4 = Data.get(i+rem4);
            Sensor sensor5 = Data.get(i+rem5);
            Sensor sensor6 = Data.get(i+rem6);
            extractSensorData(dataList, sensor1);
            extractSensorData(dataList, sensor2);
            extractSensorData(dataList, sensor3);
            extractSensorData(dataList, sensor4);
            extractSensorData(dataList, sensor5);
            extractSensorData(dataList, sensor6);
            Calculate(dataList,4);
        }
    }
    private static void extractSensorData(ArrayList<Double> list, Sensor sensor) {
        if(sensor != null) {
            list.add(sensor.AccX);
            list.add(sensor.AccY);
            list.add(sensor.AccZ);
            list.add(sensor.GyroX);
            list.add(sensor.GyroY);
            list.add(sensor.GyroZ);
            list.add(sensor.Roll);
            list.add(sensor.Pitch);
            list.add(sensor.Yaw);
        }
    }
    public static void StringRead(String s1,String s2,String s3,String s4){
        StringBuffer sb1 = new StringBuffer(s1);
        StringBuffer sb2 = new StringBuffer(s2);
        StringBuffer sb3 = new StringBuffer(s3);
        StringBuffer sb4 = new StringBuffer(s4);
        for(int i=0;i<sb1.length();i++){
            char ch = sb1.charAt(i);
            if(ch=='\n'||ch=='\r'||ch==',') sb1.setCharAt(i,' ');
        }
        for(int i=0;i<sb2.length();i++){
            char ch = sb2.charAt(i);
            if(ch=='\n'||ch=='\r'||ch==',') sb2.setCharAt(i,' ');
        }
        for(int i=0;i<sb3.length();i++){
            char ch = sb3.charAt(i);
            if(ch=='\n'||ch=='\r'||ch==',') sb3.setCharAt(i,' ');
        }
        for(int i=0;i<sb4.length();i++){
            char ch = sb4.charAt(i);
            if(ch=='\n'||ch=='\r'||ch==',') sb4.setCharAt(i,' ');
        }
        s1 = sb1.toString();
        s2 = sb2.toString();
        s3 = sb3.toString();
        s4 = sb4.toString();
        StringReader sr1 = new StringReader(s1);
        StringReader sr2 = new StringReader(s2);
        StringReader sr3 = new StringReader(s3);
        StringReader sr4 = new StringReader(s4);
        Scanner sc1 = new Scanner(sr1);
        Scanner sc2 = new Scanner(sr2);
        Scanner sc3 = new Scanner(sr3);
        Scanner sc4 = new Scanner(sr4);

        try {
            // Skip headers for all scanners
            for(int i=1;i<=12;++i) { // Assuming 12 header columns as before
                if (sc1.hasNext()) sc1.next(); else throw new CsvValidationException("CSV 文件1表头不完整或数据行过少");
                if (sc2.hasNext()) sc2.next(); else throw new CsvValidationException("CSV 文件2表头不完整或数据行过少");
                if (sc3.hasNext()) sc3.next(); else throw new CsvValidationException("CSV 文件3表头不完整或数据行过少");
                if (sc4.hasNext()) sc4.next(); else throw new CsvValidationException("CSV 文件4表头不完整或数据行过少");
            }

            ArrayList<Sensor> Data1 = new ArrayList<Sensor>();
            ArrayList<Sensor> Data2 = new ArrayList<Sensor>();
            ArrayList<Sensor> Data3 = new ArrayList<Sensor>();
            ArrayList<Sensor> Data4 = new ArrayList<Sensor>();

            // Reading data for sc1
            while(sc1.hasNext()){
                Sensor cur = new Sensor();
                cur.date = sc1.next();
                cur.timestamp = sc1.next(); // Potential point for "missing timestamp column"
                cur.ID = sc1.next();
                cur.name = sc1.next();
                cur.AccX = sc1.nextDouble();
                cur.AccY = sc1.nextDouble();
                cur.AccZ = sc1.nextDouble();
                cur.GyroX = sc1.nextDouble();
                cur.GyroY = sc1.nextDouble();
                cur.GyroZ = sc1.nextDouble();
                cur.Roll = sc1.nextDouble();
                cur.Pitch = sc1.nextDouble();
                cur.Yaw = sc1.nextDouble();
                Data1.add(cur);
            }
            // Similar while loops for sc2, sc3, sc4...
            while(sc2.hasNext()){ // For sc2
                Sensor cur = new Sensor();
                cur.date = sc2.next(); cur.timestamp = sc2.next(); cur.ID = sc2.next(); cur.name = sc2.next();
                cur.AccX = sc2.nextDouble(); cur.AccY = sc2.nextDouble(); cur.AccZ = sc2.nextDouble();
                cur.GyroX = sc2.nextDouble(); cur.GyroY = sc2.nextDouble(); cur.GyroZ = sc2.nextDouble();
                cur.Roll = sc2.nextDouble(); cur.Pitch = sc2.nextDouble(); cur.Yaw = sc2.nextDouble();
                Data2.add(cur);
            }
            while(sc3.hasNext()){ // For sc3
                Sensor cur = new Sensor();
                cur.date = sc3.next(); cur.timestamp = sc3.next(); cur.ID = sc3.next(); cur.name = sc3.next();
                cur.AccX = sc3.nextDouble(); cur.AccY = sc3.nextDouble(); cur.AccZ = sc3.nextDouble();
                cur.GyroX = sc3.nextDouble(); cur.GyroY = sc3.nextDouble(); cur.GyroZ = sc3.nextDouble();
                cur.Roll = sc3.nextDouble(); cur.Pitch = sc3.nextDouble(); cur.Yaw = sc3.nextDouble();
                Data3.add(cur);
            }
            while(sc4.hasNext()){ // For sc4
                Sensor cur = new Sensor();
                cur.date = sc4.next(); cur.timestamp = sc4.next(); cur.ID = sc4.next(); cur.name = sc4.next();
                cur.AccX = sc4.nextDouble(); cur.AccY = sc4.nextDouble(); cur.AccZ = sc4.nextDouble();
                cur.GyroX = sc4.nextDouble(); cur.GyroY = sc4.nextDouble(); cur.GyroZ = sc4.nextDouble();
                cur.Roll = sc4.nextDouble(); cur.Pitch = sc4.nextDouble(); cur.Yaw = sc4.nextDouble();
                Data4.add(cur);
            }

            // Check if any data was actually read (after headers)
            if (Data1.isEmpty() && Data2.isEmpty() && Data3.isEmpty() && Data4.isEmpty()) {
                throw new CsvValidationException("所有CSV文件均未包含有效的数据行 (表头之后)");
            }

            Calculate1(Data1); 
            Calculate2(Data2);
            Calculate3(Data3); 
            Calculate4(Data4);
        } catch (InputMismatchException e) {
            throw new CsvValidationException("CSV文件内容格式错误：期望的数值类型与实际读取到的不匹配。", e);
        } catch (NoSuchElementException e) {
            throw new CsvValidationException("CSV文件内容不完整：读取数据时发现缺少预期的列或行。", e);
        } finally {
            sc1.close();
            sc2.close();
            sc3.close();
            sc4.close();
        }
    }
    public static LinkedHashMap<String,Double> Statistic(String s1,String s2,String s3,String s4){
        clearStaticState();
        StringRead(s1,s2,s3,s4);
        double rem1,rem2,rem3,rem4,rem5,rem6,rem7,rem8,rem9,rem10,rem11,rem12;
        LinkedHashMap<String,Double> res = new LinkedHashMap<>();
        rem1=GetMidMin(FlexionLeft)+90;
        rem2=GetMidMin(FlexionRight)+90;
        res.put("左前屈",rem1);
        res.put("右前屈",rem2);
        rem3=GetMaxMid(ExtensionLeft);
        rem4=GetMaxMid(ExtensionRight);
        res.put("左后伸",rem3);
        res.put("右后伸",rem4);
        rem5=GetMaxMid(AbductionLeft)*1.1;
        rem6=GetMaxMid(AbductionRight)*1.1;
        res.put("左外展",rem5);
        res.put("右外展",rem6);
        rem7=GetMidMin(AdductionLeft)*1.5;
        rem8=GetMidMin(AdductionRight)*1.5;
        res.put("左内收",rem7);
        res.put("右内收",rem8);
        rem9=GetMaxMid(ExRotationLeft)*1.5;
        rem10=GetMidMin(ExRotationRight)*1.5;
        res.put("左外旋",rem9);
        res.put("右外旋",rem10);
         rem11=GetMidMin(InRotationLeft)*1.1;
        rem12=GetMaxMid(InRotationRight)*1.1;
        res.put("左内旋",rem11);
        res.put("右内旋",rem12);
        res.put("左前屈差值",rem1-125);
        res.put("右前屈差值",rem2-125);
        res.put("左后伸差值",rem3-20);
        res.put("右后伸差值",rem4-20);
        res.put("左外展差值",rem5-45);
        res.put("右外展差值",rem6-45);
        res.put("左内收差值",rem7-30);
        res.put("右内收差值",rem8-30);
        res.put("左外旋差值",rem9-30);
        res.put("右外旋差值",rem10-30);
        res.put("左内旋差值",rem11-10);
        res.put("右内旋差值",rem12-10);
       /*  for (Map.Entry<String, Double> entry : res.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
        return res;
    }
    public static void clearStaticState() {
        FlexionLeft.clear();
        FlexionRight.clear();
        ExtensionLeft.clear();
        ExtensionRight.clear();
        AbductionLeft.clear();
        AbductionRight.clear();
        AdductionLeft.clear();
        AdductionRight.clear();
        ExRotationLeft.clear();
        ExRotationRight.clear();
        InRotationLeft.clear();
        InRotationRight.clear();
    }
}
