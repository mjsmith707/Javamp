/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * GUI Controller for Library Scanner
 * Based in part from Sean Kirkpatrick's MyFriends examples.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import us.bogusville.DialogBoxes.DialogResult;
import us.bogusville.DialogBoxes.MessageBox;
import us.bogusville.DialogBoxes.MessageBoxButton;
import us.bogusville.DialogBoxes.MessageBoxImage;

public class LibraryScanGUIController implements Initializable
{
    private Stage scannerStage;
    @FXML private TextField selectedDirectoryPathTextField;
    @FXML private Button browseButton;
    @FXML private RadioButton recursiveScanRadioButton;
    @FXML private TextField currentProgressTextField;
    @FXML private Button scanNowButton;
    @FXML private ProgressBar scanProgressBar;
    @FXML private TextField currentScanItemTextField;
    @FXML private File fHandle;
    private boolean recursiveScan;
    private boolean toggleRecursive;
    private Task<Integer> scanTask;
    Thread scanThread;
    private int fileCount;
    private int addedCount;
    private int errorCount;
    private MediaLibrary mediaLibrary;
    
    // http://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm
    // GUI would stall out otherwise.
    // Also very big thanks for the multiple windows code. Incredibly useful.
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        recursiveScan = false;
        toggleRecursive = false;
        fHandle = null;
        mediaLibrary = null;
        
        selectedDirectoryPathTextField.setStyle("-fx-background-color: black;-fx-border-color: #6bafb9;");
        currentProgressTextField.setStyle("-fx-background-color: black;-fx-border-color: #6bafb9;");
        currentScanItemTextField.setStyle("-fx-background-color: black;-fx-border-color: #6bafb9;");
        
