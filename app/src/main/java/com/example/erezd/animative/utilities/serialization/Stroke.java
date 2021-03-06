package com.example.erezd.animative.utilities.serialization;

import android.graphics.RectF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.wacom.ink.manipulation.Intersectable;
import com.wacom.ink.path.PathBuilder;
import com.wacom.ink.rasterization.BlendMode;
import com.wacom.ink.utils.Utils;

public class Stroke implements Intersectable{
	public static enum StrokeType{PATH, OBJ};
	private StrokeType type;
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

	private FloatBuffer segmentsBounds;
	private RectF bounds;
	
	public Stroke(){
		bounds = new RectF();
		type = StrokeType.OBJ;
	}
	
	public Stroke(int size) {
		this();
		setPoints(Utils.createNativeFloatBufferBySize(size), size);
		//default values
		startT = 0.0f;
		endT = 1.0f;
	}

	public Stroke(Stroke source){
		color = source.color;
		stride = source.stride;
		size = source.size;
		width = source.width;
		startT = source.startT;
		endT = source.endT;
		blendMode = source.blendMode;
		paintIndex = source.paintIndex;
		seed = source.seed;
		hasRandomSeed = source.hasRandomSeed;
		bounds = new RectF(source.bounds);
		segmentsBounds = source.segmentsBounds;
		type = source.type;
		copyPoints(source.points, 0, source.size);
	}

	public int getStride() {
		return stride;
	}

	public void setStride(int stride) {
		this.stride = stride;
	}

	public FloatBuffer getPoints() {
		return points;
	}

	public int getSize() {
		return size;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getStartValue() {
		return startT;
	}

	public float getEndValue() {
		return endT;
	}

	public StrokeType getType() {
		return type;
	}

	public void setType(StrokeType type) {
		this.type = type;
	}

	@Override
	public FloatBuffer getSegmentsBounds() {
		return segmentsBounds;
	}

	@Override
	public RectF getBounds() {
		return bounds;
	}

	public void setInterval(float startT, float endT) {
		this.startT = startT;
		this.endT = endT;
	}

	public void setPoints(FloatBuffer points, int pointsSize) {
		size = pointsSize;  
		this.points = points;
	}

	public void copyPoints(FloatBuffer source, int sourcePosition, int size) {
		this.size = size;
		points = ByteBuffer.allocateDirect(size * Float.SIZE/Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
		Utils.copyFloatBuffer(source, points, sourcePosition, 0, size);
	}

	public void setBlendMode(BlendMode blendMode){
		this.blendMode = blendMode;
	}
	
	public BlendMode getBlendMode() {
		return blendMode;
	}

	public void setPaintIndex(int paintIndex) {
		this.paintIndex = paintIndex;
	}

	public int getPaintIndex() {
		return paintIndex;
	}

	public int getSeed() {
		return seed;
	}
	
	public void setSeed(int seed){
		this.seed = seed;
	}

	public void setHasRandomSeed(boolean hasRandomSeed) {
		this.hasRandomSeed = hasRandomSeed;
	}
	
	public boolean hasRandomSeed() {
		return hasRandomSeed;
	}


	public void calculateBounds(){
		//a segment is a rectangle of float values
		RectF segmentBounds = new RectF();
		Utils.invalidateRectF(bounds);
		//Allocate a float buffer to hold the segments' bounds.
		//each segment has 4 fields
		FloatBuffer segmentsBounds = Utils.createNativeFloatBuffer(PathBuilder.calculateSegmentsCount(size, stride) * 4);
		//set the buffer at position 0
		segmentsBounds.position(0);

		for (int index=0;index<PathBuilder.calculateSegmentsCount(size, stride);index++){
			PathBuilder.calculateSegmentBounds(getPoints(), getStride(), getWidth(), index, 0.0f, segmentBounds);
			segmentsBounds.put(segmentBounds.left); //left x
			segmentsBounds.put(segmentBounds.top); //left y
			segmentsBounds.put(segmentBounds.width());
			segmentsBounds.put(segmentBounds.height());
			Utils.uniteWith(bounds, segmentBounds);
		}
		this.segmentsBounds = segmentsBounds;
	}
	
}
