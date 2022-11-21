package com.atomykcoder.atomykplay.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import org.parceler.Parcels;

import java.util.List;
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
        music = Parcels.unwrap(getIntent().getParcelableExtra("music"));
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
            String path = music.getsPath().replace( ".mp3", "");
            Log.i("info", "path: " + path);

            String command = " -ss " + _from + " -y -i \"" + music.getsPath() + "\" -t " + diff +
                    " -vn -c copy \"" + path + "_atomykplay.mp3\"";
            Log.i("info", "command: " + command);

            FFmpegSession session = FFmpegKit.execute(command);




            //Logging purposes
            List<com.arthenica.ffmpegkit.Log> logs = session.getLogs();
            for(com.arthenica.ffmpegkit.Log log : logs) {
                Log.i("info", log.toString());
            }
                Log.i("info", session.getReturnCode().toString());
                if(ReturnCode.isSuccess(session.getReturnCode())) {
                    Log.i("info", "created the trimmed ringtone");
                    Toast.makeText(this, "Successfully saved in " + path, Toast.LENGTH_LONG).show();
                } else if(ReturnCode.isCancel(session.getReturnCode())){
                    Log.i("info", "canceled execution");
                    Toast.makeText(this, "Execution Cancelled ", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("info", "something went awry");
                    Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
        }
        else {
            Toast.makeText(this, "Fields Empty", Toast.LENGTH_SHORT).show();
        }
    }
}
