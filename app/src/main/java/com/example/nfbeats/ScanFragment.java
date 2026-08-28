package com.example.nfbeats;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScanFragment extends Fragment {

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_scan, container, false);

        TextView tap_scanwrite_textview = view.findViewById(R.id.tapScanWriteTextView);
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity.fragmentState.equals("Read"))
            tap_scanwrite_textview.setText("Tap NFC tag to read music");
        else if (mainActivity.fragmentState.equals("Write"))
            tap_scanwrite_textview.setText("Tap NFC tag to write music");
        else
            tap_scanwrite_textview.setText("ERROR Unknown Function");

        return view;
    }
}