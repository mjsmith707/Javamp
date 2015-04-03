/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * Mp3AudioFile class
 * Internal class used to represent wav files
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.Objects;

public class PCMAudioFile extends AudioFile
{
    PCMAudioFile(File fHandle)
    {
        super();
        try
        {
            String probeContentType = Files.probeContentType(fHandle.toPath());
            if (probeContentType == null)
            {
                throw new InvalidParameterException("Unsupported File Type");
            }
            if (!probeContentType.equals("audio/wav"))
            {
                throw new InvalidParameterException("Unsupported File Type");
            }
            super.setFilePath(fHandle.getAbsolutePath());
            super.setMimeType(probeContentType);
            readWAVTags(fHandle);
        }
        catch (IOException ex)
        {
            throw new InvalidParameterException("Unsupported File Type");
        }
    }
    
    PCMAudioFile()
    {
        super();
    }

    private void readWAVTags(File fHandle)
    {
        String list[] = super.getFilePath().split("\\\\");
        super.setTitle(list[list.length-1]);
    }

    @Override
    public void writeTags() 
    {
        throw new RuntimeException("Writing tags is unsupported for PCM files.");
    }

    @Override
    public void writeAlbumArt(File image) 
    {
        throw new RuntimeException("Writing album art is unsupported for PCM files.");
    }
    
    @Override
    public boolean equals(Object obj) 
    {
        if (obj instanceof PCMAudioFile)
        {
            AudioFile operand = (AudioFile) obj;
            return (super.getFilePath().equals(operand.getFilePath()) && super.getMimeType().equals(operand.getMimeType()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(super.getMimeType());
        hash = 59 * hash + Objects.hashCode(super.getFilePath());
        return hash;
    }
}
