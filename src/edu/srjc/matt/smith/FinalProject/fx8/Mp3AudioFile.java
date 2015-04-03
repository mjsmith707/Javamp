/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * Mp3AudioFile class
 * Internal class used to represent mp3 files
*/

// Mp3 Tag Reader
// http://www.jthink.net/jaudiotagger/index.jsp
// Completely awesome.

package edu.srjc.matt.smith.FinalProject.fx8;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.images.Artwork;

public class Mp3AudioFile extends AudioFile
{
    public static HashMap<String, String> mp3Genres = new HashMap<>();
    
    Mp3AudioFile(File fHandle)
    {
        super();
        loadHashmap();
        try
        {
            String probeContentType = Files.probeContentType(fHandle.toPath());
            if (probeContentType == null)
            {
                throw new InvalidParameterException("Unsupported File Type");
            }
            if (!probeContentType.equals("audio/mpeg"))
            {
                throw new InvalidParameterException("Unsupported File Type");
            }
            super.setFilePath(fHandle.getAbsolutePath());
            super.setMimeType(probeContentType);
            readID3Tags(fHandle);
        }
        catch (IOException ex)
        {
            throw new InvalidParameterException("Unsupported File Type");
        }
    }
    
    Mp3AudioFile()
    {
        super();
        loadHashmap();
    }
    
    private static void loadHashmap()
    {
        mp3Genres.put("(0)", "Blues");
        mp3Genres.put("(1)", "Classic Rock");
        mp3Genres.put("(2)", "Country");
        mp3Genres.put("(3)", "Dance");
        mp3Genres.put("(4)", "Disco");
        mp3Genres.put("(5)", "Funk");
        mp3Genres.put("(6)", "Grunge");
        mp3Genres.put("(7)", "Hip-Hop");
        mp3Genres.put("(8)", "Jazz");
        mp3Genres.put("(9)", "Metal");
        mp3Genres.put("(10)", "New Age");
        mp3Genres.put("(11)", "Oldies");
        mp3Genres.put("(12)", "Other");
        mp3Genres.put("(13)", "Pop");
        mp3Genres.put("(14)", "R&B");
        mp3Genres.put("(15)", "Rap");
        mp3Genres.put("(16)", "Reggae");
        mp3Genres.put("(17)", "Rock");
        mp3Genres.put("(18)", "Techno");
        mp3Genres.put("(19)", "Industrial");
        mp3Genres.put("(20)", "Alternative");
        mp3Genres.put("(21)", "Ska");
        mp3Genres.put("(22)", "Death Metal");
        mp3Genres.put("(23)", "Pranks");
        mp3Genres.put("(24)", "Soundtrack");
        mp3Genres.put("(25)", "Euro-Techno");
        mp3Genres.put("(26)", "Ambient");
        mp3Genres.put("(27)", "Trip-Hop");
        mp3Genres.put("(28)", "Vocal");
        mp3Genres.put("(29)", "Jazz+Funk");
        mp3Genres.put("(30)", "Fusion");
        mp3Genres.put("(31)", "Trance");
        mp3Genres.put("(32)", "Classical");
        mp3Genres.put("(33)", "Instrumental");
        mp3Genres.put("(34)", "Acid");
        mp3Genres.put("(35)", "House");
        mp3Genres.put("(36)", "Game");
        mp3Genres.put("(37)", "Sound Clip");
        mp3Genres.put("(38)", "Gospel");
        mp3Genres.put("(39)", "Noise");
        mp3Genres.put("(40)", "AlternRock");
        mp3Genres.put("(41)", "Bass");
        mp3Genres.put("(42)", "Soul");
        mp3Genres.put("(43)", "Punk");
        mp3Genres.put("(44)", "Space");
        mp3Genres.put("(45)", "Meditative");
        mp3Genres.put("(46)", "Instrumental Pop");
        mp3Genres.put("(47)", "Instrumental Rock");
        mp3Genres.put("(48)", "Ethnic");
        mp3Genres.put("(49)", "Gothic");
        mp3Genres.put("(50)", "Darkwave");
        mp3Genres.put("(51)", "Techno-Industrial");
        mp3Genres.put("(52)", "Electronic");
        mp3Genres.put("(53)", "Pop-Folk");
        mp3Genres.put("(54)", "Eurodance");
        mp3Genres.put("(55)", "Dream");
        mp3Genres.put("(56)", "Southern Rock");
        mp3Genres.put("(57)", "Comedy");
        mp3Genres.put("(58)", "Cult");
        mp3Genres.put("(59)", "Gangsta");
        mp3Genres.put("(60)", "Top 40");
        mp3Genres.put("(61)", "Christian Rap");
        mp3Genres.put("(62)", "Pop/Funk");
        mp3Genres.put("(63)", "Jungle");
        mp3Genres.put("(64)", "Native American");
        mp3Genres.put("(65)", "Cabaret");
        mp3Genres.put("(66)", "New Wave");
        mp3Genres.put("(67)", "Psychadelic");
        mp3Genres.put("(68)", "Rave");
        mp3Genres.put("(69)", "Showtunes");
        mp3Genres.put("(70)", "Trailer");
        mp3Genres.put("(71)", "Lo-Fi");
        mp3Genres.put("(72)", "Tribal");
        mp3Genres.put("(73)", "Acid Punk");
        mp3Genres.put("(74)", "Acid Jazz");
        mp3Genres.put("(75)", "Polka");
        mp3Genres.put("(76)", "Retro");
        mp3Genres.put("(77)", "Musical");
        mp3Genres.put("(78)", "Rock & Roll");
        mp3Genres.put("(79)", "Hard Rock");
    }
    
