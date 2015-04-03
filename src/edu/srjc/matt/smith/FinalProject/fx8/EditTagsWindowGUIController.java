/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * GUI Controller for Tag Editor Window
 * Based in large part from Sean Kirkpatrick's MyFriends examples.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import us.bogusville.DialogBoxes.DialogResult;
import us.bogusville.DialogBoxes.MessageBox;
import us.bogusville.DialogBoxes.MessageBoxButton;
import us.bogusville.DialogBoxes.MessageBoxImage;

public class EditTagsWindowGUIController implements Initializable
{
    private Stage editStage;
    private MediaLibrary mediaLibrary;
    private AudioFile targetTrack;
    
    @FXML private TextField pathTextField;
    @FXML private TextField yearTextField;
    @FXML private TextField trackTextField;
    @FXML private TextField discTextField;
    @FXML private TextField titleTextField;
    @FXML private TextField artistTextField;
    @FXML private TextField albumTextField;
    @FXML private TextField genreTextField;
    
    @FXML private ImageView albumArtImageView;
    
    private File imageFile;
    private boolean updateArtwork;
            
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        imageFile = null;
        updateArtwork = false;
    }
    
    public void setStage(Stage stage)
    {
        editStage = stage;
        editStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
        {
            @Override
            public void handle(WindowEvent event) 
            {
                handleCloseRequest(event);
                event.consume();
            }
        });
    }
    
    public void setMediaLibrary(MediaLibrary mediaLibrary)
    {
        this.mediaLibrary = mediaLibrary;
    }
    
    public void setTargetTrack(AudioFile track)
    {
        if (track == null)
        {
            return;
        }
        this.targetTrack = track;
        populateTextFields();
        loadAlbumArt();
    }
    
    private void populateTextFields()
    {
        pathTextField.setText(this.targetTrack.getFilePath());
        yearTextField.setText(this.targetTrack.getYear());
        trackTextField.setText(this.targetTrack.getTrackNumber());
        discTextField.setText(this.targetTrack.getDiscNumber());
        titleTextField.setText(this.targetTrack.getTitle());
        artistTextField.setText(this.targetTrack.getArtist());
        albumTextField.setText(this.targetTrack.getAlbum());
        genreTextField.setText(this.targetTrack.getGenre());
    }
    
    private void loadAlbumArt()
    {
        BufferedImage artwork = null;
        try
        {
            artwork = this.targetTrack.getArtwork();
        }
        catch (Exception e)
        {
        }
        
        if (artwork == null)
        {
            albumArtImageView.setImage(null);
        }
        else
        {
            albumArtImageView.setImage(SwingFXUtils.toFXImage(artwork, null));
        }
    }
    
    private void handleCloseRequest(WindowEvent event)
    {
        Stage stage = ((Stage)event.getSource());
        stage.close();
    }
    
    @FXML private void handleBtnSave()
    {
        if (Dialog("Are you sure you want to write these changes to the file?", "Confirmation"))
        {
            try
            {
                this.targetTrack.setYear(yearTextField.getText());
                this.targetTrack.setTrackNumber(trackTextField.getText());
                this.targetTrack.setDiscNumber(discTextField.getText());
                this.targetTrack.setTitle(titleTextField.getText());
                this.targetTrack.setArtist(artistTextField.getText());
                this.targetTrack.setAlbum(albumTextField.getText());
                this.targetTrack.setGenre(genreTextField.getText());
                this.targetTrack.writeTags();
                
                if (updateArtwork)
                {
                    this.targetTrack.writeAlbumArt(imageFile);
                }
                
                mediaLibrary.updateFile(targetTrack);
            }
            catch (Exception e)
            {
                errorMsg("Failed to write tags: " + e.getMessage(), "Error writing tags");
                return;
            }
            successMsg("File successfully updated.", "Success");
            editStage.close();
        }
    }
    
    @FXML private void handleBtnCancel()
    {
        editStage.close();
    }
    
    @FXML private void handleBtnEditImage()
    {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        imageFile = fc.showOpenDialog(null);
        if (imageFile == null)
        {
            return;
        }
        BufferedImage newArt = null;
        try
        {
            newArt = ImageIO.read(imageFile);
            if (newArt == null)
            {
                errorMsg("Failed to open image", "Error");
                updateArtwork = false;
            }
            else
            {
                albumArtImageView.setImage(SwingFXUtils.toFXImage(newArt, null));
                updateArtwork = true;
            }
        }
        catch (IOException e)
        {
            errorMsg("Failed to open image", "Error");
        }
    }
    
    private boolean Dialog(String message, String title)
    {
        DialogResult dialog = MessageBox.show(message,title,MessageBoxButton.YesNo, MessageBoxImage.Question);
        return (dialog == DialogResult.Yes);
    }
    
    private void errorMsg(String message, String title)
    {
        DialogResult dialog = MessageBox.show(message,title,MessageBoxButton.OK,MessageBoxImage.Error);
    }
    
    private void successMsg(String message, String title)
    {
        DialogResult dialog = MessageBox.show(message,title,MessageBoxButton.OK,MessageBoxImage.Information);
    }
}