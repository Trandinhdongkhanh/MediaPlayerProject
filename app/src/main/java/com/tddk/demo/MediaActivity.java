package com.tddk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import Adapter.NewSongAdapter;
import Interface.IClickItem;
import Model.Song;

public class MediaActivity extends AppCompatActivity {
    ImageView img_home;
    ImageView signOut;
    ImageView img_upload;
    RecyclerView recyclerViewNew;
    private NewSongAdapter newSongAdapter;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    ArrayList<Song> listSong = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_layout);
        img_home = findViewById(R.id.imageViewhome);
        img_upload = findViewById(R.id.image_upload);
        signOut = findViewById(R.id.sign_out);
        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("audios");
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                listSong.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Song song = postSnapshot.getValue(Song.class);
                    song.setKey(postSnapshot.getKey());
                    listSong.add(song);
                }
                newSongAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MediaActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MediaActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
            }
        });
        recyclerViewNew = findViewById(R.id.recycle_view_new);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerViewNew.setLayoutManager(linearLayoutManager1);

        newSongAdapter = new NewSongAdapter(listSong, new IClickItem() {
            @Override
            public void onClickItemSong(ArrayList<Song> songList, int pos) {
                onCLickGoToPlayMedia(songList, pos);
            }
        });
        newSongAdapter.setData(listSong);
        recyclerViewNew.setAdapter(newSongAdapter);

        img_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        img_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaActivity.this, UploadFileActivity.class);
                startActivity(intent);
            }
        });


    }

    private void onCLickGoToPlayMedia(ArrayList<Song> songList, int pos) {
        Intent intent = new Intent(this, PlayMediaActivity.class);
        intent.putExtra("songList", songList);
        intent.putExtra("pos", pos);
        startActivity(intent);
    }

}
