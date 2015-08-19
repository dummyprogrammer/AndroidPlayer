package com.player.licenta.androidplayer;

import com.player.licenta.androidplayer.MusicService.MusicBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.MediaController.MediaPlayerControl;

public class SongPickedActivity extends Activity
{
	private boolean musicBound=false;
	private boolean paused=false, playbackPaused=false;
	private MusicController controller;
	private ImageView coverArt;
	private String songFilePath;
	private MusicService musicSrv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
 		super.onCreate(savedInstanceState);

		setContentView(R.layout.song_picked);

		// Get the message from the intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		songFilePath = extras.getString("SONG_PATH");
		String songArtist = extras.getString("SONG_ARTIST");
		String songTitle = extras.getString("SONG_TITLE");

		coverArt = (ImageView)findViewById(R.id.coverArt);

		String title = songArtist + " - " + songTitle;
		setTitle(title);

		extractAlbumArt();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.song_picked, menu);
		return true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

/*	@Override
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

	public void extractAlbumArt()
	{
		android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(songFilePath);

		byte[] data = mmr.getEmbeddedPicture();
		if (data != null)
		{
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			coverArt.setImageBitmap(bitmap); //associated cover art in bitmap
		}
		else
		{
			coverArt.setImageResource(R.drawable.fallback_cover); //any default cover resourse folder
		}

		coverArt.setAdjustViewBounds(true);
		coverArt.setLayoutParams(new RelativeLayout.LayoutParams(500, 500));
	}

	//connect to the service
	private ServiceConnection musicConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			MusicBinder binder = (MusicBinder)service;
			musicSrv = binder.getService();
			//musicSrv.setList(songList);
			musicBound = true;
			setController();
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			musicBound = false;
		}
	};

	private void setController()
	{		//set the controller up
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
