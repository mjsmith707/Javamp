/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * GUI Controller for About Window
 * Based in large part from Sean Kirkpatrick's MyFriends examples.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import us.bogusville.DialogBoxes.DialogResult;
import us.bogusville.DialogBoxes.MessageBox;
import us.bogusville.DialogBoxes.MessageBoxButton;
import us.bogusville.DialogBoxes.MessageBoxImage;

public class AboutWindowGUIController implements Initializable
{
    private Stage aboutStage;
    private MediaPlayer player;
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        URL dreaming = this.getClass().getClassLoader().getResource("dreaming.mp3");
        Media inDigital = new Media(dreaming.toString());
        player = new MediaPlayer(inDigital);
        player.setVolume(0.2);
        player.play();
    }
    
    public void setStage(Stage stage)
    {
        aboutStage = stage;
        aboutStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
        {
            @Override
            public void handle(WindowEvent event) 
            {
                handleCloseRequest(event);
                event.consume();
            }
        });
    }
    
    private void handleCloseRequest(WindowEvent event)
    {
        if (player != null)
        {
            player.stop();
        }
        Stage stage = ((Stage)event.getSource());
        stage.close();
    }
    
    private void errorMsg(String message, String title)
    {
        DialogResult dialog = MessageBox.show(message,title,MessageBoxButton.OK,MessageBoxImage.Error);
    }
}