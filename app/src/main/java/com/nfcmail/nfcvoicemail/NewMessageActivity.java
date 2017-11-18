package com.nfcmail.nfcvoicemail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
//import android.support.v7.widget.AppCompatButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

public class NewMessageActivity extends AppCompatActivity {

    // Largely adaptated sample by the developers guide to android developpment https://developer.android.com/guide/topics/media/mediarecorder.html
    private static final String LOG_TAG = "NewMessageActivity";
    // 200 = record_audio request code
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private boolean mStartRecording = true;
    private int i = 0;
    private ProgressBar mProgressBar;
    private CountDownTimer mCountDownTimer;





    //private boolean sinusoidState = true;
    //private AnimatedVectorDrawable drawableSinusoid;

    private static Context context;

    // Requesting permission to RECORD_AUDIO
    // NB : RECORD_AUDIO is considered a "dangerous" permission
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    // Gets the results from the permission asked in onCreate
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Get permissions for each permission request code
        switch(requestCode){
            // Only for the request code of the RECORD_AUDIO permission
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        // If permission isn't granted by user -> quit activity
        if(!permissionToRecordAccepted)
            finish();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        findViewById(R.id.playButton).setEnabled(false);
        findViewById(R.id.playButton).setBackgroundTintList(this.getResources().getColorStateList(R.color.colorButtonDisabled));

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mPlayer.release();
                mPlayer = null;
                findViewById(R.id.playButton).setEnabled(true);
                findViewById(R.id.playButton).setBackgroundTintList(NewMessageActivity.getAppContext().getResources().getColorStateList(R.color.colorAccent));
            }
        });

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFileName);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        startPlaying();
    }


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        NewMessageActivity.context = getApplicationContext();

        TextView nfcTextView = findViewById(R.id.nfcTextView);
        ImageView nfcImageView = findViewById(R.id.nfcImageView);

        // (Tutorial comment) Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/recording_cache.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        final ImageButton mRecordButton = findViewById(R.id.recordButton);
        final ImageButton mPlayButton = findViewById(R.id.playButton);

        /*final AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)getDrawable(R.drawable.animated_record_progressbar);
        final ImageView progressbar = findViewById(R.id.record_progressbar);
        progressbar.setImageDrawable(drawable);*/

        final Animation recordInflateButton = AnimationUtils.loadAnimation(this, R.anim.recordbutton);
        recordInflateButton.setRepeatCount(Animation.INFINITE);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setProgress(0);
        mCountDownTimer = new CountDownTimer(15000,1000) {

            @Override
            public void onTick(long msUntilFinished) {
                i++;
                mProgressBar.setProgress((i/15)*100);
                Log.i(LOG_TAG, "+1sec");
            }

            @Override
            public void onFinish() {
                mProgressBar.setProgress(100);
            }
        };

        mRecordButton.setOnTouchListener(new View.OnTouchListener() {
                 @Override
                 public boolean onTouch(View view, MotionEvent event) {
                     if (event.getAction() == MotionEvent.ACTION_DOWN && mStartRecording) {
                         view.performClick();
                         onRecord(mStartRecording);
                         mStartRecording = !mStartRecording;
                         mRecordButton.startAnimation(recordInflateButton);
                         //drawable.start();

                         mCountDownTimer.start();

                     } else if (event.getAction() == MotionEvent.ACTION_UP) {
                         onRecord(mStartRecording);
                         mStartRecording = !mStartRecording;
                         mRecordButton.clearAnimation();
                         mCountDownTimer.cancel();
                     }
                     return true;
                 }
             }
        );

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlaying();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        // In case the user quits the app without stopping the recording
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        // And delete the audio file in cache
        /*File file = new File(mFileName);
        boolean delete = file.delete();*/
    }

    public static Context getAppContext()
    {
        return NewMessageActivity.context;
    }

}
