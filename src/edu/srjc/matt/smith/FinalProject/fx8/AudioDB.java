/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * AudioDB Class
 * Controls access to and from sqlite database.
 * Based in large part from Sean Kirkpatrick's MyFriends examples.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AudioDB
{
    private String dbPathname;
    private Connection dbConnection;
    private final String dbSchema = "CREATE  TABLE \"main\".\"AudioDB\" (\"filePath\" VARCHAR(512) PRIMARY KEY  NOT NULL  UNIQUE , \"mimeType\" VARCHAR(30) NOT NULL , \"Artist\" VARCHAR(30), \"Album\" VARCHAR(30), \"Title\" VARCHAR(30), \"Genre\" VARCHAR(30), \"TrackNumber\" VARCHAR(30), \"Year\" VARCHAR(30), \"DiscNumber\" VARCHAR(30), \"Length\" VARCHAR(30))";
    
    AudioDB(File fHandle) throws IOException, InvalidParameterException
    {
        this.dbPathname = "";
        this.dbConnection = null;
        if (fHandle == null)
        {
            throw new InvalidParameterException("Unknown DB Filename");
        }
        else
        {
            this.dbPathname = fHandle.getCanonicalPath();
            if (!connect())
            {
                this.dbPathname = "";
                this.dbConnection = null;
                throw new InvalidParameterException("Failed to connect to Audio DB");
            }
        }
    }
    
    private boolean connect()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            this.dbConnection = DriverManager.getConnection("jdbc:sqlite:"+dbPathname);
        }
        catch (SQLException | ClassNotFoundException e)
        {
            this.dbConnection = null;
            return false;
        }
        return true;
    }
    
    // I'm sure this is far from incomplete
    private String sanitizeString(String inString)
    {
        if (inString == null)
        {
            return "";
        }
        inString = inString.replaceAll("'", "''");
        inString = inString.replaceAll("(?i)DROP TABLE main.AudioDB", "");
        inString = inString.replaceAll("(?i)DROP TABLE AudioDB", "");
        return inString;
    }
    
    public void dropTable() throws SQLException
    {
        if (this.dbConnection == null && !connect())
        {
            return;
        }
        Statement sqlDB = dbConnection.createStatement();
        sqlDB.setQueryTimeout(30);
        sqlDB.executeUpdate("DROP TABLE main.AudioDB");
    }
    
    public void createDB() throws SQLException
    {
        if (this.dbConnection == null && !connect())
        {
            return;
        }
        
        Statement sqlDB;
        sqlDB = dbConnection.createStatement();
        sqlDB.setQueryTimeout(30);
        sqlDB.executeUpdate(this.dbSchema);
    }
    
    public void addFile(AudioFile newFile) throws SQLException
    {
        if (this.dbConnection == null)
        {
            connect();
        }
        
        Statement sqlDB = dbConnection.createStatement();
        sqlDB.setQueryTimeout(30);
        String sqlCmd = String.format("INSERT INTO main.AudioDB (filePath, mimeType, Artist, Album, Title, Genre, TrackNumber, Year, DiscNumber, Length) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')", 
                sanitizeString(newFile.getFilePath()), sanitizeString(newFile.getMimeType()), sanitizeString(newFile.getArtist()), sanitizeString(newFile.getAlbum()), sanitizeString(newFile.getTitle()), 
                sanitizeString(newFile.getGenre()), sanitizeString(newFile.getTrackNumber()), sanitizeString(newFile.getYear()), sanitizeString(newFile.getDiscNumber()), sanitizeString(newFile.getLength()));
        sqlDB.executeUpdate(sqlCmd);
    }
    
    public void deleteFile(AudioFile targetFile) throws SQLException
    {
        if (this.dbConnection == null)
        {
            connect();
        }

        Statement sqlDB = dbConnection.createStatement();
        sqlDB.setQueryTimeout(30);
        String sqlCmd = String.format("DELETE FROM main.AudioDB WHERE filePath='%s'",  sanitizeString(targetFile.getFilePath()));
        sqlDB.executeUpdate(sqlCmd);
    }
    
    public void updateFile(AudioFile targetFile) throws SQLException
    {
        if (this.dbConnection == null)
        {
            connect();
        }

        Statement sqlDB = dbConnection.createStatement();
        sqlDB.setQueryTimeout(30);
        String sqlCmd = String.format("UPDATE main.AudioDB SET Artist='%s', Album='%s', Title='%s', Genre='%s', TrackNumber='%s', Year='%s', DiscNumber='%s', Length='%s' WHERE filePath='%s'",  
                sanitizeString(targetFile.getArtist()), sanitizeString(targetFile.getAlbum()), sanitizeString(targetFile.getTitle()), sanitizeString(targetFile.getGenre()), 
                sanitizeString(targetFile.getTrackNumber()), sanitizeString(targetFile.getYear()), sanitizeString(targetFile.getDiscNumber()), sanitizeString(targetFile.getLength()), sanitizeString(targetFile.getFilePath()));
        sqlDB.executeUpdate(sqlCmd);
    }
    
    public ArrayList<AudioFile> queryDB(boolean or, String ... arguments)
    {
        if (arguments.length%2 != 0)
        {
            throw new InvalidParameterException("Invalid argument length for queryDB(). Must be even.");
        }
        
        ArrayList<String> columnList = new ArrayList<>();
        ArrayList<String> rowList = new ArrayList<>();
        for (int i=0; i<arguments.length; i++)
        {
            if (i%2 == 0)
            {
                columnList.add(sanitizeString(arguments[i]));
            }
            else
            {
                rowList.add(sanitizeString(arguments[i]));
            }
        }
        
        if (this.dbConnection == null)
        {
            connect();
        }

        ArrayList<AudioFile> list = new ArrayList<>();

        try
        {
            Statement sqlDB = dbConnection.createStatement();
            sqlDB.setQueryTimeout(30);
            String sqlCmd = String.format("SELECT * FROM main.AudioDB ");
            boolean whereAdded = false;
            boolean needAnd = false;
            
            for (int i=0; i<columnList.size(); i++)
            {
                if (columnList.get(i).equals("|ALL|") || rowList.get(i).equals("|ALL|"))
                {
                    continue;
                }
                
                if (needAnd)
                {
                    if (or)
                    {
                        sqlCmd += "OR ";
                    }
                    else
                    {
                        sqlCmd += "AND ";
                    }
                    needAnd = false;
                }
                
                if (!whereAdded)
                {
                    whereAdded = true;
                    sqlCmd += "WHERE ";
                }
                
                if (or)
                {
                    sqlCmd += String.format("%s LIKE '%%%s%%' ", columnList.get(i), rowList.get(i));
                }
                else
                {
                    sqlCmd += String.format("%s='%s' ", columnList.get(i), rowList.get(i));
                }

                needAnd = true;
            }
            
            ResultSet result = sqlDB.executeQuery(sqlCmd);
            
            // Strong typing makes for a lot of redundant code here.
            while (result.next())
            {
                if (result.getString("mimeType").equals("audio/mpeg"))
                {
                    Mp3AudioFile newFile = new Mp3AudioFile();
                    newFile.setFilePath(result.getString("filePath"));
                    newFile.setMimeType(result.getString("mimeType"));
                    newFile.setArtist(result.getString("Artist"));
                    newFile.setAlbum(result.getString("Album"));
                    newFile.setGenre(result.getString("Genre"));
                    newFile.setTitle(result.getString("Title"));
                    newFile.setTrackNumber(result.getString("TrackNumber"));
                    newFile.setYear(result.getString("Year"));
                    newFile.setDiscNumber(result.getString("DiscNumber"));
                    newFile.setLength(result.getString("Length"));
                    list.add(newFile);
                }
                else if (result.getString("mimeType").equals("audio/m4a"))
                {
                    M4aAudioFile newFile = new M4aAudioFile();
                    newFile.setFilePath(result.getString("filePath"));
                    newFile.setMimeType(result.getString("mimeType"));
                    newFile.setArtist(result.getString("Artist"));
                    newFile.setAlbum(result.getString("Album"));
                    newFile.setGenre(result.getString("Genre"));
                    newFile.setTitle(result.getString("Title"));
                    newFile.setTrackNumber(result.getString("TrackNumber"));
                    newFile.setYear(result.getString("Year"));
                    newFile.setDiscNumber(result.getString("DiscNumber"));
                    newFile.setLength(result.getString("Length"));
                    list.add(newFile);
                }
                else if (result.getString("mimeType").equals("audio/wav"))
                {
                    PCMAudioFile newFile = new PCMAudioFile();
                    newFile.setFilePath(result.getString("filePath"));
                    newFile.setMimeType(result.getString("mimeType"));
                    newFile.setArtist(result.getString("Artist"));
                    newFile.setAlbum(result.getString("Album"));
                    newFile.setGenre(result.getString("Genre"));
                    newFile.setTitle(result.getString("Title"));
                    newFile.setTrackNumber(result.getString("TrackNumber"));
                    newFile.setYear(result.getString("Year"));
                    newFile.setDiscNumber(result.getString("DiscNumber"));
                    newFile.setLength(result.getString("Length"));
                    list.add(newFile);
                }
            }
            
        }
        catch (SQLException ex)
        {
        }
        return list;
    }
}