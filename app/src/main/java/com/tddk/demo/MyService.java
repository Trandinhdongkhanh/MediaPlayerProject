package com.tddk.demo;


import static com.tddk.demo.MyApplication.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import Model.Song;
import Utils.GetImageFromUrl;

public class MyService extends Service {
    private MediaPlayer mediaPlayer;
    private static final int FIRST_PLAY = 0;
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME = 2;
    public static final int ACTION_CLEAR = 3;
    public static final int ACTION_START = 4;
    public static final int ACTION_NEXT = 5;
    public static final int ACTION_PREV = 6;
    private boolean isPlaying;
    private ArrayList<Song> songList;
    private int pos;
    private Song mSong;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Khanh1307", "MyService onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Khanh1307", "MyService onDestroy");
        stopMusic();
        sendActionToActivity(FIRST_PLAY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Khanh1307", "MyService onStartCommand");
        songList = (ArrayList<Song>) intent.getSerializableExtra("songList");
        pos = intent.getIntExtra("pos", 0);
        int actionMusic = intent.getIntExtra("action_music_receiver", 0);

        if (songList != null) {
            if (pos >= songList.size()) {
                pos = 0;
            }
            if (pos < 0) {
                pos = songList.size() - 1;
            }
        }

        if (actionMusic == FIRST_PLAY) {
            if (songList != null) {
                mSong = songList.get(pos);
                startMusic(mSong);
                sendNotification(mSong);
                sendActionToActivity(ACTION_START);
            }
        }
        handleActionMusic(actionMusic);
        return START_NOT_STICKY;
    }


    private void startMusic(Song song) {
        if (isPlaying) {
            stopMusic();
        }
        mediaPlayer = new MediaPlayer();
        String source = song.getResource();
        setMediaSource(source);
        mediaPlayer.start();
        isPlaying = true;
    }

    private void stopMusic() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    private void playNext() {
        if (mediaPlayer != null && isPlaying) {
            pos++;
            if (pos >= songList.size()) {
                pos = 0;
            }
            stopMusic();
            mSong = songList.get(pos);
            startMusic(mSong);
            sendNotification(mSong);
        }
    }

    private void playPrev() {
        if (mediaPlayer != null && isPlaying) {
            pos--;
            if (pos < 0) {
                pos = songList.size() - 1;
            }
            stopMusic();
            mSong = songList.get(pos);
            startMusic(mSong);
            sendNotification(mSong);
        }
    }

    private void setMediaSource(String source) {
        try {
            mediaPlayer.setDataSource(source);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleActionMusic(int action) {
        switch (action) {
            case ACTION_PAUSE:
                pauseMusic();
                sendActionToActivity(ACTION_PAUSE);
                break;
            case ACTION_RESUME:
                resumeMusic();
                sendActionToActivity(ACTION_RESUME);
                break;
            case ACTION_CLEAR:
                stopSelf();
                sendActionToActivity(ACTION_CLEAR);
                break;
            case ACTION_NEXT:
                playNext();
                sendActionToActivity(ACTION_NEXT);
                break;
            case ACTION_PREV:
                playPrev();
                sendActionToActivity(ACTION_PREV);
                break;
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            sendNotification(mSong);
        }
    }

    private void resumeMusic() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            sendNotification(mSong);
        }
    }

    private void sendNotification(@NotNull Song song) {
        Intent intent = new Intent(this, PlayMediaActivity.class);
        intent.putExtra("songList", songList);
        intent.putExtra("pos", pos);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap bitmap;
        try {
            bitmap = new GetImageFromUrl().execute(song.getImage()).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        MediaSessionCompat mediaSession = new MediaSessionCompat(this, "tag");

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_stat_player)
                .addAction(R.drawable.ic_prev, "Previous", getPendingIntent(this, ACTION_PREV))
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying ? "Pause" : "Resume",
                        getPendingIntent(this, isPlaying ? ACTION_PAUSE : ACTION_RESUME))
                .addAction(R.drawable.ic_next, "Next", getPendingIntent(this, ACTION_NEXT))
                .addAction(R.drawable.ic_clear, "Clear", getPendingIntent(this, ACTION_CLEAR))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setShowActionsInCompactView(1)
                        .setShowActionsInCompactView(2)
                        .setShowActionsInCompactView(3)
                        .setMediaSession(mediaSession.getSessionToken())
                )
                .setContentIntent(resultPendingIntent)
                .setContentTitle(song.getTittle())
                .setContentText(song.getSingle())
                .setLargeIcon(bitmap)
                .build();

        startForeground(1, notification);
    }

    private PendingIntent getPendingIntent(Context context, int action) {

        Intent intent = new Intent(this, MyReceiver.class);
        intent.putExtra("songList", songList);
        intent.putExtra("pos", pos);
        intent.putExtra("action_music", action);

        return PendingIntent.getBroadcast(
                context.getApplicationContext(),
                action,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private void sendActionToActivity(int action) {
        Intent intent = new Intent("send_data_to_activity");
        Bundle bundle = new Bundle();
        bundle.putSerializable("song", mSong);
        bundle.putBoolean("status", isPlaying);
        bundle.putInt("action_music", action);

        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}