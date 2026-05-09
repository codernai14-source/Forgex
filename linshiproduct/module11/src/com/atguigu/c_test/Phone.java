package com.atguigu.c_test;

public class Phone {
    public void start(){
        System.out.println("手机开机成功");
    }
    public void shutdown(){
        System.out.println("手机已关机");
    }

    public void usePeripheral(PhonePeripheral p){
        p.open();
        if (p instanceof HeadSet){
            HeadSet headSet =(HeadSet) p;
            headSet.playMusic();
        }else if (p instanceof PowerBank){
            PowerBank powerBank=(PowerBank) p;
            powerBank.charge();
        }
        p.close();
    }
}
