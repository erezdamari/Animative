package com.example.erezd.animative.animation;

import android.os.Environment;

import com.example.erezd.animative.utilities.serialization.Stroke;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Animation {
    private ArrayList<Stroke> m_Object;
    private ArrayList<Stroke> m_Path;
    private int m_Speed;
    private ArrayList<Float> m_PathPoints;

    public Animation(){
        m_Object = new ArrayList<>();
        m_Path = new ArrayList<>();
        m_Speed = 0;
    }

    public void CreatePath(){
        if(m_Path.size() > 0){
            FloatBuffer buffer = m_Path.get(0).getPoints().duplicate();
            buffer.flip();
            m_PathPoints = new ArrayList<>();
            m_PathPoints.add(0f);
            while (buffer.remaining() > 0){
                m_PathPoints.add(buffer.get());
                m_PathPoints.add(buffer.get());
                buffer.get();
            }
        }
    }

    public void WritePointsToFile() throws IOException {
            File pointsFile = new File(Environment.getExternalStorageDirectory(),"pathPoints.txt");
            FileOutputStream file = new FileOutputStream(pointsFile);
            PrintWriter printWriter = new PrintWriter(file,true);

            for(int i = 0; i < m_PathPoints.size(); i++){
                printWriter.write(m_PathPoints.get(i).toString() + "\n");
            }

            printWriter.close();
            file.close();
    }

    public ArrayList<Float> getPathPoints() {
        return m_PathPoints;
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
