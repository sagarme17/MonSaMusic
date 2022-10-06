package com.example.monsamusic;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;




public class MainActivity extends AppCompatActivity  {

    //mieb¿mbrks
    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allsongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configuracion de la barra de herramientas y el titulo de la app

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        //recyclerview
        recyclerView = findViewById(R.id.recyclerview);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isgranted->{
            if(isgranted){
                //fetch Songs
                fetchSongs();
            }
            else{
                userResponses();
            }
        });

        //launch storage permision on create
        storagePermissionLauncher.launch(permission);
    }

    private void userResponses() {
        if(ContextCompat.checkSelfPermission(this, permission)== PackageManager.PERMISSION_GRANTED){
            //fetchSongs
            fetchSongs();
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(shouldShowRequestPermissionRationale(permission)){
                //show an education
                //yss alert dialof
                new AlertDialog.Builder(this)
                        .setTitle("Resquesting Permission")
                        .setMessage("Allow us to fetch songs on your device")
                        .setPositiveButton("allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //request permission
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "You denied us to show songs", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
        else {
            Toast.makeText(this,"You canceled to show song",Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSongs() {
        //define a list to cary songs
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        }else{
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //define proyeccion
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,

        };
        //order
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + "DESC";

        //get tje songs
        try(Cursor cursor = getContentResolver().query(mediaStoreUri,projection,null,null,sortOrder))
        {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            //clear the previus loaded before adding loading again
            while(cursor.moveToNext()){
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumIdColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);


                Uri albumArtworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);

                //remove .mp3 del nombre aimprimir
                name = name.substring(0,name.lastIndexOf("."));

                //song item

                Song song = new Song(name,uri,albumArtworkUri,size,duration);
                //add song item to song list

                songs.add(song);

            }
// display songs
            showSongs(songs);



        }
    }

    private void showSongs(List<Song> songs) {
        if(songs.size() ==0){
            Toast.makeText(this, "No Songs",Toast.LENGTH_SHORT).show();
            return;


        }
        allsongs.clear();
        allsongs.addAll(songs);

        String title = getResources().getString(R.string.app_name)+ " - "+songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        //layout manager

        LinearLayoutManager layouManager  = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layouManager);

        //song adapter

        songAdapter = new SongAdapter(this,songs);
        //set the adapter to recyclerView
        recyclerView.setAdapter(songAdapter);

    }
}