package com.example.erezd.animative.utilities;

import android.util.Log;

import com.wacom.ink.rasterization.BlendMode;
import com.wacom.ink.utils.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Stroke {
    private FloatBuffer points;
    private int color;
    private int stride;
    private int size;
    private float width;
    private float startT;
    private float endT;
    private BlendMode blendMode;
    private int paintIndex;
    private int seed;
    private boolean hasRandomSeed;

    public Stroke(){

    }

    public Stroke(int size) {
        SetPoints(Utils.createNativeFloatBufferBySize(size), size);
        startT = 0.0f;
        endT = 1.0f;
    }
    public int GetStride() {
        return stride;
    }

    public void SetStride(int stride) {
        this.stride = stride;
    }

    public FloatBuffer GetPoints() {
        return points;
    }

    public int GetSize() {
        return size;
    }

    public int GetColor() {
        return color;
    }

    public void SetColor(int color) {
        this.color = color;
    }

    public float GetWidth() {
        return width;
    }

    public void SetWidth(float width) {
        this.width = width;
    }

    public float GetStartValue() {
        return startT;
    }

    public float GetEndValue() {
        return endT;
    }

    public void SetInterval(float startT, float endT) {
        this.startT = startT;
        this.endT = endT;
    }

    public void SetBlendMode(BlendMode blendMode){
        this.blendMode = blendMode;
    }

    public BlendMode GetBlendMode() {
        return blendMode;
    }

    public void SetPaintIndex(int paintIndex) {
        this.paintIndex = paintIndex;
    }

    public int GetPaintIndex() {
        return paintIndex;
    }

    public int GetSeed() {
        return seed;
    }

    public void SetSeed(int seed){
        this.seed = seed;
    }

    public void SetHasRandomSeed(boolean hasRandomSeed) {
        this.hasRandomSeed = hasRandomSeed;
    }

    public boolean GetHasRandomSeed() {
        return hasRandomSeed;
    }

    public void CopyPoints(FloatBuffer i_Source, int i_SourcePosition, int i_Size) {
        this.size = size;
        points = ByteBuffer.allocateDirect(size * Float.SIZE/Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        try {
            Utils.copyFloatBuffer(i_Source, points, i_SourcePosition, 0, i_Size);
        }
        catch (Exception ex){
            Log.e("ERRORR", ex.getStackTrace().toString());
        }
    }

    public void SetPoints(FloatBuffer points, int pointsSize) {
        size = pointsSize;
        this.points = points;
    }
}
