package com.example.erezd.animative.utilities.serialization;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.example.erezd.animative.R;
import com.wacom.ink.WILLException;
import com.wacom.ink.serialization.InkDecoder;
import com.wacom.ink.serialization.InkEncoder;
import com.wacom.ink.serialization.InkPathData;
import com.wacom.ink.utils.Utils;
import com.wacom.ink.willformat.BaseNode;
import com.wacom.ink.willformat.CorePropertiesBuilder;
import com.wacom.ink.willformat.ExtendedPropertiesBuilder;
import com.wacom.ink.willformat.Paths;
import com.wacom.ink.willformat.Section;
import com.wacom.ink.willformat.WILLFormatException;
import com.wacom.ink.willformat.WILLReader;
import com.wacom.ink.willformat.WILLWriter;
import com.wacom.ink.willformat.WillDocument;
import com.wacom.ink.willformat.WillDocumentFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
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
     * save the strokes to a WILL file format.
     * @param strokesList the strokes to save
     * @param context the app to use
     * @param sceneWidth
     * @param sceneHeight
     */
    private void saveWillFile(LinkedList<Stroke> strokesList, Context context, int sceneWidth, int sceneHeight){
        File willFile = new File(Environment.getExternalStorageDirectory() + "/sample.will");

        LinkedList<InkPathData> inkPathsDataList = new LinkedList<InkPathData>();
        //saves each stroke
        for (Stroke stroke: strokesList){
            InkPathData inkPathData = new InkPathData(
                    stroke.getPoints(),
                    stroke.getSize(),
                    stroke.getStride(),
                    stroke.getWidth(),
                    stroke.getColor(),
                    stroke.getStartValue(),
                    stroke.getEndValue(),
                    stroke.getBlendMode(),
                    stroke.getPaintIndex(),
                    stroke.getSeed(),
                    stroke.hasRandomSeed());
            inkPathsDataList.add(inkPathData);
        }

        WillDocumentFactory factory = new WillDocumentFactory(context, context.getCacheDir());
        try{
            //make new instance of WillDocument
            WillDocument willDoc = factory.newDocument();

            //set its properties
            willDoc.setCoreProperties(new CorePropertiesBuilder()
                    .category("category")
                    .created(new Date())
                    .build());

            willDoc.setExtendedProperties(new ExtendedPropertiesBuilder()
                    .template("light")
                    .application("demo")
                    .appVersion("0.0.1")
                    .build());
            ////
            //create a new section to add to the WillDocument
            Section section = willDoc.createSection()
                    .width(sceneWidth)
                    .height(sceneHeight)
                    .addChild(//creates a set of strokes as a child in the section
                            willDoc.createPaths(inkPathsDataList, 2));
            /* MORE OPTIONS FOR THE Section LIKE SETTING AN ID, IS TO BE ADDED HERE.*/

            //add this section to the set of sections in the WillDocument
            willDoc.addSection(section);

            //Write the Document to the address (willFile)
            new WILLWriter(willFile).write(willDoc);
            //release unmanaged resources in the document.
            willDoc.recycle();
        } catch (WILLFormatException e){
            throw new WILLException("Can't write the sample.will file. Reason: " + e.getLocalizedMessage() + " / Check stacktrace in the console.");
        }
    }

    /**
     * load a WillFile format (from a WillDocument)
     * @param strokesList add the loaded paths/strokes to this list
     * @param context to get access to the local files dedicated to this app
     */
    private void loadWillFile(LinkedList<Stroke> strokesList, Context context){
        File willFile = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.FILE_WILL_SAVE_NAME)+ ".will");
        try {
            WILLReader reader = new WILLReader(new WillDocumentFactory(context, context.getCacheDir()), willFile);
            WillDocument doc = reader.read();
            for (Section section: doc.getSections()){
                //a set of paths in the section
                ArrayList<BaseNode> pathsElements = section.findChildren(BaseNode.TYPE_PATHS);
                //loop through each set and get the strokes
                for (BaseNode node: pathsElements){
                    Paths pathsElement = (Paths)node;
                    //loop of strokes in set
                    for (InkPathData inkPath: pathsElement.getInkPaths()){
                        Stroke stroke = new Stroke();
                        stroke.copyPoints(inkPath.getPoints(), 0, inkPath.getSize());
                        stroke.setStride(inkPath.getStride());
                        stroke.setWidth(inkPath.getWidth());
                        stroke.setBlendMode(inkPath.getBlendMode());
                        stroke.setInterval(inkPath.getTs(), inkPath.getTf());
                        stroke.setColor(inkPath.getColor());
                        stroke.setPaintIndex(inkPath.getPaintIndex());
                        stroke.setSeed(inkPath.getRandomSeed());
                        stroke.setHasRandomSeed(inkPath.hasRandomSeed());
                        strokesList.add(stroke);
                    }
                }
            }
            doc.recycle();
        } catch (WILLFormatException e) {
            throw new WILLException("Can't read the sample.will file. Reason: " + e.getLocalizedMessage() + " / Check stacktrace in the console.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    /**
     * encode a list of strokes to a binary file
     * @param uri a location to save the file
     * @param strokes the list of strokes to encode
     * @return
     */
    public boolean serialize(@NonNull Uri uri, LinkedList<Stroke> strokes) {
        if(strokes == null)
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
     * load strokes from a binary file.
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
            stroke.getPoints().position(0);
            stroke.calculateBounds();

            result.add(stroke);//add the stroke to the list
        }
        return result;
    }
}
