package com.example.erezd.animative.utilities.serialization;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.wacom.ink.serialization.InkDecoder;
import com.wacom.ink.serialization.InkEncoder;
import com.wacom.ink.utils.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

/**
 * Created by avivam on 27/04/2018.
 */

public class StrokeSerializer {

    private final static int DEFAULT_DECIMAL_PRECISION = 2;
    private int decimalPrecision;

    public StrokeSerializer(int decimalPrecision) {
        this.decimalPrecision = decimalPrecision;
    }
    public StrokeSerializer(){decimalPrecision = DEFAULT_DECIMAL_PRECISION;}


    /**
     * encode a list of strokes to a file
     * @param uri a location to save the file
     * @param strokes the list of strokes to encode
     * @return
     */
    public boolean serialize(@NonNull Uri uri, LinkedList<Stroke> strokes) {
        if(strokes == null || strokes.size() == 0)
            return false;

        byte[] bytes = null;
        int encSize = 0;

        InkEncoder encoder = new InkEncoder();

        //encode each stroke in the list
        for (Stroke stroke : strokes) {
            encoder.encodePath(
                    0, stroke.getPoints(), stroke.getSize(),
                    stroke.getStride(), stroke.getWidth(), stroke.getColor(),
                    stroke.getStartValue(), stroke.getEndValue(), stroke.getBlendMode());
        }

        ByteBuffer encData = encoder.getEncodedData(); //get binary presentation of the encoded strokes
        encSize = encoder.getEncodedDataSizeInBytes(); //size of the binary data

        bytes = new byte[encSize];
        if (encSize > 0) {
            encData.position(0);//set buffer position to 0
            encData.get(bytes); //copy the buffer to the bytes array
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);//order the bytes appropriate for a file saving
        buffer.put(bytes);//put back the bytes to the buffer

        return Utils.saveBinaryFile(uri, buffer, 0, encSize);
    }


    /**
     * load strokes from a file.
     * @param uri a location to load the file
     * @return LinkedList<Stroke> of the loaded strokes
     */
    public LinkedList<Stroke> deserialize(@NonNull Uri uri) {
        ByteBuffer buffer = Utils.loadBinaryFile(uri); // get the strokes from a binary file
        if (buffer==null){
            return new LinkedList<Stroke>();
        }

        buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);

        InkDecoder decoder = new InkDecoder(buffer);//decode with this bytes buffer(the strokes container)
        LinkedList<Stroke> result = new LinkedList<Stroke>();

        //loop through every Path/stroke in the decoder
        while (decoder.decodeNextPath()){
            Stroke stroke = new Stroke(decoder.getDecodedPathSize());

            stroke.setColor(decoder.getDecodedPathIntColor());
            stroke.setStride(decoder.getDecodedPathStride());
            stroke.setInterval(decoder.getDecodedPathTs(), decoder.getDecodedPathTf());//startT, endT of the stroke
            stroke.setWidth(decoder.getDecodedPathWidth());
            stroke.setBlendMode(decoder.getDecodedBlendMode());
            Utils.copyFloatBuffer(decoder.getDecodedPathData(), stroke.getPoints(), 0, 0, decoder.getDecodedPathSize());

            result.add(stroke);//add the stroke to the list
        }
        return result;
    }
}
