package com.atomykcoder.atomykplay.activities;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import java.util.concurrent.TimeUnit;

public class RingtoneManagerActivity extends AppCompatActivity {

    private TextView song_name_tv, artist_name_tv, duration_tv;
    private EditText from_editText, to_editText;
    private Button cut_bt;
    MusicDataCapsule music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone_manager);
        music = (MusicDataCapsule) getIntent().getSerializableExtra("music");
        String convertedDuration = MusicHelper.convertDuration(music.getsLength());

        song_name_tv = findViewById(R.id.ringtone_song_name);
        artist_name_tv = findViewById(R.id.ringtone_artist_name);
        duration_tv = findViewById(R.id.ringtone_original_length);
        from_editText = findViewById(R.id.ringtone_from_edit);
        to_editText = findViewById(R.id.ringtone_to_edit);
        cut_bt = findViewById(R.id.ringtone_cut_button);
        to_editText.setHint(convertedDuration);

        song_name_tv.setText(music.getsName());
        artist_name_tv.setText(music.getsArtist());
        duration_tv.setText(convertedDuration);

        cut_bt.setOnClickListener(v -> cutRingtone());
    }

    private void cutRingtone() {
        String _from = from_editText.getText().toString().trim();
        String _to = to_editText.getText().toString().trim();

        long start = TimeUnit.MILLISECONDS.toSeconds(MusicHelper.convertToMillis(_from));
        long end = TimeUnit.MILLISECONDS.toSeconds(MusicHelper.convertToMillis(_to));
        int diff = (int) Math.abs(end - start);
        Log.i("info", "start: " + start + " end: " + end + " diff: " + diff);

        if(!_from.isEmpty() && !_to.isEmpty()) {

        }
        else {
            Toast.makeText(this, "Fields Empty", Toast.LENGTH_SHORT).show();
        }
    }
}
