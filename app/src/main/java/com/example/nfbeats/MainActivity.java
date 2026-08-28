package com.example.nfbeats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.protocol.types.Uri;

public class MainActivity extends AppCompatActivity {

    TabLayout tabLayout;
    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;
    String fragmentState;
    HomeFragment homeFragment;
    String spotifyLink;
    TextView songTextView;
    PlayerApi spotifyPlayer;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] writingTagFilters;
    boolean writeMode;
    Tag myTag;
    Context context;

    private static final String CLIENT_ID = "a5fbb9eec26d4c998af169f150a3f017";
    private static final String REDIRECT_URI = "http://localhost:8080";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        bottomNavigationView = findViewById(R.id.bottomNav);
        frameLayout = findViewById(R.id.frameLayout);
        context = this;
        homeFragment = new HomeFragment();

        // instantiate NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null){
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }

        // read an NFC
        readFromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[] { tagDetected };

        // tab functions
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScanFragment()).commit();
                        fragmentState = "Read";
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new InputFragment()).commit();
                        fragmentState = "Write";
                        break;
                    default:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScanFragment()).commit();
                        fragmentState = "Read";
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScanFragment()).commit();
                        fragmentState = "Read";
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new InputFragment()).commit();
                        fragmentState = "Write";
                        break;
                    default:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ScanFragment()).commit();
                        fragmentState = "Read";
                        break;
                }
            }
        });

        // nav bar functions
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.bottom_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
                    fragmentState = "Home";
                    return true;
                } else if (menuItem.getItemId() == R.id.bottom_tutorial) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TutorialFragment()).commit();
                    fragmentState = "Tutorial";
                    return true;
                }
                return false;
            }
        });

        // deselect tab, select home nav button
        tabLayout.selectTab(null);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // spotify ConnectionParams
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        // connect spotify
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        spotifyPlayer = mSpotifyAppRemote.getPlayerApi();
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        songTextView.setVisibility(View.VISIBLE);
                        connected();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        songTextView.setVisibility(View.GONE);
    }

    private void connected() {
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                        songTextView.setText("Now Playing:\n\n" + track.name + " by " + track.artist.name);
                    }
                });
    }

    // converts spotify url to uri and plays from uri
    private void useSpotifyLink(String link) {
        String[] parts = link.split("/");
        if (parts.length < 5 || !parts[2].equals("open.spotify.com")) {
            Log.i("info", "Invalid Spotify URL");
        }
        parts[4] = parts[4].split("[?]")[0];
        String linkUri = "spotify:" + parts[3] + ":" + parts[4];
        if (parts.length > 5) {
            // include extra parameters
            linkUri += ":" + parts[5];
        }
        Log.i("info", "parts: " + Arrays.toString(parts));
        Log.i("info", "linkUri: " + linkUri);

        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        // Play a track
        mSpotifyAppRemote.getPlayerApi().play(linkUri);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // store nfc data when detected
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }

        // read or write based on tab
        if (fragmentState.equals("Read"))
            readFromIntent(intent);
        else if (fragmentState.equals("Write") && myTag != null)
            try {
                write(spotifyLink, myTag);
                Toast.makeText(context, "Successfully written to NFC tag", Toast.LENGTH_SHORT).show();
                bottomNavigationView.setSelectedItemId(R.id.bottom_home);
            }
            catch (IOException e) {
                Toast.makeText(context, "Error writing to NFC tag", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            catch (FormatException e) {
                Toast.makeText(context, "Error writing to NFC tag", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
    }

    @Override
    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }

    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writingTagFilters, null);
    }

    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

    // read nfc tag
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            readNFC(msgs);
        }
    }
    private void readNFC(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(text));
        //startActivity(browserIntent);
        useSpotifyLink(text);
    }

    // write nfc tag
    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createNdefRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    private NdefRecord createNdefRecord(String text) throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes(StandardCharsets.US_ASCII);
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);

        return recordNFC;
    }
}