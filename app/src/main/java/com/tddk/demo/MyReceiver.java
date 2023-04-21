package com.tddk.demo;

import static com.tddk.demo.MyService.ACTION_NEXT;
import static com.tddk.demo.MyService.ACTION_PREV;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import Model.Song;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int actionMusic = intent.getIntExtra("action_music", 0);
        ArrayList<Song> songList = (ArrayList<Song>) intent.getSerializableExtra("songList");
        int pos = intent.getIntExtra("pos", 0);

        Intent intentService = new Intent(context, MyService.class);
        intentService.putExtra("songList", songList);
        intentService.putExtra("pos", pos);
        intentService.putExtra("action_music_receiver", actionMusic);
        context.startService(intentService);
    }
}
