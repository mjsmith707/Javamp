/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * MediaLibrary Class
 * Handles interaction between the GUI components and AudioDB
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MediaLibrary
{
    private AudioDB audioDatabase;
    private HashMap<String, String> mp3Genres;
    private String libraryDBPath;
    
    MediaLibrary(String dbPath) throws IOException, SQLException
    {
        this.libraryDBPath = dbPath;
        
        instantiateDatabase();
        
        // These are the translations from the mp3 genre field to the
        // real genre name. Dumb design on their part to save a couple bytes...
        // http://axon.cs.byu.edu/~adam/gatheredinfo/organizedtables/musicgenrelist.php
        mp3Genres = new HashMap<>();
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
    
    private void instantiateDatabase() throws IOException, SQLException
    {
        File libraryDBHandle = new File(libraryDBPath);
        audioDatabase = new AudioDB(libraryDBHandle);
        try
        {
            audioDatabase.createDB();
        }
        catch (SQLException e)
        {
            if (!e.getMessage().equals("table \"AudioDB\" already exists"))
            {
                throw e;
            }
        }
    }
    
    public String getMp3GenreFromID(String id)
    {
        String result = mp3Genres.get(id);
        if (result == null)
        {
            return id;
        }
        return result;
    }
    
    public String getMp3IDFromCanonical(String canonical)
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
    
    public String fileExtension(String targetPath)
    {
        if (targetPath.length() > 4)
        {
            String ext = Character.toString(targetPath.charAt(targetPath.length()-3));
            ext += Character.toString(targetPath.charAt(targetPath.length()-2));
            ext += Character.toString(targetPath.charAt(targetPath.length()-1));
            return ext.toLowerCase();
        }
        
        return "";
    }
    
    public ArrayList<String> searchGenreList(boolean or, String query)
    {
        query = parseQuery(query);
        
        ArrayList<String> genreList = new ArrayList<>();
        genreList.add("(All Genres)");
        for (AudioFile track : audioDatabase.queryDB(or, "Genre", parseQuery(query)))
        {
            if (!genreList.contains(getMp3GenreFromID(track.getGenre())))
            {
                if (track.getGenre().equals("") && !genreList.contains("(No Genre)"))
                {
                    genreList.add("(No Genre)");
                }
                else if (!track.getGenre().equals(""))
                {
                    genreList.add(getMp3GenreFromID(track.getGenre()));
                }
            }
        }
        Collections.sort(genreList);
        return genreList;
    }
    
    public ArrayList<String> searchArtistList(boolean or, String query1, String query2)
    {
        ArrayList<String> artistList = new ArrayList<>();
        artistList.add("(All Artists)");
        query1 = parseQuery(query1);
        query2 = parseQuery(query2);
        
        ArrayList<AudioFile> resultSet = audioDatabase.queryDB(or, "Genre", parseQuery(query1), "Artist", parseQuery(query2));
        for (AudioFile track : resultSet)
        {
            if (!artistList.contains(track.getArtist()))
            {
                if (track.getArtist().equals("") && !artistList.contains("(No Artist)"))
                {
                    artistList.add("(No Artist)");
                }
                else if (!track.getArtist().equals(""))
                {
                    artistList.add(track.getArtist());
                }
            }
        }
        Collections.sort(artistList);
        return artistList;
    }
    
    public ArrayList<String> searchAlbumList(boolean or, String query1, String query2, String query3)
    {
        ArrayList<String> albumList = new ArrayList<>();
        albumList.add("(All Albums)");
        query1 = parseQuery(query1);
        query2 = parseQuery(query2);
        query3 = parseQuery(query3);
        
        ArrayList<AudioFile> resultSet = audioDatabase.queryDB(or, "Genre", parseQuery(query1), "Artist", parseQuery(query2), "Album", parseQuery(query3));
        
        for (AudioFile track : resultSet)
        {
            if (!albumList.contains(track.getAlbum()))
            {
                if (track.getAlbum().equals("") && !albumList.contains("(No Album)"))
                {
                    albumList.add("(No Album)");
                }
                else if (!track.getAlbum().equals(""))
                {
                    albumList.add(track.getAlbum());
                }
            }
        }
        Collections.sort(albumList);
        return albumList;
    }
    
    public ArrayList<AudioFile> searchTrackList(boolean or, String query1, String query2, String query3, String query4)
    {
        query1 = parseQuery(query1);
        query2 = parseQuery(query2);
        query3 = parseQuery(query3);
        query4 = parseQuery(query4);
        
        ArrayList<AudioFile> resultSet = audioDatabase.queryDB(or, "Genre", parseQuery(query1), "Artist", parseQuery(query2), "Album", parseQuery(query3), "Title", parseQuery(query4));

        return resultSet;
    }
    
    private String parseQuery(String query)
    {
        if (query == null || query.equals("(All Genres)") || query.equals("(All Artists)") || query.equals("(All Albums)") || query.equals("(All Tracks)"))
        {
            query = "|ALL|";
        }
        else if (query.equals("(No Genre)") || query.equals("(No Artist)") || query.equals("(No Album)"))
        {
            query = "";
        }
        return query;
    }
    
    public void eraseLibrary() throws IOException, SQLException
    {
        this.audioDatabase.dropTable();
        this.audioDatabase.createDB();
    }
    
    public void addFile(File target) throws IOException, InvalidParameterException, SQLException
    {
        if (target == null)
        {
            return;
        }
        
        String contentType = Files.probeContentType(target.toPath());
        if (contentType == null)
        {
            throw new InvalidParameterException("Unsupported File Type");
        }
        contentType = contentType.replace("/", "_");
        if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_mpeg.toString()))
        {
            try 
            {
                Mp3AudioFile newFile = new Mp3AudioFile(target);
                this.audioDatabase.addFile(newFile);
            }
            catch (SQLException ex)
            {
                
            }
        }
        else if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_m4a.toString()))
        {
            M4aAudioFile newFile = new M4aAudioFile(target);
            this.audioDatabase.addFile(newFile);
        }
        else if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_wav.toString()))
        {
            PCMAudioFile newFile = new PCMAudioFile(target);
            this.audioDatabase.addFile(newFile);
        }
    }
    
    public void deleteFile(AudioFile target) throws SQLException
    {
        if (target == null)
        {
            return;
        }
        audioDatabase.deleteFile(target);
    }
    
    public void updateFile(AudioFile target) throws SQLException
    {
        if (target == null)
        {
            return;
        }
        audioDatabase.updateFile(target);
    }
}
