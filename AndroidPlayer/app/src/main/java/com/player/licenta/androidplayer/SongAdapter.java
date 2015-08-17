package com.player.licenta.androidplayer;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SongAdapter extends BaseAdapter 
{
	 
	private ArrayList<Song> songs;
	private LayoutInflater songInf;
	
	private LinearLayout songLay;
	
	private TextView artistView;
	private TextView songView;
	private Song currSong;
	
	
	public SongAdapter(Context c, ArrayList<Song> theSongs)
	{
		songs=theSongs;
		songInf=LayoutInflater.from(c);
	}
	
	
	@Override
	public int getCount() 
	{
		return songs.size();
	}
	 
	@Override
	public Object getItem(int arg0) 
	{
	    // TODO Auto-generated method stub
	    return null;
	}
	 
	@Override
	public long getItemId(int arg0) 
	{
	    // TODO Auto-generated method stub
	    return 0;
	}
	 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		//map to song layout
		songLay = (LinearLayout)songInf.inflate
	      (R.layout.song, parent, false);
		
		//get title and artist views
		songView = (TextView)songLay.findViewById(R.id.song_title);
		artistView = (TextView)songLay.findViewById(R.id.song_artist);
		
		//get song using position
		currSong = songs.get(position);
		
		//get title and artist strings
		songView.setText(currSong.getTitle());
		artistView.setText(currSong.getArtist());
		
		/*
		songLay.setOnClickListener(new OnClickListener() 
		{
            public void onClick(View v) 
            {
            	//do stuff here
            	artistView.setBackgroundColor(Color.CYAN);
            }
        });
		*/
		
		//set position as tag
		songLay.setTag(position);
		return songLay;
	}
	
	

 
}