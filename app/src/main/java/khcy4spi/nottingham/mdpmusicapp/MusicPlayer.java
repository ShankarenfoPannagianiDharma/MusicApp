package khcy4spi.nottingham.mdpmusicapp;

import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MusicPlayer extends AppCompatActivity
{
    private File currentDir;                                //current directory file
    private AdapterMusics songadapter;                      //adapter for songs list
    private AdapterMusics fileadapter;                      //adapter for folders list
    private MediaPlayer mdplayer = new MediaPlayer();       //mediaplayer for music- use from library
    private int songlength;                                 //integer for the song's play duration
    private boolean songplaying = false;                   //boolean if song is in the middle of playing
    private List<FileItem>Listfolders = new ArrayList<FileItem>(); //arraylist for folders
    private List<FileItem>Listfiles = new ArrayList<FileItem>();   //arraylist for files (filter just .mp3 extension)
    private List<FileItem>playlist = new ArrayList<FileItem>();    //arraylist for currently playing playlist
    private Handler updater = new Handler();                 //handler to update the seekbar by seconds
    private int currentindex;                               //index of song that is currently playing

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        //bind meadiaplayer to app


        //initial fill the listview
        currentDir = Environment.getExternalStorageDirectory();
        getitems(currentDir);
        setLVlisteners();

        //standard SD card filepath
        String SDPath = currentDir.getAbsolutePath();

        //add listeners to seekbar
        final SeekBar sbar = (SeekBar) findViewById(R.id.sb_songplay);
        sbar.setMax(0);
        sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            //listener when the seekbar seeker is moved
            @Override
            public void onProgressChanged(SeekBar seekBar, int seekpoint, boolean userinteracted)
            {
                if(mdplayer != null && songplaying && userinteracted)
                {mdplayer.seekTo(seekpoint * 1000);}
            }
            //override methods not used bu still needed to be declared
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //setup listener for mediaplayer when it completes
        mdplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                //run only when the mediaplayer does exist and is running
                if(mdplayer != null && songplaying) {
                    mdplayer.reset();
                    songplaying = false;

                    ToggleButton looptoggle = (ToggleButton) findViewById(R.id.tgl_looping);

                    if (!looptoggle.isChecked()) {
                        //ADVANCE TO NEXT SONG:
                        //move the song pointer
                        int targetsong = currentindex + 1;
                        //catch if the file is at the end of list or at the start- loop to the other end.
                        if (targetsong >= playlist.size()) {
                            targetsong -= playlist.size();
                        } else if (targetsong < 0) {
                            targetsong = playlist.size() - 1;
                        }

                        //get the data and pass it to play function
                        FileItem targetfile = playlist.get(targetsong);
                        //modify the current index
                        currentindex = targetsong;
                        //play the song!
                        playsong(targetfile);
                    } else {
                        //PLAY THE SONG AGAIN
                        //get the data and pass it to play function
                        FileItem targetfile = songadapter.getItem(currentindex);
                        playsong(targetfile);
                    }
                }
            }
        });

        //run the update method on ui thread
        MusicPlayer.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SeekBar sbar = (SeekBar) findViewById(R.id.sb_songplay);
                //only update when the song is playing
                if(mdplayer != null || songplaying == true)
                {
                    int sbarpos = mdplayer.getCurrentPosition()/1000;
                    sbar.setProgress(sbarpos);
                }
                updater.postDelayed(this,1000);
            }
        });

    }

    //function to get initial files and folders (Target SD card)
    private void getitems(File f)
    {
        //put files into arrayfiles and folders to arraydirs
        //pass arrayfiles to arrayadapter for listview
        File[] dirs = f.listFiles();                      //things seen from f directory (all files on that level)

        //send the filepath to textview
        TextView pathing = (TextView) findViewById(R.id.txt_folderpath);
        pathing.setText(currentDir.toString());

        //if the current directory isn't at root...
        if(!Objects.equals(currentDir.toString(), "/")) {
            //add the first option for folder to go back- the "..." directory
            String parentfolder = currentDir.getParent();
            Listfolders.add(new FileItem("...", parentfolder));
        }

        //place items to list
        try
        {
            //go through the directory items
            for(File selectedfile: dirs)
            {
                String filename = selectedfile.getName();
                String filepath = selectedfile.getAbsolutePath();

                //if selected file is a dir
                if(selectedfile.isDirectory())
                {
                    //add into arraylist for folders - create a new instance of Fileitem
                    Listfolders.add(new FileItem(filename, filepath));
                }
                else
                {
                    //get the extension of the file  (getName() also returns extension)
                    int indexofextension = filename.lastIndexOf('.');              //returns index of the extension
                    String fileextension = filename.substring(indexofextension+1);  //the extension string
                    String filelabel = filename.substring(0,indexofextension);      //the actual name without extension of file

                    //if not a dir but is an mp3 file
                    if(Objects.equals(fileextension, "mp3"))
                    {
                        //add into arraylist for mp3 files- new instance of Fileitem
                        Listfiles.add(new FileItem(filelabel, filepath));
                    }
                }
            }//if not folder or mp3 file, do nothing
        }
        catch(Exception e)
        {e.printStackTrace();}

        //change the help text to show the number of songs scanned in the folder:
        TextView songcounter = (TextView) findViewById(R.id.txt_numberofsongs);
        songcounter.setText("MP3 files found: "+Listfiles.size());

        //add adapter list to song list
        songadapter = new AdapterMusics(MusicPlayer.this, R.layout.listviewitems, Listfiles);
        ListView songview = (ListView) findViewById(R.id.lv_songlist);
        songview.setAdapter(songadapter);

        //add adapter list to directory list
        fileadapter = new AdapterMusics(MusicPlayer.this, R.layout.listviewitems, Listfolders);
        ListView folderview = (ListView) findViewById(R.id.lv_directorylist);
        folderview.setAdapter(fileadapter);
    }

    //method to add lsiteneres to both listviews- folders and songs lists
    public void setLVlisteners()
    {
        //add listener to folder listview
        ListView folderlist = (ListView) findViewById(R.id.lv_directorylist);
        folderlist.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                //do the folder relocation based on list item selected- full path of directory is
                //stated in te FileItem class
                FileItem selected = fileadapter.getItem(position);
                currentDir = new File(selected.getPath());

                //clear the lists
                Listfolders.clear();
                Listfiles.clear();

                //redo the scanner
                getitems(currentDir);
            }
        });

        //add listener to songs listview
        ListView songlist = (ListView) findViewById(R.id.lv_songlist);
        songlist.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                //clear the playlist
                playlist.clear();
                //import the next tracks to play
                for(FileItem nexttrack : Listfiles)
                {playlist.add(nexttrack);}

                //play the song selected through mediaplayer
                FileItem selected = songadapter.getItem(position);
                currentindex = position;

                //play the song through another function
                playsong(selected);
            }
        });
    }

    //independent method to play the song- parameter is the song file
    private void playsong(FileItem song)
    {
        //try to play
        try
        {
            //set song name to the text
            TextView songname = (TextView) findViewById(R.id.txt_songtitle);
            songname.setText(song.getName());

            //restart the mediaplayer and tell it to run the music file
            mdplayer.reset();
            mdplayer.setDataSource(song.getPath());
            mdplayer.prepare();
            mdplayer.start();

            //setup the seekbar
            int maxlength = mdplayer.getDuration()/1000;
            SeekBar sbar = (SeekBar) findViewById(R.id.sb_songplay);
            sbar.setMax(maxlength);

            //update the state
            songplaying = true;
        } catch (IOException e)
        {e.printStackTrace();}
    }


    //onclick method to switch visible lists- folders or songs (stated in xml)
    public void switchlist(View v)
    {
        FrameLayout songlist = (FrameLayout) findViewById(R.id.fly_MusicList);
        LinearLayout foldlist = (LinearLayout) findViewById(R.id.ll_folderbrowser);
        Button switchbutton = (Button) findViewById(R.id.btn_browse);
        //make the switch
        if(songlist.getVisibility() == View.VISIBLE)
        {
            songlist.setVisibility(View.GONE);
            foldlist.setVisibility(View.VISIBLE);
            switchbutton.setText("Songs");
        }
        else
        {
            songlist.setVisibility(View.VISIBLE);
            foldlist.setVisibility(View.GONE);
            switchbutton.setText("Folders");
        }
    }

    //onclick method for pause/play button (stated in xml)
    public void ppbutton(View v)
    {
        ImageButton pp = (ImageButton) findViewById(R.id.ib_middle);

        if(mdplayer.isPlaying())
        {
            pp.setImageResource(R.drawable.ic_play);
            mdplayer.pause();
            songlength = mdplayer.getCurrentPosition();
        }
        else if(songplaying)
        {
            pp.setImageResource(R.drawable.ic_pause);
            mdplayer.seekTo(songlength);
            mdplayer.start();
        }
    }

    //onclick method for back button (stated in xml)
    public void prevbutton(View v)
    {
        //only do anything when music is playing
        if(songplaying)
        {
            //if the played duration is less than 0.5 seconds, play the previous on the list
            if(mdplayer.getCurrentPosition() <= 500)
            {
                //move the song pointer
                int targetsong = currentindex - 1;
                //catch if the file is at the end of list or at the start- loop to the other end.
                if(targetsong >= playlist.size())
                {targetsong -= playlist.size();}
                else if(targetsong<0)
                {targetsong = playlist.size() -1;}

                //get the data and pass it to play function
                FileItem targetfile = playlist.get(targetsong);
                //modify the current index
                currentindex = targetsong;
                //play the song!
                playsong(targetfile);
            }
            else    //else replay
            {mdplayer.seekTo(0);}
        }

    }

    //onclick method for forward button (stated in xml)
    public void ffbutton(View v)
    {
        //only do anything when music is playing
        if(songplaying)
        {
            //move the song pointer
            int targetsong = currentindex + 1;
            //catch if the file is at the end of list or at the start- loop to the other end.
            if(targetsong >= playlist.size())
            {targetsong -=playlist.size();}
            else if(targetsong<0)
            {targetsong =playlist.size()-1;}

            //get the data and pass it to play function
            FileItem targetfile = playlist.get(targetsong);
            //modify the current index
            currentindex = targetsong;
            //play the song!
            playsong(targetfile);
        }
    }
}
