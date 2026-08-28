package com.example.nfbeats;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.types.PlayerState;

import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    View view;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        mainActivity = (MainActivity) getActivity();
        mainActivity.songTextView = view.findViewById(R.id.songTextView);
        mainActivity.songTextView.setVisibility(View.GONE);

        return view;
    }
}