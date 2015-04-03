/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * MediaPlayerMain Class
 * Spawns MediaPlayerGUI through the JavaFX Runtime
 * Thanks to Sean Silva on the mailing list for the setStage stuff.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MediaPlayerMain extends Application
{
    
    @Override
    public void start(Stage stage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MediaPlayerGUI.fxml"));
        Parent root = (Parent)loader.load();
        MediaPlayerGUIController main = (MediaPlayerGUIController)loader.getController();
        main.setStage(stage);
        Scene scene = new Scene(root);
        root.setStyle("-fx-background-color: #383f43");
        stage.setTitle("Javamp");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
    
}
