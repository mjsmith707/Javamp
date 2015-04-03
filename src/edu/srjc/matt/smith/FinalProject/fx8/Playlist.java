/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * Playlist Class
 * Internal data structure for playlists.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Playlist
{
    private ArrayList<AudioFile> playlist;
    private int indexPosition;
    private boolean shuffle;
    private boolean repeat;
    private Random rng;
    
    Playlist()
    {
        this.playlist = new ArrayList<>();
        this.indexPosition = 0;
        this.rng = new Random();
        this.shuffle = false;
        this.repeat = false;
    }
    
    private int getRandomIndex()
    {
        if (this.playlist.isEmpty())
        {
            return 0;
        }
        return this.rng.nextInt(this.playlist.size());
        
    }
    
    public ArrayList<AudioFile> getPlaylist()
    {
        return this.playlist;
    }
    
    public void clearPlaylist()
    {
        this.playlist.clear();
        this.indexPosition = 0;
    }
    
    public void appendTracksAt(int targetPosition, ArrayList<AudioFile> newList)
    {
        if (targetPosition > this.playlist.size())
        {
            for (AudioFile newTrack : newList)
            {
                this.playlist.add(newTrack);
            }
        }
        else if (targetPosition <= 0)
        {
            targetPosition = 0;
            for (AudioFile newTrack : newList)
            {
                this.playlist.add(targetPosition, newTrack);
                targetPosition++;
            }
        }
        else
        {
            for (AudioFile newTrack : newList)
            {
                this.playlist.add(targetPosition, newTrack);
                targetPosition++;
            }
        }
    }
    
    public void addTrack(AudioFile newTrack)
    {
        this.playlist.add(newTrack);
    }
    
    public void addTrackAt(int index, AudioFile newTrack)
    {
        if ((index > this.playlist.size()) || index < 0)
        {
            addTrack(newTrack);
        }
        else
        {
            try
        {
            this.playlist.add(index, newTrack);
        }
            catch (IndexOutOfBoundsException e)
            {
                throw new RuntimeException("Error target index out of bounds.");
            }
        }
        
    }
    
    public void nextTrack()
    {
        if (this.indexPosition == -1)
        {
            if (!this.playlist.isEmpty())
            {
                this.indexPosition = 0;
            }
        }
        else if (shuffle)
        {
            if (this.playlist.isEmpty())
            {
                this.indexPosition = -1;
            }
            else if (!repeat && (this.indexPosition == this.playlist.size()-1))
            {
                this.indexPosition = -1;
            }
            else
            {
                if (this.playlist.size() > 1)
                {
                    int tempIndex = 0;
                    do
                    {
                        tempIndex = getRandomIndex();
                    } while (tempIndex == this.indexPosition);
                    this.indexPosition = tempIndex;
                }
            }
        }
        else if ((repeat) && (this.indexPosition == this.playlist.size()-1))
        {
            this.indexPosition = 0;
        }
        else if (this.indexPosition < this.playlist.size()-1)
        {
            this.indexPosition++;
        }
        else if (this.indexPosition == this.playlist.size()-1)
        {
            this.indexPosition = -1;
        }
    }
    
    public void previousTrack()
    {
        if (shuffle)
        {
            if (this.playlist.isEmpty())
            {
                this.indexPosition = -1;
            }
            else
            {
                if (this.playlist.size() > 1)
                {
                    int tempIndex = 0;
                    do
                    {
                        tempIndex = getRandomIndex();
                    } while (tempIndex == this.indexPosition);
                    this.indexPosition = tempIndex;
                }
            }
        }
        else if (repeat)
        {
            if (this.indexPosition == 0)
            {
                this.indexPosition = this.playlist.size()-1;
            }
        }
        if (this.indexPosition > 0)
        {
            this.indexPosition--;
        }
    }
    
    public AudioFile getCurrentTrack()
    {
        AudioFile track = null;
        try
        {
            track = this.playlist.get(this.indexPosition);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new RuntimeException("Error empty playlist.");
        }
        return track;
    }
    
    public AudioFile getTrackAt(int index)
    {
        AudioFile track = null;
        try
        {
            track = this.playlist.get(index);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new RuntimeException("Error index position out of bounds.");
        }
        return track;
    }
    
    public void removeTrackAt(int index)
    {
        AudioFile track = null;
        try
        {
            this.playlist.remove(index);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new RuntimeException("Error index position out of bounds.");
        }
    }
    
    public void setIndexPosition(int newPosition)
    {
        if (newPosition < 0)
        {
            this.indexPosition = 0;
        }
        else if (newPosition > playlist.size())
        {
            this.indexPosition = playlist.size()-1;
        }
        else
        {
            this.indexPosition = newPosition;
        }
    }
    
    public int getIndexPosition()
    {
        return this.indexPosition;
    }
    
    public boolean isShuffling()
    {
        return this.shuffle;
    }
    
    public boolean isRepeating()
    {
        return this.repeat;
    }
    
    public void setShuffle(boolean shuffle)
    {
        this.shuffle = shuffle;
    }
    
    public void setRepeat(boolean repeat)
    {
        this.repeat = repeat;
    }
    
    public void loadM3UPlaylist(File fHandle)
    {
        if (fHandle == null)
        {
            return;
        }
        
        try
        {
            Scanner istream = new Scanner(fHandle);
            while (istream.hasNextLine())
            {
                File targetHandle = new File(istream.nextLine());
                String contentType = Files.probeContentType(targetHandle.toPath());
                if (contentType == null)
                {
                    continue;
                }
                contentType = contentType.replace("/", "_");
                if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_mpeg.toString()))
                {
                    try 
                    {
                        Mp3AudioFile newFile = new Mp3AudioFile(targetHandle);
                        this.playlist.add(newFile);
                    }
                    catch (InvalidParameterException ex)
                    {
                    }
                }
                else if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_m4a.toString()))
                {
                    try 
                    {
                        M4aAudioFile newFile = new M4aAudioFile(targetHandle);
                        this.playlist.add(newFile);
                    }
                    catch (InvalidParameterException ex) 
                    {
                    }
                }
                else if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_wav.toString()))
                {
                    try 
                    {
                        PCMAudioFile newFile = new PCMAudioFile(targetHandle);
                        this.playlist.add(newFile);
                    }
                    catch (InvalidParameterException ex) 
                    {
                    }
                }
            }
            istream.close();
        }
        catch (IOException ex)
        {
        }

    }
    
    public String toM3UPlaylist()
    {
        String m3uPlaylist = "";
        for (AudioFile target : this.playlist)
        {
            m3uPlaylist += String.format("%s\n", target.getFilePath());
        }
        
        return m3uPlaylist;
    }
    
    @Override
    public String toString()
    {
        String shuffling = "false";
        String repeating = "false";
        if (shuffle)
        {
            shuffling = "true";
        }
        if (repeat)
        {
            repeating = "true";
        }
        return String.format("Class: Playlist, Shuffling: %s, Repeating: %s, Size: %d", shuffling, repeating, this.playlist.size());
    }
}