        newRun();
    }
    
    public void setStage(Stage stage)
    {
        scannerStage = stage;
        scannerStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
        {
            @Override
            public void handle(WindowEvent event) 
            {
                handleCloseRequest(event);
                event.consume();
            }
        });
    }
    
    private void newRun()
    {
        this.scanThread = null;
        this.scanTask = new Task<Integer>()
        {
            @Override
            protected Integer call() throws Exception
            {
                if (isCancelled())
                {
                    return addedCount;
                }
                fileCount = getFileCount(0, fHandle);
                addedCount = scanDirectory(0, fHandle);
                return errorCount;
            }
            
            private int getFileCount(int inCount, File fHandle)
            {
                int localCount = inCount;
                if (isCancelled())
                {
                    return localCount;
                }
                
                if (fHandle == null)
                {
                    return localCount;
                }
                
                if (fHandle.isDirectory())
                {
                    File[] directoryList = fHandle.listFiles();
                    for (File target : directoryList)
                    {
                        if (recursiveScan && target.isDirectory())
                        {
                            localCount = getFileCount(localCount, target);
                            updateTitle("0/" + localCount);
                        }
                        else if (target.isFile())
                        {
                            if (mediaLibrary.fileExtension(target.getAbsolutePath()).equals("mp3") 
                                    || mediaLibrary.fileExtension(target.getAbsolutePath()).equals("m4a")
                                    || mediaLibrary.fileExtension(target.getAbsolutePath()).equals("wav"))
                            {
                                localCount++;
                                updateTitle("0/" + localCount);
                            }
                        }
                    }
                }
                return localCount;
            }
            
            private int scanDirectory(int inCount, File fHandle)
            {
                int currentCount = inCount;
                if (isCancelled())
                {
                    return currentCount;
                }
                
                if ((fHandle == null) || (mediaLibrary == null))
                {
                    return currentCount;
                }
                
                if (fHandle.isDirectory())
                {
                    File[] directoryList = fHandle.listFiles();
                    for (File target : directoryList)
                    {
                        if (recursiveScan && target.isDirectory())
                        {
                            currentCount = scanDirectory(currentCount, target);
                        }
                        else if (target.isFile())
                        {
                            if (mediaLibrary.fileExtension(target.getAbsolutePath()).equals("mp3") 
                                    || mediaLibrary.fileExtension(target.getAbsolutePath()).equals("m4a")
                                    || mediaLibrary.fileExtension(target.getAbsolutePath()).equals("wav"))
                            {
                                try
                                {
                                    updateProgress(currentCount, fileCount);
                                    updateMessage(target.getAbsolutePath());
                                    updateTitle(currentCount + "/" + fileCount);
                                    mediaLibrary.addFile(target);
                                    currentCount++;
                                }
                                catch (IOException | InvalidParameterException | SQLException ex)
                                {
                                    errorCount++;
                                }
                            }
                        }
                    }
                }
                return currentCount;
            }
        };
        EventHandler<WorkerStateEvent> onSucceededEventHandler = new EventHandler<WorkerStateEvent>()
        {
            @Override
            public void handle(WorkerStateEvent t)
            {
                handleTaskFinished();
            }
        };
        
        EventHandler<WorkerStateEvent> onCanceledEventHandler = new EventHandler<WorkerStateEvent>()
        {
            @Override
            public void handle(WorkerStateEvent t)
            {
                handleTaskFinished();
            }
        };
        
        EventHandler<WorkerStateEvent> onFailedEventHandler = new EventHandler<WorkerStateEvent>()
        {
            @Override
            public void handle(WorkerStateEvent t)
            {
                handleTaskFailed();
            }
        };
        this.scanTask.setOnSucceeded(onSucceededEventHandler);
        this.scanTask.setOnCancelled(onCanceledEventHandler);
        this.scanTask.setOnFailed(onFailedEventHandler);
    }
    
    private void handleCloseRequest(WindowEvent event)
    {
        if (scanTask.isRunning() && Dialog("Are you sure you want to cancel scanning?", "Confirmation"))
        {
            try
            {
                scanTask.cancel();
                
                // Not ideal but this loop shouldnt run too long...
                // Real solution is Thread wait/notify
                while (scanTask.isRunning())
                {
                }
            }
            catch (Exception e)
            {
                System.err.println("DEBUG: scanTask.cancel() " + e.getMessage());
            }
        }
        else
        {
            Stage stage = ((Stage)event.getSource());
            stage.close();
        }
    }
    
    public void setMediaLibrary(MediaLibrary mediaLibrary)
    {
        this.mediaLibrary = mediaLibrary;
    }
    
    @FXML private void handleBrowseButtonAction(ActionEvent e)
    {
        if (scanTask.isRunning())
        {
            return;
        }
        DirectoryChooser dc = new DirectoryChooser();
        fHandle = dc.showDialog(null);
        if (fHandle != null)
        {
            selectedDirectoryPathTextField.setText(fHandle.getAbsolutePath());
        }
    }
    
    @FXML private void toggleRecursiveScan(ActionEvent e)
    {
        if (scanTask.isRunning())
        {
            return;
        }
        toggleRecursive = !toggleRecursive;
    }

    @FXML private void handleScanNowButtonAction(ActionEvent e)
    {
        if (fHandle == null)
        {
            return;
        }
        if (scanTask.isRunning())
        {
            return;
        }
        
        recursiveScan = toggleRecursive;
        scanNowButton.setText("Scanning...");
        scanThread = new Thread(scanTask);
        bindProgress(scanTask);
        scanThread.start();
    }
    
    private void handleTaskFinished()
    {
        if (scanTask.isRunning())
        {
            return;
        }
        boolean leave = true;
        
        scanNowButton.setText("Scan Finished.");
        if ((errorCount > 0) && (errorCount == fileCount))
        {
            errorMsg("Failed to add any files!", "Error.");
        }
        else if (errorCount > 0)
        {
            errorMsg("Failed to add " + errorCount + " out of " + fileCount + " files.", "Error.");
        }
        else if (addedCount != 0)
        {
            successMsg("All files added successfully.", "Success");
        }
        else if (fileCount == 0)
        {
            errorMsg("No files found in directory.", "Error");
            leave = false;
            newRun();
        }
        
        if (leave)
        {
            scannerStage.close();
        }
    }
    
    private void handleTaskFailed()
    {
        errorMsg("Scanning Failed.", "Error");
        scannerStage.close();
    }
    
    private void bindProgress(Task task)
    {
        currentScanItemTextField.textProperty().bind(task.messageProperty());
        currentScanItemTextField.setVisible(true);
        scanProgressBar.progressProperty().bind(task.progressProperty());
        scanProgressBar.setVisible(true);
        currentProgressTextField.textProperty().bind(task.titleProperty());
        currentProgressTextField.setVisible(true);
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