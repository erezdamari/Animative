package com.example.erezd.animative.ui;


import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by aviva on 30/04/2018.
 */

public class SeekBarListener implements SeekBar.OnSeekBarChangeListener{

    private int m_minimumValue;
    private int m_progressValue;
    private TextView m_currentValTxt;

    public SeekBarListener(int currentProgress, int minimumValue, @NonNull TextView currentValTxt){
        m_minimumValue = minimumValue;
        m_currentValTxt = currentValTxt;
        m_progressValue = currentProgress;
        m_currentValTxt.setText(String.valueOf(m_progressValue));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        if(i == 0) {
            //hold the bar on 1
            seekBar.setProgress(1);
            m_currentValTxt.setText(String.valueOf(m_progressValue = 1));//update text
        }else {
            //do not let the bar to get the Max+1
            m_progressValue = (i == seekBar.getMax() + 1 ?  seekBar.getMax() : m_minimumValue + i);
            m_currentValTxt.setText(String.valueOf(m_progressValue-1));//update text
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekBar.setProgress(m_progressValue-1);
     //Log.d("seekbar", ""+seekBar.getProgress());
    }
}
