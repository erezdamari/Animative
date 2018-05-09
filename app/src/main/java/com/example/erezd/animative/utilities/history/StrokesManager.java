package com.example.erezd.animative.utilities.history;

import com.example.erezd.animative.utilities.serialization.Stroke;

import java.util.LinkedList;

/**
 * Created by aviva on 08/05/2018.
 */

public class StrokesManager {


    public static void restoreColor(LinkedList<Stroke[]> orignAndCopyList){

        for(Stroke[] orgNdCopy : orignAndCopyList){
            Stroke origin = orgNdCopy[0], copy = orgNdCopy[1];
            if(origin != null && copy != null)
                origin.setColor(copy.getColor());
        }
    }
}
