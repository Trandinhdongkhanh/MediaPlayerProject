package com.tddk.demo;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Model.Song;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlayMediaActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 10;
    private CircleImageView imageMusic;
    private TextView textViewName, textViewMusican, tvTitle, tvSingle;
    private RelativeLayout layout_bottom;
    private CircleImageView imgSong;
    private ImageView imgPlayOrPause, imgClose, btnPlay, btnNext, btnPrev, icPrev, icNext;
    private FloatingActionButton btnStopService;
    private Song mSong;
    private boolean isPlaying;
    private int pos;
    private ArrayList<Song> songList;
    private ObjectAnimator rotateAnimation;
    private AnimatorSet animatorSet;
    private int actionMusic = FIRST_PLAY;
    private static final int FIRST_PLAY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Khanh1307", "PlayMediaAct onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playmedia_layout);

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(broadcastReceiver, new IntentFilter("send_data_to_activity"));

        assignID();
        animatorSet = createRotateImage();

        songList = (ArrayList<Song>) getIntent().getSerializableExtra("songList");
        pos = getIntent().getIntExtra("pos", 0);
        mSong = songList.get(pos);

        loadImage(mSong);

        btnStopService.setOnClickListener((view -> checkPermission()));

        btnPlay.setOnClickListener(view -> {
            if (!isPlaying){
                if (actionMusic == FIRST_PLAY){
                    playSong(songList, pos);
                }
                else {
                    resumeSong();
                }
            }
            else{
                pauseSong();
            }
        });
        btnNext.setOnClickListener(view -> {
            playSong(songList, ++pos);
        });
        btnPrev.setOnClickListener(view -> {
            playSong(songList, --pos);
        });
    }

    private void assignID() {
        icPrev = findViewById(R.id.ic_prev);
        icNext = findViewById(R.id.ic_next);
        imageMusic = findViewById(R.id.imageMusic);
        btnNext = findViewById(R.id.next_button);
        btnPrev = findViewById(R.id.previous_button);
        textViewName = findViewById(R.id.tvName);
        textViewMusican = findViewById(R.id.tvmusician);
        btnPlay = findViewById(R.id.btn_start_stop_service);
        btnStopService = findViewById(R.id.btn_stop_service);
        layout_bottom = findViewById(R.id.layout_bottom);
        imgSong = findViewById(R.id.img_song);
        imgPlayOrPause = findViewById(R.id.playorpause);
        imgClose = findViewById(R.id.close);
        tvTitle = findViewById(R.id.tv_tittle);
        tvSingle = findViewById(R.id.tv_single_song);
    }

    private AnimatorSet createRotateImage() {
        rotateAnimation = ObjectAnimator.ofFloat(imageMusic, "rotation", 0f, 360f);
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotateAnimation);
        return animatorSet;
    }

    private void loadImage(Song song) {
        if (song != null) {
            textViewMusican.setText(song.getSingle());
            textViewName.setText(song.getTittle());
            Picasso.get()
                    .load(song.getImage().trim())
                    .into(imageMusic);
        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, REQUEST_PERMISSION_CODE);
            } else {
                startDownloadFile();
            }
        } else {
            startDownloadFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownloadFile();
            } else {
                Toast.makeText(this, "Permisson Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startDownloadFile() {
        String urlFile = mSong.getResource();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlFile));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(mSong.getTittle());
        request.setDescription("Download file...");

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()));

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            mSong = (Song) bundle.get("song");
            isPlaying = bundle.getBoolean("status");
            actionMusic = bundle.getInt("action_music");

            handleLayoutMusic(actionMusic);
        }
    };

    private void handleLayoutMusic(int actionMusic) {
        switch (actionMusic) {
            case MyService.ACTION_START:
                layout_bottom.setVisibility(View.VISIBLE);
                showInfoSong();
                setStatusButtonPlayOrPause();
                animatorSet.start();
                break;
            case MyService.ACTION_PAUSE:
                rotateAnimation.pause();
                btnPlay.setImageResource(R.drawable.play_button);
                setStatusButtonPlayOrPause();
                break;
            case MyService.ACTION_RESUME:
                rotateAnimation.resume();
                btnPlay.setImageResource(R.drawable.btn_pause);
                setStatusButtonPlayOrPause();
                break;
            case MyService.ACTION_CLEAR:
                layout_bottom.setVisibility(View.GONE);
                btnPlay.setImageResource(R.drawable.play_button);
                rotateAnimation.cancel();
                break;
            case MyService.ACTION_NEXT:
                showInfoSong();
                break;
            case MyService.ACTION_PREV:
                layout_bottom.setVisibility(View.VISIBLE);
                showInfoSong();
                break;
        }
    }

    private void showInfoSong() {
        if (mSong == null) {
            return;
        }

        loadImage(mSong);

        Picasso.get()
                .load(mSong.getImage().trim())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(imgSong);
        tvTitle.setText(mSong.getTittle());
        tvSingle.setText(mSong.getSingle());


        imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    sendActionToService(MyService.ACTION_PAUSE);
                } else {
                    sendActionToService(MyService.ACTION_RESUME);
                }
            }
        });
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendActionToService(MyService.ACTION_CLEAR);
            }
        });
        icNext.setOnClickListener(view -> {
            playSong(songList, ++pos);
        });
        icPrev.setOnClickListener(view -> {
            playSong(songList, --pos);
        });
    }

    private void setStatusButtonPlayOrPause() {
        if (isPlaying) {
            imgPlayOrPause.setImageResource(R.drawable.pause);
        } else {
            imgPlayOrPause.setImageResource(R.drawable.play);
        }
    }

    private void sendActionToService(int action) {
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("action_music_receiver", action);
        startService(intent);
    }


    private void pauseSong() {
        btnPlay.setImageResource(R.drawable.play_button);
        sendActionToService(MyService.ACTION_PAUSE);
    }

    private void resumeSong(){
        btnPlay.setImageResource(R.drawable.btn_pause);
        sendActionToService(MyService.ACTION_RESUME);
    }

    private void playSong(ArrayList<Song> songList, int pos) {
        btnPlay.setImageResource(R.drawable.btn_pause);
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("songList", songList);
        intent.putExtra("pos", pos);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(broadcastReceiver);
        Log.e("Khanh1307", "PlayMediaAct onDestroy");
    }

}