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
import android.widget.TextView;
import android.widget.Toast;


import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

import android.widget.MediaController.MediaPlayerControl;




public class MainActivity extends Activity implements MediaPlayerControl 
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
        
        //setController();
        
    }
    
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection()
    {
    	@Override
    	public void onServiceConnected(ComponentName name, IBinder service) 
    	{
    	MusicBinder binder = (MusicBinder)service;
    	//get service
    	musicSrv = binder.getService();
    	//pass list
    	musicSrv.setList(songList);
    	musicBound = true;
    	}
 
    	@Override
    	public void onServiceDisconnected(ComponentName name) 
    	{
    	musicBound = false;
    	}
    };
    
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

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) 
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
    
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
    	
    	Intent intent = new Intent(this, SongPickedActivity.class);
    	TextView textViewArtist = (TextView) findViewById(R.id.song_artist);
        String message = textViewArtist.getText().toString();
        String message2 = view.getTag().toString();
        
        Integer index = Integer.parseInt(view.getTag().toString());
        
        Song currentSong  = (Song)songList.get(index);
        String message3 = currentSong.getArtist().toString() + " - " + currentSong.getTitle().toString();
        
        
        intent.putExtra(EXTRA_MESSAGE, message3);
    	
    	startActivity(intent);
    	
    	musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
    	
    	musicSrv.playSong();
    	
    	/*
    	setController();
    	
    	//musicSrv.getPosn();
    	
    	if(playbackPaused)
    	{
		    setController();
		    playbackPaused=false;
    	}
    	controller.show(0);
    	*/
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	//menu item selected
    	switch (item.getItemId()) 
    	{
    		case R.id.action_end:
    			stopService(playIntent);
    			musicSrv=null;
    			System.exit(0);
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



	@Override
	public void start() 
	{
		musicSrv.go();
		//controller.show(0);
	}



	@Override
	public void pause() 
	{
		playbackPaused=true;
		musicSrv.pausePlayer();
	}



	@Override
	public int getDuration() 
	{
		if(musicSrv!=null && musicBound && musicSrv.isPng())
		{
			return musicSrv.getDur();
		}
		else 
		{
			return 0;
		}
	}



	@Override
	public int getCurrentPosition() 
	{
		if(musicSrv!=null && musicBound && musicSrv.isPng())
		{
			return musicSrv.getPosn();
		}
		else 
		{
			return 0;
		}
	}



	@Override
	public void seekTo(int pos) 
	{
		musicSrv.seek(pos);
	}


	@Override
	public boolean isPlaying() 
	{
		if(musicSrv!=null && musicBound)
		{
			 return musicSrv.isPng();
		} 
		return false;
	}



	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public boolean canPause() 
	{
		return true;
	}

	@Override
	public boolean canSeekBackward() 
	{
		return true;
	}



	@Override
	public boolean canSeekForward() 
	{
		return true;
	}



	@Override
	public int getAudioSessionId() 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void setController()
	{
		//set the controller up
		controller = new MusicController(this);
		
		controller.setPrevNextListeners(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				playNext();
			}
		}, new View.OnClickListener() 
		{
		@Override
			public void onClick(View v) 
			{
				playPrev();
			}
		});
		
		//controller.setBackgroundColor(Color.CYAN);
		
		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
		
	}
	
	
	//play next
	private void playNext()
	{
		 musicSrv.playNext();
		 setController();
		 if(playbackPaused)
		 {
			 setController();
			 playbackPaused=false;
		 }
		 controller.show(0);
	}
	 
	//play previous
	private void playPrev()
	{
		musicSrv.playPrev();
		setController();
		if(playbackPaused)
		{
		    setController();
		    playbackPaused=false;
		}
		controller.show(0);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		paused=true;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		if(paused)
		{
			setController();
			paused=false;
		}
	}
    
	@Override
	protected void onStop() 
	{
		//controller.hide();
		super.onStop();
	}
	
	
	

    
}