    public static String getMp3GenreFromID(String id)
    {
        String result = mp3Genres.get(id);
        if (result == null)
        {
            return id;
        }
        return result;
    }
    
    public static String getMp3IDFromCanonical(String canonical)
    {
        for (Map.Entry<String, String> entry : mp3Genres.entrySet())
        {
            if (canonical.equals(entry.getValue()))
            {
                return entry.getKey();
            }
        }
        return canonical;
    }
    
    private void readID3Tags(File fHandle) throws IOException
    {
        try
        {
            MP3File mp3 = (MP3File)AudioFileIO.read(fHandle);
            if (mp3.hasID3v2Tag())
            {
                AbstractID3v2Tag iD3v2Tag = mp3.getID3v2Tag();
                super.setArtist(iD3v2Tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST));
                if (super.getArtist().equals(""))
                {
                    if (mp3.hasID3v1Tag())
                    {
                        ID3v1Tag iD3v1Tag = mp3.getID3v1Tag();
                        super.setArtist(iD3v1Tag.getFirstArtist());
                    }
                }
                super.setAlbum(iD3v2Tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM));
                if (super.getAlbum().equals(""))
                {
                    if (mp3.hasID3v1Tag())
                    {
                        ID3v1Tag iD3v1Tag = mp3.getID3v1Tag();
                        super.setAlbum(iD3v1Tag.getFirstAlbum());
                    }
                }
                super.setGenre(getMp3GenreFromID(iD3v2Tag.getFirst(ID3v24Frames.FRAME_ID_GENRE)));
                if (super.getGenre().equals(""))
                {
                    if (mp3.hasID3v1Tag())
                    {
                        ID3v1Tag iD3v1Tag = mp3.getID3v1Tag();
                        super.setGenre(getMp3GenreFromID(iD3v1Tag.getFirstGenre()));
                    }
                }
                super.setTitle(iD3v2Tag.getFirst(ID3v24Frames.FRAME_ID_TITLE));
                if (super.getTitle().equals(""))
                {
                    if (mp3.hasID3v1Tag())
                    {
                        ID3v1Tag iD3v1Tag = mp3.getID3v1Tag();
                        super.setTitle(iD3v1Tag.getFirstTitle());
                    }
                }
                super.setYear(iD3v2Tag.getFirst(ID3v24Frames.FRAME_ID_YEAR));
                if (super.getYear().equals(""))
                {
                    if (mp3.hasID3v1Tag())
                    {
                        ID3v1Tag iD3v1Tag = mp3.getID3v1Tag();
                        setYear(iD3v1Tag.getFirstYear());
                    }
                }
                super.setTrackNumber(iD3v2Tag.getFirst(ID3v24Frames.FRAME_ID_TRACK));
                super.setLength(iD3v2Tag.getFirst(ID3v24Frames.FRAME_ID_LENGTH));
                super.setDiscNumber(iD3v2Tag.getFirst(FieldKey.DISC_NO));
                org.jaudiotagger.audio.AudioFile mp3file = (org.jaudiotagger.audio.AudioFile)AudioFileIO.read(fHandle);
                int minutes = mp3file.getAudioHeader().getTrackLength()/60;
                int seconds = mp3file.getAudioHeader().getTrackLength()%60;
                super.setLength(String.format("%d:%02d", minutes, seconds));
            }
            else if (mp3.hasID3v1Tag())
            {
                ID3v1Tag iD3v1Tag = mp3.getID3v1Tag();
                super.setArtist(iD3v1Tag.getFirstArtist());
                super.setAlbum(iD3v1Tag.getFirstAlbum());
                super.setGenre(iD3v1Tag.getFirstGenre());
                super.setTitle(iD3v1Tag.getFirstTitle());
                super.setYear(iD3v1Tag.getFirstYear());
                org.jaudiotagger.audio.AudioFile mp3file = (org.jaudiotagger.audio.AudioFile)AudioFileIO.read(fHandle);
                int minutes = mp3file.getAudioHeader().getTrackLength()/60;
                int seconds = mp3file.getAudioHeader().getTrackLength()%60;
                super.setLength(String.format("%d:%02d", minutes, seconds));
            }
        }
        catch (IOException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex)
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
            MP3File mp3 = (MP3File)AudioFileIO.read(fileHandle);
            if (mp3.hasID3v2Tag())
            {
                AbstractID3v2Tag iD3v2Tag = mp3.getID3v2Tag();
                Artwork artwork = iD3v2Tag.getFirstArtwork();
                BufferedImage image = (BufferedImage)artwork.getImage();
                return image;
            }
        }
        catch (NullPointerException | CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex)
        {
            return null;
        }
        return null;
    }
    
    @Override
    public void writeTags()
    {
        try 
        {
            File fHandle = new File(this.getFilePath());
            MP3File mp3 = (MP3File)AudioFileIO.read(fHandle);
            AbstractID3v2Tag iD3v2Tag = mp3.getID3v2Tag();
            if (iD3v2Tag == null)
            {
                mp3.createDefaultTag();
                iD3v2Tag = mp3.getID3v2Tag();
            }
            iD3v2Tag.setField(FieldKey.ARTIST, super.getArtist());
            iD3v2Tag.setField(FieldKey.ALBUM, super.getAlbum());
            iD3v2Tag.setField(FieldKey.GENRE, getMp3IDFromCanonical(super.getGenre()));
            iD3v2Tag.setField(FieldKey.DISC_NO, super.getDiscNumber());
            iD3v2Tag.setField(FieldKey.TRACK, super.getTrackNumber());
            iD3v2Tag.setField(FieldKey.TITLE, super.getTitle());
            iD3v2Tag.setField(FieldKey.YEAR, super.getYear());
            mp3.commit();
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
            MP3File mp3 = (MP3File)AudioFileIO.read(fHandle);
            AbstractID3v2Tag iD3v2Tag = mp3.getID3v2Tag();
            if (iD3v2Tag == null)
            {
                mp3.createDefaultTag();
                iD3v2Tag = mp3.getID3v2Tag();
            }
            
            Artwork artwork = org.jaudiotagger.tag.images.StandardArtwork.createArtworkFromFile(image);
            iD3v2Tag.deleteArtworkField();
            iD3v2Tag.setField(artwork);
            mp3.commit();
        } 
        catch (Exception ex) 
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
        if (obj instanceof Mp3AudioFile)
        {
            AudioFile operand = (AudioFile) obj;
            return (super.getFilePath().equals(operand.getFilePath()) && super.getMimeType().equals(operand.getMimeType()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(super.getMimeType());
        hash = 59 * hash + Objects.hashCode(super.getFilePath());
        return hash;
    }
}