/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * AudioFile class
 * Superclass for all supported formats in the music player.
 * Probably needs to be changed to an abstract base class.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class AudioFile
{
    private String mimeType;
    private String filePath;
    private String artist;
    private String album;
    private String genre;
    private String discNumber;
    private String trackNumber;
    private String title;
    private String year;
    private String length;
    
    static public enum SUPPORTED_TYPES
    {
        audio_mpeg, audio_m4a, audio_wav
    };

    AudioFile()
    {
        this.mimeType = "";
        this.filePath = "";
        this.artist = "";
        this.album = "";
        this.genre = "";
        this.discNumber = "";
        this.trackNumber = "";
        this.title = "";
        this.year = "";
        this.length = "";
    }
    
    public void setMimeType(String mimetype)
    {
        this.mimeType = mimetype;
    }
    
    public String getMimeType()
    {
        return this.mimeType;
    }
    
    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
    
    public String getFilePath()
    {
        return this.filePath;
    }
    
    public void setArtist(String artist)
    {
        this.artist = artist;
    }
    
    public String getArtist()
    {
        return this.artist;
    }
    
    public void setAlbum(String album)
    {
        this.album = album;
    }
    
    public String getAlbum()
    {
        return this.album;
    }
    
    public void setGenre(String genre)
    {
        this.genre = genre;
    }
    
    public String getGenre()
    {
        return this.genre;
    }
    
    public void setDiscNumber(String discNumber)
    {
        this.discNumber = discNumber;
    }
    
    public String getDiscNumber()
    {
        return this.discNumber;
    }
    
    public void setTrackNumber(String trackNumber)
    {
        this.trackNumber = trackNumber;
    }
    
    public String getTrackNumber()
    {
        return this.trackNumber;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public String getTitle()
    {
        return this.title;
    }
    
    public void setYear(String year)
    {
        this.year = year;
    }
    
    public String getYear()
    {
        return this.year;
    }
    
    public void setLength(String length)
    {
        this.length = length;
    }
    
    public String getLength()
    {
        return this.length;
    }
    
    public BufferedImage getArtwork()
    {
        return null;
    }
    
    public void writeTags()
    {
        return;
    }
    
    public void writeAlbumArt(File image)
    {
        return;
    }
    
    @Override
    public String toString()
    {
        return String.format("mimeType: %s, Artist: %s, Album: %s, Title: %s, Genre: %s, Track #: %s, Year: %s, ", this.mimeType, this.artist, this.album, this.title, this.genre, this.trackNumber, this.year);
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj instanceof AudioFile)
        {
            AudioFile operand = (AudioFile) obj;
            return (this.filePath.equals(operand.filePath) && this.mimeType.equals(operand.mimeType));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.mimeType);
        hash = 59 * hash + Objects.hashCode(this.filePath);
        return hash;
    }
}