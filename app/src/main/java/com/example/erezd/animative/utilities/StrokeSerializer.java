package com.example.erezd.animative.utilities;

import android.net.Uri;

import com.wacom.ink.serialization.InkDecoder;
import com.wacom.ink.serialization.InkEncoder;
import com.wacom.ink.utils.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

public class StrokeSerializer {
    InkEncoder m_Encoder;
    InkDecoder m_Decoder;
    private final static int DEFAULT_DECIMAL_PRECISION = 2;
    private int m_DecimalPrecision;

    public StrokeSerializer(int i_DecimalPrecision) {
        this.m_DecimalPrecision = i_DecimalPrecision;
    }

    public StrokeSerializer() {
        this(DEFAULT_DECIMAL_PRECISION);
    }

    public boolean Serialize(Uri i_Uri, LinkedList<Stroke> i_StrokesList){
        m_Encoder = new InkEncoder();
        int encodedDataSize = 0;
        byte[] bytes = null;

        for(Stroke stroke : i_StrokesList){
            m_Encoder.encodePath(m_DecimalPrecision, stroke.GetPoints(), stroke.GetSize(), stroke.GetStride(), stroke.GetWidth(), stroke.GetColor(), stroke.GetStartValue(), stroke.GetEndValue(), stroke.GetBlendMode());
        }

        ByteBuffer encodedDate = m_Encoder.getEncodedData();
        encodedDataSize = m_Encoder.getEncodedDataSizeInBytes();
        bytes = new byte[encodedDataSize];
        if(encodedDataSize > 0){
            encodedDate.position(0);
            encodedDate.get(bytes);
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(bytes);

        return Utils.saveBinaryFile(i_Uri, buffer, 0, encodedDataSize);
    }

    public LinkedList<Stroke> Deserialize(Uri uri) {
        ByteBuffer buffer = Utils.loadBinaryFile(uri);
        if (buffer==null){
            return new LinkedList<Stroke>();
        }

        buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);

        m_Decoder = new InkDecoder(buffer);
        LinkedList<Stroke> result = new LinkedList<Stroke>();

        while (m_Decoder.decodeNextPath()){
            Stroke stroke = new Stroke(m_Decoder.getDecodedPathSize());

            stroke.SetColor(m_Decoder.getDecodedPathIntColor());
            stroke.SetStride(m_Decoder.getDecodedPathStride());
            stroke.SetInterval(m_Decoder.getDecodedPathTs(), m_Decoder.getDecodedPathTf());
            stroke.SetWidth(m_Decoder.getDecodedPathWidth());
            stroke.SetBlendMode(m_Decoder.getDecodedBlendMode());
            Utils.copyFloatBuffer(m_Decoder.getDecodedPathData(), stroke.GetPoints(), 0, 0, m_Decoder.getDecodedPathSize());

            result.add(stroke);
        }
        return result;
    }
}
