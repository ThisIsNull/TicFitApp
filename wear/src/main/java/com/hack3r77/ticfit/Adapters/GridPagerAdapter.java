package com.hack3r77.ticfit.Adapters;

/**
 * Created by LucP77 on 2016-12-10.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;

import com.hack3r77.ticfit.Fragments.RunInfoFragment;
import com.hack3r77.ticfit.Fragments.RunStartFragment;

public class GridPagerAdapter extends FragmentGridPagerAdapter {

    final String TAG = "GridPagerAdapter";
    boolean logDebug = false;
    private int row = 1;
    private int col = 1;

    RunStartFragment runStart_fragment;
    RunInfoFragment runInfo_fragment;

    public GridPagerAdapter(FragmentManager fm, RunStartFragment runStart_fragment, RunInfoFragment runInfo_fragment) {
        super(fm);
        if(logDebug){
            Log.d(TAG, "constructor");
        }
        this.runStart_fragment = runStart_fragment;
        this.runInfo_fragment = runInfo_fragment;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if(logDebug){
            Log.d(TAG, "getFragment " + row + "," + col);
        }
        int position = (row * 10) + col;
        switch (position){
            case 00: //row 0, col 0
                return runStart_fragment;
            case 01: //row0, col 1
                return runInfo_fragment;
            default:
                return null;
        }
        //return new RunInfoFragment();
    }

    @Override
    public int getRowCount() {
        if(logDebug){
            Log.d(TAG, "getRowCount");
        }
        return row;
    }

    @Override
    public int getColumnCount(int row) {
        if(logDebug){
            Log.d(TAG, "getColumnCount");
        }
        return col;
    }

    public void setRowCount(int row){
        this.row = row;
    }

    public void setColCount(int col){
        this.col = col;
    }
}
