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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

public class NewMessageActivity extends AppCompatActivity {

    // Page d'aide montrant comment enregistrer et jouer un fichier audio
    // https://developer.android.com/guide/topics/media/mediarecorder.html

    // Contexte de l'application = où on en est dans l'application, appelé par diverses fonctions
    private static Context context;

    // Identifiant de l'application dans les logs
    private static final String LOG_TAG = "NewMessageActivity";
    // Le nombre 200 correspond au code de requête d'un enregistrement audio auprès du système
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Chemin du fichier où sera enregistré le fichier audio
    private static String mFileName = null;

    // Classes permettant d'enregistrer et de jouer l'audio
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    // Variable booléenne sachant si un enregistrement doit être commencé (=true) ou arrêté (=false)
    private boolean mStartRecording = true;
    // Variable booléenne permettant d'arrêter l'enregistrement à la fin du compte à rebours, avant que l'utilisateur lève son doigt.
    private boolean isCountDownOver = false;

    // Comptes à rebours
    private CountDownTimer mCountDownTimer;
    private CountDownTimer mResetTimer;

    // Eléments de l'interface
    private ProgressBar mProgressBar;
    private Button sendButton;

    // Variable booléenne pour savoir si l'utilisateur a accepté que l'app enregistre le son, et ainsi continuer
    // NB : RECORD_AUDIO est considérée comme une permission "dangereuse"
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    // Trouver le onCreate
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Trouver les résultats des demandes de permission ...
        switch(requestCode){
            // Ici seulement pour RECORD_AUDIO (mais d'autres peuvent être ajoutées)
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        // Si la permission n'est pas acceptée : on quitte l'activité
        if(!permissionToRecordAccepted)
            finish();
    }

    private void startPlaying() {
        // Démarrage de la lecture du fichier audio spécifié
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        // Désactiver le bouton Play et changer sa couleur
        findViewById(R.id.playButton).setEnabled(false);
        findViewById(R.id.playButton).setBackgroundTintList(this.getResources().getColorStateList(R.color.colorButtonDisabled));

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(mPlayer != null)
                {
                    mPlayer.release();
                    mPlayer = null;
                }
                findViewById(R.id.playButton).setEnabled(true);
                findViewById(R.id.playButton).setBackgroundTintList(NewMessageActivity.getAppContext().getResources().getColorStateList(R.color.colorAccent));

            }
        });
    }

    // Choix entre démarrage ou arrêt de l'enregistrement
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        // Paramétrage de l'enregsitrement audio : source = micro de l'appareil, format en sortie : .3gp, encodeur audio : AMR-NB
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


        // TODO bugfix : check if mFileName still exists and wasn't deleted before trying to record to it
        mRecorder.start();

        isCountDownOver = false;
    }

    private void stopRecording() {

        // Fin de l'enregsitrement audio : enregistrement stoppé et paramètres réinitialisés
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        // Ajout d'un temps où l'on ne peut pas appuyer sur le bouton d'enregistrement pour éviter de créer un enregistrement null
        mResetTimer = new CountDownTimer(500,10) {
            @Override public void onTick(long msUntilFinished){} // Fonction nécessaire pour la classe CountDownTimer

            @Override
            public void onFinish() {
                findViewById(R.id.recordButton).setClickable(true);
                findViewById(R.id.recordButton).setBackgroundTintList(NewMessageActivity.getAppContext().getResources().getColorStateList(R.color.colorRecord));
            }
        };
        findViewById(R.id.recordButton).setClickable(false);
        findViewById(R.id.recordButton).setBackgroundTintList(NewMessageActivity.getAppContext().getResources().getColorStateList(R.color.colorButtonDisabled));
        mResetTimer.start();

        startPlaying();
    }


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        NewMessageActivity.context = getApplicationContext();

        ImageView nfcImageView = findViewById(R.id.nfcImageView);

        // (Tutorial comment) Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/recording_cache.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        final ImageButton mRecordButton = findViewById(R.id.recordButton);
        final ImageButton mPlayButton = findViewById(R.id.playButton);
        mProgressBar = findViewById(R.id.progressBar);
        sendButton = findViewById(R.id.sendButton);
        final Animation recordInflateButton = AnimationUtils.loadAnimation(this, R.anim.recordbutton);
        recordInflateButton.setRepeatCount(Animation.INFINITE);


        mProgressBar.setProgress(0);
        mCountDownTimer = new CountDownTimer(15000,100) {

            @Override
            public void onTick(long msUntilFinished) {
                int i = (15000 - Math.toIntExact(msUntilFinished))/150;
                mProgressBar.setProgress(i);
            }

            @Override
            public void onFinish() {
                mProgressBar.setProgress(100);
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
                mRecordButton.clearAnimation();
                isCountDownOver = true;
                sendButton.setVisibility(View.VISIBLE);
                Log.i(LOG_TAG, "Countdown done");
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
                         sendButton.setVisibility(View.GONE);
                         mCountDownTimer.start();
                         Log.i(LOG_TAG, "Finger down countdown" + isCountDownOver);
                     }
                     else if (event.getAction() == MotionEvent.ACTION_UP) {
                         if(!isCountDownOver) {
                             onRecord(mStartRecording);
                             mStartRecording = !mStartRecording;
                             mRecordButton.clearAnimation();
                             mCountDownTimer.cancel();
                             sendButton.setVisibility(View.VISIBLE);
                             Log.i(LOG_TAG, "Finger Up");
                         }
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

        //final FrameLayout includeSend = (FrameLayout)findViewById(R.id.includeSendMethod);
        //final Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);

        /*sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation bottomUp = AnimationUtils.loadAnimation(getAppContext(), R.anim.bottom_up);
                FrameLayout includeSend = (FrameLayout)findViewById(R.id.includeSendMethod);
                includeSend.startAnimation(bottomUp);
                includeSend.setVisibility(View.VISIBLE);
            }
        });*/

    }

    @Override
    public void onStop() {
        super.onStop();
        // In case the user quits the activity without stopping the recording
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
