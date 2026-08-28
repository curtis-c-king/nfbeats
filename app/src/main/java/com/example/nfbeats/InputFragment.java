package com.example.nfbeats;

import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InputFragment extends Fragment {

    View view;
    MainActivity mainActivity;
    ClipboardManager clipboard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_input, container, false);

        Button button = view.findViewById(R.id.inputConfirmButton);
        mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            clipboard = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        }

        button.setOnClickListener(view -> {
            if (!(clipboard.hasPrimaryClip())) {
                Log.i("info", "no data in clipboard");
                Toast.makeText(mainActivity, "Nothing in clipboard!", Toast.LENGTH_SHORT).show();
            } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {
                Log.i("info", "data not plaintext");
                Toast.makeText(mainActivity, "No plaintext in clipboard!", Toast.LENGTH_SHORT).show();
            } else {
                String pastedText = clipboard.getPrimaryClip().getItemAt(0).getText().toString();

                if (pastedText.contains("https://open.spotify.com/")) {
                    mainActivity.spotifyLink = pastedText;
                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScanFragment()).commit();
                }
                else {
                    Log.i("info", "pastedText: " + pastedText + " is not a valid Spotify link");
                    Toast.makeText(mainActivity, "Invalid Spotify link!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}