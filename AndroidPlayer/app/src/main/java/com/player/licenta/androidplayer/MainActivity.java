package com.player.licenta.androidplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.player.licenta.androidplayer.MusicService.MusicBinder;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity
{
	private ArrayList<Song> songList;
	private ListView songView;
	
	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound=false;
	
	private MusicController controller;
	
	private SongAdapter songAdt;
	
	private boolean paused=false, playbackPaused=false;
	
	public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

	//connect to the service
	private ServiceConnection musicConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			MusicBinder binder = (MusicBinder)service;
			musicSrv = binder.getService();
			musicSrv.setList(songList);
			musicBound = true;
			setController();
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			musicBound = false;
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
		getSongList();
        
        Collections.sort(songList, new Comparator<Song>()
        {
        	public int compare(Song a, Song b)
        	{
        		return a.getTitle().compareTo(b.getTitle());
        	}
        });
        
        songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }

    @Override
    protected void onStart() 
    {
    	super.onStart();
    	if(playIntent==null)
    	{
	        playIntent = new Intent(this, MusicService.class);
	        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
	        startService(playIntent);
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void getSongList() 
    {
    	  //retrieve song info
    	ContentResolver musicResolver = getContentResolver();
    	Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    	Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
    	
        if(musicCursor!=null && musicCursor.moveToFirst())
        {
			//get columns
			int titleColumn = musicCursor.getColumnIndex
			(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex
			(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex
			(android.provider.MediaStore.Audio.Media.ARTIST);
			//add songs to list
			do
			{
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist));
			}
			while (musicCursor.moveToNext());
        }
    }

	public void songPicked(View view)
    {
		//showCoverArtActivity(view);
		int songIndex = Integer.parseInt(view.getTag().toString());
		musicSrv.setSong(songIndex);
		musicSrv.playSong();
		controller.show();
	}

	private void showCoverArtActivity(View view)
	{
		Intent intent = new Intent(this, SongPickedActivity.class);
		TextView textViewArtist = (TextView) findViewById(R.id.song_artist);
		String message = textViewArtist.getText().toString();
		String message2 = view.getTag().toString();

		Integer index = Integer.parseInt(view.getTag().toString());

		Song currentSong  = (Song)songList.get(index);

		String songPath = musicSrv.getSongPath();

		//String message3 = currentSong.getArtist().toString() + " - " + currentSong.getTitle().toString() + " " + songPath;
		String message3 = songPath;

		intent.putExtra(EXTRA_MESSAGE, message3);

		startActivity(intent);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	//menu item selected
    	switch (item.getItemId()) 
    	{
    		case R.id.action_end:
    			finish();
    			break;
    			
    		case R.id.action_shuffle:
    			 musicSrv.setShuffle();
    			 break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() 
    {
	    stopService(playIntent);
	    musicSrv=null;
	    super.onDestroy();
    }

	private void setController()
	{
		//set the controller up
		controller = new MusicController(this);
		
		controller.setPrevNextListeners(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						musicSrv.playNext();
					}
				},
				new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						musicSrv.playPrev();
					}
				}
		);

		controller.setMediaPlayer(musicSrv);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
	}
}
