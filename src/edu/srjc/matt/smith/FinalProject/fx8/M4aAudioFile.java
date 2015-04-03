/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * M4aAudioFile class
 * Internal class used to represent m4a files
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.Objects;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;

public class M4aAudioFile extends AudioFile
{
    M4aAudioFile(File fHandle)
    {
        super();
        try
        {
            String probeContentType = Files.probeContentType(fHandle.toPath());
            if (probeContentType == null)
            {
                throw new InvalidParameterException("Unsupported File Type");
            }
            if (!probeContentType.equals("audio/m4a"))
            {
                throw new InvalidParameterException("Unsupported File Type");
            }
            super.setFilePath(fHandle.getAbsolutePath());
            super.setMimeType(probeContentType);
            readM4ATags(fHandle);
        }
        catch (IOException ex)
        {
            throw new InvalidParameterException("Unsupported File Type");
        }
    }
    
    M4aAudioFile()
    {
        super();
    }

    private void readM4ATags(File fHandle)
    {
        try
        {
            org.jaudiotagger.audio.AudioFile m4a = (org.jaudiotagger.audio.AudioFile)AudioFileIO.read(fHandle);
            Mp4Tag m4atag = (Mp4Tag)m4a.getTag();
            super.setArtist(m4atag.getFirst(Mp4FieldKey.ARTIST));
            super.setAlbum(m4atag.getFirst(Mp4FieldKey.ALBUM));
            super.setGenre(m4atag.getFirst(Mp4FieldKey.GENRE));
            super.setTitle(m4atag.getFirst(Mp4FieldKey.TITLE));
            super.setTrackNumber(m4atag.getFirst(Mp4FieldKey.TRACK));
            super.setYear(m4atag.getFirst(FieldKey.YEAR));
            super.setDiscNumber(m4atag.getFirst(Mp4FieldKey.DISCNUMBER));
            int minutes = m4a.getAudioHeader().getTrackLength()/60;
            int seconds = m4a.getAudioHeader().getTrackLength()%60;
            super.setLength(String.format("%d:%02d", minutes, seconds));
        }
        catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex)
        {
        }
        
        if (super.getTitle().equals(""))
        {
            String list[] = super.getFilePath().split("\\\\");
            super.setTitle(list[list.length-1]);
        }
    }
    
    @Override
    public BufferedImage getArtwork()
    {
        try
        {
            File fileHandle = new File(super.getFilePath());
            org.jaudiotagger.audio.AudioFile m4a = (org.jaudiotagger.audio.AudioFile)AudioFileIO.read(fileHandle);
            Mp4Tag m4atag = (Mp4Tag)m4a.getTag();
            Artwork artwork = m4atag.getFirstArtwork();
            BufferedImage image = (BufferedImage)artwork.getImage();
            return image;
        }
        catch (NullPointerException | CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex)
        {
            return null;
        }
    }
    
    @Override
    public void writeTags()
    {
        try 
        {
            File fHandle = new File(this.getFilePath());
            org.jaudiotagger.audio.AudioFile m4a = (org.jaudiotagger.audio.AudioFile)AudioFileIO.read(fHandle);
            Mp4Tag m4atag = (Mp4Tag)m4a.getTag();
            if (m4atag == null)
            {
                m4a.createDefaultTag();
                m4atag = (Mp4Tag)m4a.getTag();
            }
            m4atag.setField(Mp4FieldKey.ARTIST, super.getArtist());
            m4atag.setField(Mp4FieldKey.ALBUM, super.getAlbum());
            m4atag.setField(Mp4FieldKey.GENRE_CUSTOM, super.getGenre());
            m4atag.setField(Mp4FieldKey.DISCNUMBER, super.getDiscNumber());
            m4atag.setField(Mp4FieldKey.TRACK, super.getTrackNumber());
            m4atag.setField(Mp4FieldKey.TITLE, super.getTitle());
            m4atag.setField(FieldKey.YEAR, super.getYear());
            m4a.commit();
        } 
        catch (CannotWriteException | CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) 
        {
            if ((ex instanceof CannotReadException) || (ex instanceof ReadOnlyFileException) || (ex instanceof IOException))
            {
                throw new RuntimeException("Unable to read file.");
            }
            else if ((ex instanceof TagException) || (ex instanceof InvalidAudioFrameException))
            {
                throw new InvalidParameterException("Invalid fields entered: " + ex.getMessage());
            }
            else if (ex instanceof CannotWriteException)
            {
                throw new RuntimeException("Unable to write to file.");
            }
            else
            {
                throw new RuntimeException(ex.getMessage());
            }
        }
    }
    
    @Override
    public void writeAlbumArt(File image)
    {
        if (image == null)
        {
            return;
        }
        try 
        {
            File fHandle = new File(this.getFilePath());
            org.jaudiotagger.audio.AudioFile m4a = (org.jaudiotagger.audio.AudioFile)AudioFileIO.read(fHandle);
            Mp4Tag m4atag = (Mp4Tag)m4a.getTag();
            if (m4atag == null)
            {
                m4a.createDefaultTag();
                m4atag = (Mp4Tag)m4a.getTag();
            }
            
            Artwork artwork = org.jaudiotagger.tag.images.StandardArtwork.createArtworkFromFile(image);
            m4atag.deleteArtworkField();
            m4atag.setField(artwork);
            m4a.commit();
        } 
        catch (CannotWriteException | CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) 
        {
            if ((ex instanceof CannotReadException) || (ex instanceof ReadOnlyFileException) || (ex instanceof IOException))
            {
                throw new RuntimeException("Unable to read file.");
            }
            else if ((ex instanceof TagException) || (ex instanceof InvalidAudioFrameException))
            {
                throw new InvalidParameterException("Invalid fields entered: " + ex.getMessage());
            }
            else if (ex instanceof CannotWriteException)
            {
                throw new RuntimeException("Unable to write to file.");
            }
            else
            {
                throw new RuntimeException(ex.getMessage());
            }
        }
    }
    
    @Override
    public boolean equals(Object obj) 
    {
        if (obj instanceof M4aAudioFile)
        {
            AudioFile operand = (AudioFile) obj;
            return (super.getFilePath().equals(operand.getFilePath()) && super.getMimeType().equals(operand.getMimeType()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(super.getMimeType());
        hash = 59 * hash + Objects.hashCode(super.getFilePath());
        return hash;
    }
}