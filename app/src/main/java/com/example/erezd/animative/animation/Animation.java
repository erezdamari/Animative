package com.example.erezd.animative.animation;

import com.example.erezd.animative.utilities.serialization.Stroke;

import java.util.ArrayList;

public class Animation {
    private ArrayList<Stroke> m_Object;
    private ArrayList<Stroke> m_Path;
    private int m_Speed;

    public Animation(){
        m_Object = new ArrayList<>();
        m_Path = new ArrayList<>();
        m_Speed = 0;
    }

    public void foo(){

    }

    public ArrayList<Stroke> GetObject() {
        return m_Object;
    }

    public ArrayList<Stroke> GetPath() {
        return m_Path;
    }

    public int GetSpeed() {
        return m_Speed;
    }

    public void SetObject(ArrayList<Stroke> i_List){
        m_Object = i_List;
    }

    public void SetPath(ArrayList<Stroke> i_List){
        m_Path = i_List;
    }

    public void SetSpeed(int i_Speed){
        m_Speed = i_Speed;
    }
}
