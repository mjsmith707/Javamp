/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * MediaPlayerWrapper
 * Wrapper Class for JavaFX's MediaPlayer
 * Provides functionality to create a new MediaPlayer object with included
 * JavaFX GUI components all backed by proper event handlers.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.io.File;
import java.net.MalformedURLException;
import java.security.InvalidParameterException;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

// http://docs.oracle.com/javafx/2/media/playercontrol.htm
// Large amounts of code from here. Some of it copy&pasted with minimal
// modification. Can't believe MediaPlayer would require so much support
// structure just to function reasonably.

public class MediaPlayerWrapper
{
    private MediaPlayer player;
    private Duration duration;
    private Slider timeSlider;
    private Slider volumeSlider;
    private Label playTime;
    private Label status;
    private Button mute;
    private double volume;
    private boolean muted;
    private AudioFile currentTrack;
    
    MediaPlayerWrapper(AudioFile audioFile)
    {
        if (audioFile == null)
        {
            throw new InvalidParameterException("Failed to open audio file.");
        }
        this.currentTrack = audioFile;
        File fHandle = new File(audioFile.getFilePath());
        Media mediaFile;
        try
        {
            mediaFile = new Media(fHandle.toURI().toURL().toString());
        }
        catch (MalformedURLException ex)
        {
            throw new InvalidParameterException("Failed to open audio file.");
        }
        
        this.player = new MediaPlayer(mediaFile);
        this.player.currentTimeProperty().addListener(new InvalidationListener() 
        {
            @Override
            public void invalidated(Observable ov) 
            {
                updateValues();
            }
        });
 
        this.player.setOnReady(new Runnable() 
        {
            @Override
            public void run() 
            {
                duration = player.getMedia().getDuration();
                updateValues();
                player.play();
            }
        });
        
        this.timeSlider = new Slider();
        this.timeSlider.valueProperty().addListener(new InvalidationListener() 
        {
            @Override
            public void invalidated(Observable ov) 
            {
                if (timeSlider.isValueChanging()) 
                {
                  seek();
                }
            }
        });
        
        this.volumeSlider = new Slider();
        this.volumeSlider.valueProperty().addListener(new InvalidationListener() 
        {
            @Override
            public void invalidated(Observable ov) 
            {
               if (volumeSlider.isValueChanging()) 
               {
                   volume();
                   muteCheck();
               }
            }
        });
        
        this.playTime = new Label();
        this.status = new Label();
        this.mute = new Button();
        this.volume = 0.5;
        this.muted = false;
        this.mute.setOnAction(new EventHandler<ActionEvent>() 
        {
            @Override public void handle(ActionEvent event) 
            {
                if (muted)
                {
                    setVolumeLevel(volume);
                    muted = false;
                }
                else
                {
                    volume = player.getVolume();
                    setVolumeLevel(0.0);
                    muted = true;
                }
            }     
        }); 
    }
    
    private void seek()
    {
        this.duration = this.player.getMedia().getDuration();
        this.player.seek(this.duration.multiply(this.timeSlider.getValue() / 100.0));
    }
    
    private void volume()
    {
        this.player.setVolume(this.volumeSlider.getValue() / 100.0);
    }
    
    private void muteCheck()
    {
        if (this.player.getVolume() < 0.001)
        {
            muted = true;
            volume = 0.0;
        }
        else
        {
            muted = false;
        }
    }
    
    // The next 2 functions are copied more or less verbatem from the above tutorial.
    // In order to allow parts of the GUI to update independently from the main thread.
    protected void updateValues() 
    {
        if (player != null && playTime != null && timeSlider != null && volumeSlider != null && status != null && duration != null) 
        {
            if (isHalted() || isUnknown())
            {
                return;
            }
            Platform.runLater(new Runnable() 
            {
                @Override
                public void run() 
                {
                  Duration currentTime = player.getCurrentTime();
                  playTime.setText(formatTime(currentTime, duration));
                  status.setText(player.getStatus().toString());
                  timeSlider.setDisable(duration.isUnknown());
                  if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) 
                  {
                      timeSlider.setValue(currentTime.divide(duration).toMillis()* 100.0);
                  }
                  if (!volumeSlider.isValueChanging()) 
                  {
                      volumeSlider.setValue((int)Math.round(player.getVolume()* 100));
                  }
                }
            });
        }
    }
    
    private String formatTime(Duration elapsed, Duration duration) 
    {
         int intElapsed = (int)Math.floor(elapsed.toSeconds());
         int elapsedHours = intElapsed / (60 * 60);
         if (elapsedHours > 0) 
         {
             intElapsed -= elapsedHours * 60 * 60;
         }
         int elapsedMinutes = intElapsed / 60;
         int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

         if (duration.greaterThan(Duration.ZERO)) 
         {
            int intDuration = (int)Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) 
            {
               intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
            if (durationHours > 0) 
            {
               return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds, durationHours, durationMinutes, durationSeconds);
            } 
            else 
            {
                return String.format("%02d:%02d/%02d:%02d",elapsedMinutes, elapsedSeconds,durationMinutes, durationSeconds);
            }
          } 
         else 
         {
             if (elapsedHours > 0) 
             {
                 return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
             } 
             else 
             {
             return String.format("%02d:%02d",elapsedMinutes, elapsedSeconds);
             }
         }
     }
    
    public Slider getTimeSlider()
    {
        return this.timeSlider;
    }
    
    public Slider getVolumeSlider()
    {
        return this.volumeSlider;
    }
    
    public double getVolumeLevel()
    {
        return this.player.getVolume();
    }
    
    public void setVolumeLevel(Double volume)
    {
        this.player.setVolume(volume);
    }
    
    public Label getPlayTime()
    {
        return this.playTime;
    }
    
    public Label getStatus()
    {
        return this.status;
    }
    
    public Button getMuteButton()
    {
        return this.mute;
    }
    
    public AudioFile getCurrentTrack()
    {
        return this.currentTrack;
    }
    
    public void play()
    {
        if (this.isUnknown() || this.isHalted() || this.isPlaying())
        {
            return;
        }
        this.player.play();
    }
    
    public void pause()
    {
        if (this.isUnknown() || this.isHalted() || this.isPaused())
        {
            return;
        }
        this.player.pause();
    }
    
    public void stop()
    {
        if (this.isUnknown() || this.isHalted()|| this.isStopped())
        {
            return;
        }
        this.player.stop();
    }
    
    public boolean isPlaying()
    {
        return this.player.getStatus()==MediaPlayer.Status.PLAYING;
    }
    
    public boolean isPaused()
    {
        return this.player.getStatus()==MediaPlayer.Status.PAUSED;
    }
    
    public boolean isStopped()
    {
        return this.player.getStatus()==MediaPlayer.Status.STOPPED;
    }
    
    public boolean isReady()
    {
        return this.player.getStatus()==MediaPlayer.Status.READY;
    }
    
    public boolean isUnknown()
    {
        return this.player.getStatus()==MediaPlayer.Status.UNKNOWN;
    }
    
    public boolean isHalted()
    {
        return this.player.getStatus()==MediaPlayer.Status.HALTED;
    }
    
    public void setOnPlayBackEndedHandler(Runnable handler)
    {
        this.player.setOnEndOfMedia(handler);
    }
    
    public void setOnReadyHandler(Runnable handler)
    {
        this.player.setOnReady(handler);
    }
    
    public void burnWithFire()
    {
        this.player.dispose();
    }
}
