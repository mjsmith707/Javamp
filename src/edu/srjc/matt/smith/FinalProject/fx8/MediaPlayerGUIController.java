/* Matt Smith
 * 5/27/2014
 * Final Project
 * CS 17.11 Java
 * GUI Controller for the root window
 * Handles all main functionality in the root window as well as
 * synchronization between JavaFX, MediaPlayerWrapper, Playlist and MediaLibrary.
 * Includes Sean Kirkpatrick's DialogBoxes packages.
*/

package edu.srjc.matt.smith.FinalProject.fx8;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import us.bogusville.DialogBoxes.DialogResult;
import us.bogusville.DialogBoxes.MessageBox;
import us.bogusville.DialogBoxes.MessageBoxButton;
import us.bogusville.DialogBoxes.MessageBoxImage;

public class MediaPlayerGUIController implements Initializable
{
    @FXML private ListView<String> genreListView;
    private ObservableList<String> genreList;
    @FXML private ListView<String> artistListView;
    private ObservableList<String> artistList;
    @FXML private ListView<String> albumListView;
    private ObservableList<String> albumList;
    
    @FXML private TableView<AudioFile> trackListTableView;
    @FXML private TableColumn trackListArtistColumn;
    @FXML private TableColumn trackListAlbumColumn;
    @FXML private TableColumn trackListTrackColumn;
    @FXML private TableColumn trackListTitleColumn;
    @FXML private TableColumn trackListGenreColumn;
    @FXML private TableColumn trackListYearColumn;
    @FXML private TableColumn trackListDiscColumn;
    @FXML private TableColumn trackListLengthColumn;
    private ObservableList<AudioFile> trackList;
    
    private Playlist playlist;
    @FXML private TableView<AudioFile> playListTableView;
    @FXML private TableColumn playListArtistColumn;
    @FXML private TableColumn playListTitleColumn;
    @FXML private TableColumn playListLengthColumn;
    
    private ArrayList<AudioFile> dragList;
    private MediaLibrary mediaLibrary;
    
    private MediaPlayerWrapper mainPlayer;
    
    @FXML private BorderPane mediaViewSliderPane;
    @FXML private BorderPane mediaViewDurationPane;
    @FXML private BorderPane mediaViewVolumePane;
    @FXML private BorderPane mediaViewStatusPane;
    @FXML private BorderPane mediaViewMuteBtnPane;
    @FXML private ToggleButton btnShuffle;
    @FXML private ToggleButton btnRepeat;
    @FXML private RadioMenuItem menuShuffle;
    @FXML private RadioMenuItem menuRepeat;
    @FXML private Button btnPreviousTrack;
    @FXML private Button btnPlay;
    @FXML private Button btnPause;
    @FXML private Button btnStop;
    @FXML private Button btnNextTrack;
    
    @FXML private TextField searchBox;
    @FXML private ImageView currentTrackAlbumArtView;
    @FXML private TextField currentTrackTitleTextField;
    @FXML private TextField currentTrackArtistTextField;
    @FXML private TextField currentTrackAlbumTextField;
    
    private boolean firstRunScrollBug;
    private double volume;
    private Stage mainStage;
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try
        {
            firstRunScrollBug = true;
            mediaLibrary = new MediaLibrary("./mediaLibray.sqlite");
            playlist = new Playlist();
            dragList = new ArrayList<>();
            volume = 0.3;
            
            initializeFakeControls();
            initializeTableViews();
            
            updateGenreList(false, "(All Genres)");
            updateArtistList(false, "(All Artists)");
            updateAlbumList(false, "(All Albums)");
            updateTrackList(false, "(All Tracks)");
        }
        catch (IOException | InvalidParameterException | SQLException ex)
        {
            errorMsg(ex.getMessage(), "FATAL ERROR!");
            System.exit(1);
        }
    }
    
    private void initializeTableViews()
    {
        // Here's your type safety warnings. Not sure how to avoid it...
            trackListArtistColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("artist"));
            trackListAlbumColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("album"));
            trackListTrackColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("trackNumber"));
            trackListTitleColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("title"));
            trackListLengthColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("length"));
            trackListGenreColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("genre"));
            trackListYearColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("year"));
            trackListDiscColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("discNumber"));
            
            // https://community.oracle.com/thread/2547620?tstart=0
            // The amount of code required just to get the index position of the dropped cell is unreal.
            
            final Callback<TableColumn<AudioFile, String>, TableCell<AudioFile, String>> playlistCellFactory = new Callback<TableColumn<AudioFile, String>, TableCell<AudioFile, String>>() 
            {
                @Override
                public TableCell<AudioFile, String> call(TableColumn<AudioFile, String> col) 
                {
                    return new TableCell<AudioFile, String>() 
                    {
                        {
                            setOnDragDetected(createPlaylistDragDetectedHandler(this, playListTableView));
                            setOnDragOver(createPlaylistDragOverHandler(this, playListTableView));
                            setOnDragDropped(createPlaylistDragDroppedHandler(this, playListTableView));
                        }
                        
                        @Override
                        public void updateItem(String item, boolean empty) 
                        {
                            super.updateItem(item, empty);
                            if (empty) 
                            {
                                setText(null);
                            } 
                            else 
                            {
                                setText(item);
                            }
                        }
                        
                        private EventHandler<MouseEvent> createPlaylistDragDetectedHandler(final TableCell<AudioFile, String> cell, final TableView<AudioFile> table) 
                        {
                            return new EventHandler<MouseEvent>() 
                            {
                                @Override
                                public void handle(MouseEvent event) 
                                {
                                    Dragboard db = table.startDragAndDrop(TransferMode.MOVE);
                                    ClipboardContent content = new ClipboardContent();
                                    content.putString(String.valueOf(cell.getIndex()));
                                    db.setContent(content);
                                    dragList.clear();
                                    try
                                    {
                                        dragList.add(playlist.getTrackAt(cell.getIndex()));
                                    }
                                    catch (RuntimeException e)
                                    {
                                        dragList.clear();
                                    }
                                }
                            };
                        }
                        
                        private EventHandler<DragEvent> createPlaylistDragOverHandler(final TableCell<AudioFile, String> cell, final TableView<AudioFile> table) 
                        {
                            return new EventHandler<DragEvent>() 
                            {
                                @Override
                                public void handle(DragEvent event) 
                                {
                                    Dragboard dragboard = event.getDragboard();
                                    if (dragboard.hasString())
                                    {
                                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                                    }
                                }
                            };
                        }
                        
                        private EventHandler<DragEvent> createPlaylistDragDroppedHandler(final TableCell<AudioFile, String> cell, final TableView<AudioFile> table) 
                        {
                            return new EventHandler<DragEvent>() 
                            {
                                @Override
                                public void handle(DragEvent event) 
                                {
                                    Dragboard dragBoard = event.getDragboard();
                                    if (dragBoard.hasString() && event.getGestureSource().equals(trackListTableView))
                                    {
                                        for (AudioFile track : dragList)
                                        {
                                            playlist.addTrackAt(cell.getIndex(), track);
                                        }
                                        playListTableView.setItems(FXCollections.observableArrayList(playlist.getPlaylist()));
                                        playListTableView.getSelectionModel().select(cell.getIndex());
                                        playListTableView.requestFocus();
                                    }
                                    else if (dragBoard.hasString() && event.getGestureSource().equals(genreListView))
                                    {
                                        String item = dragBoard.getString();
                                        for (AudioFile track : mediaLibrary.searchTrackList(false, item, "(All Artists)", "(All Albums)", "(All Tracks)"))
                                        {
                                            playlist.addTrackAt(cell.getIndex(), track);
                                        }
                                        playListTableView.setItems(FXCollections.observableArrayList(playlist.getPlaylist()));
                                        playListTableView.getSelectionModel().select(cell.getIndex());
                                        playListTableView.requestFocus();
                                    }
                                    else if (dragBoard.hasString() && event.getGestureSource().equals(artistListView))
                                    {
                                        String item = dragBoard.getString();
                                        for (AudioFile track : mediaLibrary.searchTrackList(false, genreListView.getSelectionModel().getSelectedItem(), item, "(All Albums)", "(All Tracks)"))
                                        {
                                            playlist.addTrackAt(cell.getIndex(), track);
                                        }
                                        playListTableView.setItems(FXCollections.observableArrayList(playlist.getPlaylist()));
                                        playListTableView.getSelectionModel().select(cell.getIndex());
                                        playListTableView.requestFocus();
                                    }
                                    else if (dragBoard.hasString() && event.getGestureSource().equals(albumListView))
                                    {
                                        String item = dragBoard.getString();
                                        for (AudioFile track : mediaLibrary.searchTrackList(false, genreListView.getSelectionModel().getSelectedItem(), artistListView.getSelectionModel().getSelectedItem(), item, "(All Tracks)"))
                                        {
                                            playlist.addTrackAt(cell.getIndex(), track);
                                        }
                                        playListTableView.setItems(FXCollections.observableArrayList(playlist.getPlaylist()));
                                        playListTableView.getSelectionModel().select(cell.getIndex());
                                        playListTableView.requestFocus();
                                    }
                                    else if (dragBoard.hasString() && event.getGestureSource().equals(playListTableView))
                                    {
                                        int index = Integer.parseInt(dragBoard.getString());
                                        if (playlist.getIndexPosition() == index)
                                        {
                                            playlist.setIndexPosition(cell.getIndex());
                                        }
                                        playlist.removeTrackAt(index);
                                        for (AudioFile track : dragList)
                                        {
                                            playlist.addTrackAt(cell.getIndex(), track);
                                        }
                                        playListTableView.setItems(FXCollections.observableArrayList(playlist.getPlaylist()));
                                        playListTableView.getSelectionModel().select(cell.getIndex());
                                        playListTableView.requestFocus();
                                    }
                                    event.setDropCompleted(true);
                                    event.consume();
                                }
                            };
                        }
                        
                    };
                }
            };
            
            playListArtistColumn.setCellFactory(playlistCellFactory);
            playListArtistColumn.setSortable(false);
            playListTitleColumn.setCellFactory(playlistCellFactory);
            playListTitleColumn.setSortable(false);
            playListLengthColumn.setCellFactory(playlistCellFactory);
            playListLengthColumn.setSortable(false);
            playListArtistColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("artist"));
            playListTitleColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("title"));
            playListLengthColumn.setCellValueFactory(new PropertyValueFactory<AudioFile, String>("length"));
    }
    
    private void initializeFakeControls()
    {
        Slider timeSlider = new Slider();
        Label playTime = new Label();
        playTime.setStyle("-fx-text-fill: #6BAFB9; -fx-font-size: 20");
        playTime.setText("         :");
        Label status = new Label();
        status.setStyle("-fx-text-fill: #6BAFB9; -fx-font-size: 16");
        status.setText("Stopped");
        Slider volumeSlider = new Slider();
        volumeSlider.setValue(volume);
        Button muteButton = new Button();
        muteButton.setScaleX(0.5);
        muteButton.setScaleY(0.5);
        mediaViewSliderPane.setBottom(timeSlider);
        mediaViewDurationPane.setTop(playTime);
        mediaViewVolumePane.setCenter(volumeSlider);
        mediaViewStatusPane.setCenter(status);
        mediaViewMuteBtnPane.setCenter(muteButton);
        
        // If I knew anything about CSS these would be in a stylesheet.
        btnPreviousTrack.setStyle("-fx-font-family: \"Segoe UI Symbol\";-fx-font-size: 16;-fx-text-fill: #505050;");
        btnPlay.setStyle("-fx-font-family: \"Segoe UI Symbol\";-fx-font-size: 13;-fx-text-fill: #505050;");
        btnPause.setStyle("-fx-font-family: \"Segoe UI Symbol\";-fx-font-size: 16;-fx-text-fill: #505050;");
        btnStop.setStyle("-fx-font-family: \"Segoe UI Symbol\";-fx-font-size: 13;-fx-text-fill: #505050;");
        btnNextTrack.setStyle("-fx-font-family: \"Segoe UI Symbol\";-fx-font-size: 16;-fx-text-fill: #505050;");
    }
    
    public void setStage(Stage stage)
    {
        if (stage == null)
        {
            throw new RuntimeException("NULL Stage was passed into setStage.");
        }
        this.mainStage = stage;
        setExitHandler();
    }
    
    private void setExitHandler()
    {
        this.mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
        {
            @Override
            public void handle(WindowEvent event) 
            {
                handleFileMenuExit(null);
                event.consume();
            }
        });
    }
    
    private void refreshScreen()
    {
        updateCurrentTrackDisplay();
        updatePlaylistDisplay();
        updateArtwork();
    }
    
    private void initializeMediaPlayer()
    {
        if (playlist.getIndexPosition() == -1)
        {
            return;
        }
        
        if (mainPlayer != null)
        {
            mainPlayer.stop();
            volume = mainPlayer.getVolumeLevel();
            mainPlayer.burnWithFire();
        }
        
        try
        {
            mainPlayer = new MediaPlayerWrapper(playlist.getCurrentTrack());
            mainPlayer.setVolumeLevel(volume);
            mainPlayer.setOnPlayBackEndedHandler(new Runnable()
            {  
                @Override public void run()
                {
                    playNext();
                }
            });
            
            attachMediaControls();
        }
        catch (Exception e)
        {
            if (!e.getMessage().equals("Error empty playlist."))
            {
                errorMsg(e.getMessage(), "ERROR!");
            }
        }
    }
    
    private void attachMediaControls()
    {
        Slider timeSlider = mainPlayer.getTimeSlider();
        Label playTime = mainPlayer.getPlayTime();
        playTime.setStyle("-fx-text-fill: #6BAFB9; -fx-font-size: 20");
        Label status = mainPlayer.getStatus();
        status.setStyle("-fx-text-fill: #6BAFB9; -fx-font-size: 16");
        Slider volumeSlider = mainPlayer.getVolumeSlider();
        Button muteButton = mainPlayer.getMuteButton();
        muteButton.setScaleX(0.5);
        muteButton.setScaleY(0.5);
        muteButton.setScaleY(0.5);
        mediaViewSliderPane.setBottom(timeSlider);
        mediaViewDurationPane.setTop(playTime);
        mediaViewVolumePane.setCenter(volumeSlider);
        mediaViewStatusPane.setCenter(status);
        mediaViewMuteBtnPane.setCenter(muteButton);
    }

    private void updateArtwork()
    {
        BufferedImage artwork = null;
        try
        {
            artwork = playlist.getCurrentTrack().getArtwork();
        }
        catch (Exception e)
        {
            if (!e.getMessage().equals("Error empty playlist."))
            {
                errorMsg(e.getMessage(), "ERROR!");
            }
        }
        
        if (artwork == null)
        {
            currentTrackAlbumArtView.setImage(null);
        }
        else
        {
            currentTrackAlbumArtView.setImage(SwingFXUtils.toFXImage(artwork, null));
        }
    }
    
    private void updatePlaylistDisplay()
    {
        playListTableView.setItems(FXCollections.observableArrayList(playlist.getPlaylist()));
        playListTableView.getSelectionModel().select(playlist.getIndexPosition());
        playListTableView.requestFocus();
    }
    
    private void moveTracklistToPlaylist()
    {
        if (trackList == null)
        {
            return;
        }
        ArrayList<AudioFile> newList = new ArrayList<>();
        for (AudioFile track : trackList)
        {
            newList.add(track);
        }
        playlist.clearPlaylist();
        playlist.appendTracksAt(0, newList);
    }
    
    private void updateCurrentTrackDisplay()
    {
        try
        {
            currentTrackTitleTextField.setText(playlist.getCurrentTrack().getTitle());
            currentTrackArtistTextField.setText(playlist.getCurrentTrack().getArtist());
            currentTrackAlbumTextField.setText(playlist.getCurrentTrack().getAlbum());
        }
        catch (Exception e)
        {
            if (!e.getMessage().equals("Error empty playlist."))
            {
                errorMsg(e.getMessage(), "ERROR!");
            }
        }
    }
    
    private void updateGenreList(boolean or, String query)
    {
        genreList = FXCollections.observableArrayList(mediaLibrary.searchGenreList(or, query));
        genreListView.setItems(genreList);
    }
    
    private void updateArtistList(boolean or, String query)
    {
        artistList = FXCollections.observableArrayList(mediaLibrary.searchArtistList(or, genreListView.getSelectionModel().getSelectedItem(), query));
        artistListView.setItems(artistList);
    }
    
    private void updateAlbumList(boolean or, String query)
    {
        albumList = FXCollections.observableArrayList(mediaLibrary.searchAlbumList(or, genreListView.getSelectionModel().getSelectedItem(), artistListView.getSelectionModel().getSelectedItem(), query));
        albumListView.setItems(albumList);
    }
    
    private void updateTrackList(boolean or, String query)
    {
        trackList = FXCollections.observableArrayList(mediaLibrary.searchTrackList(or, genreListView.getSelectionModel().getSelectedItem(), artistListView.getSelectionModel().getSelectedItem(), albumListView.getSelectionModel().getSelectedItem(), query));
        for (AudioFile track : trackList)
        {
            track.setGenre(mediaLibrary.getMp3GenreFromID(track.getGenre()));
        }
        trackListTableView.setItems(trackList);
    }
    
    @FXML private void handleGenreListLeftClick(MouseEvent e)
    {
        updateArtistList(false, "(All Artists)");
        updateAlbumList(false, "(All Albums)");
        updateTrackList(false, "(All Tracks)");
        
        if (e.getClickCount() == 2)
        {
            moveTracklistToPlaylist();
            initializeMediaPlayer();
            refreshScreen();
        }
    }
    
    @FXML private void handleArtistListLeftClick(MouseEvent e)
    {
        updateAlbumList(false, "(All Albums)");
        updateTrackList(false, "(All Tracks)");
        if (e.getClickCount() == 2)
        {
            moveTracklistToPlaylist();
            initializeMediaPlayer();
            refreshScreen();
        }
    }
    
    @FXML private void handleAlbumListLeftClick(MouseEvent e)
    {
        updateTrackList(false, "(All Tracks)");
        if (e.getClickCount() == 2)
        {
            moveTracklistToPlaylist();
            initializeMediaPlayer();
            refreshScreen();
        }
    }
    
    @FXML private void handleTrackListLeftClick(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            moveTracklistToPlaylist();
            playlist.setIndexPosition(trackListTableView.getSelectionModel().getSelectedIndex());
            initializeMediaPlayer();
            
            refreshScreen();
            
            if (!firstRunScrollBug)
            {
                playListTableView.scrollTo(playlist.getIndexPosition());
            }
            else
            {
                firstRunScrollBug = false;
            }
        }
    }
    
    @FXML private void handlePlayListClick(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            playlist.setIndexPosition(playListTableView.getSelectionModel().getSelectedIndex());
            initializeMediaPlayer();
            refreshScreen();
        }
    }
    
    
    @FXML private void handleBtnPlay()
    {
        if (mainPlayer == null)
        {
            initializeMediaPlayer();
            refreshScreen();
        }
        else if (mainPlayer.isPaused() || mainPlayer.isStopped())
        {
            mainPlayer.play();
            refreshScreen();
        }
    }

    @FXML private void handleBtnStop()
    {
        if (mainPlayer != null)
        {
            mainPlayer.stop();
        }
    }
    
    @FXML private void handleBtnShuffle()
    {
        this.playlist.setShuffle(btnShuffle.isSelected());
        menuShuffle.setSelected(btnShuffle.isSelected());
    }
    
    @FXML private void handleBtnRepeat()
    {
        this.playlist.setRepeat(btnRepeat.isSelected());
        menuRepeat.setSelected(btnRepeat.isSelected());
    }
    
    @FXML private void handlePlayMenuShuffle()
    {
        this.playlist.setShuffle(menuShuffle.isSelected());
        btnShuffle.setSelected(menuShuffle.isSelected());
    }
    
    @FXML private void handlePlayMenuRepeat()
    {
        this.playlist.setRepeat(menuRepeat.isSelected());
        btnRepeat.setSelected(menuRepeat.isSelected());
    }
    
    @FXML private void handleBtnPrevious()
    {
        if (mainPlayer != null)
        {
            int lastIndex = playlist.getIndexPosition();
            playlist.previousTrack();
            initializeMediaPlayer();
            
            refreshScreen();
            if ((lastIndex > playlist.getIndexPosition()+24) || (lastIndex < playlist.getIndexPosition()-24))
            {
                playListTableView.scrollTo(playlist.getIndexPosition());
            }
            // http://stackoverflow.com/questions/16919727/javafxhow-to-disable-button-for-specific-time
            // To keep MediaPlayer from destroying itself. Dirty hack last-minute workaround.
            Thread pauseButtonPrevious = new Thread() 
            {
                @Override
                public void run() 
                {
                    Platform.runLater(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            btnPreviousTrack.setDisable(true);
                        }
                    });
                    try 
                    {
                        Thread.sleep(500);
                    }
                    catch(InterruptedException ex) 
                    {
                    }
                    Platform.runLater(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            btnPreviousTrack.setDisable(false);
                        }
                    });
                }
            };
            pauseButtonPrevious.start();
        }
    }
    
    @FXML private void handleBtnNext()
    {
        if (mainPlayer != null)
        {
            int lastIndex = playlist.getIndexPosition();
            playlist.nextTrack();
            if (playlist.getIndexPosition() == -1)
            {
                mainPlayer.stop();
                return;
            }
            initializeMediaPlayer();

            refreshScreen();
            if ((lastIndex > playlist.getIndexPosition()+24) || (lastIndex < playlist.getIndexPosition()-24))
            {
                playListTableView.scrollTo(playlist.getIndexPosition());
            }
            
            // http://stackoverflow.com/questions/16919727/javafxhow-to-disable-button-for-specific-time
            // To keep MediaPlayer from destroying itself
            Thread pauseButtonNext = new Thread() 
            {
                @Override
                public void run() 
                {
                    Platform.runLater(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            btnNextTrack.setDisable(true);
                        }
                    });
                    try 
                    {
                        Thread.sleep(500);
                    }
                    catch(InterruptedException ex) 
                    {
                    }
                    Platform.runLater(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            btnNextTrack.setDisable(false);
                        }
                    });
                }
            };
            pauseButtonNext.start();
        }
    }
    
    @FXML private void handleBtnPause()
    {
        if (mainPlayer != null)
        {
            mainPlayer.pause();
        }
    }
    
    @FXML private void handleSearchAction(KeyEvent e)
    {
        String query = searchBox.getText();
        if (query.equals(""))
        {
            updateGenreList(true, "(All Genres)");
            updateArtistList(true, "(All Artists)");
            updateAlbumList(true, "(All Albums)");
            updateTrackList(true, query);
        }
        else
        {
            updateGenreList(true, query);
            updateArtistList(true, query);
            updateAlbumList(true, query);
            updateTrackList(true, query);
        }
    }
    
    private void playNext()
    {
        if (mainPlayer != null)
        {
            int lastIndex = playlist.getIndexPosition();
            playlist.nextTrack();
            if (playlist.getIndexPosition() == -1)
            {
                mainPlayer.stop();
                return;
            }
            initializeMediaPlayer();

            refreshScreen();
            if ((lastIndex > playlist.getIndexPosition()+24) || (lastIndex < playlist.getIndexPosition()-24))
            {
                playListTableView.scrollTo(playlist.getIndexPosition());
            }
        }
    }
    
    // Drag & Drop
    // http://docs.oracle.com/javafx/2/drag_drop/jfxpub-drag_drop.htm
    // http://javafxdeveloper.wordpress.com/2013/10/28/simple-drag-and-drop-tutorial/
    
    @FXML private void handleGenreListDragDetected(MouseEvent e)
    {
        if (genreListView.getSelectionModel().getSelectedItem() == null)
        {
            return;
        }
        Dragboard dragBoard = genreListView.startDragAndDrop(TransferMode.COPY);
        ClipboardContent clipboard = new ClipboardContent();
        clipboard.putString(genreListView.getSelectionModel().getSelectedItem());
        dragBoard.setContent(clipboard);
    }
    
    @FXML private void handleArtistListDragDetected(MouseEvent e)
    {
        if (artistListView.getSelectionModel().getSelectedItem() == null)
        {
            return;
        }
        Dragboard dragBoard = artistListView.startDragAndDrop(TransferMode.COPY);
        ClipboardContent clipboard = new ClipboardContent();
        clipboard.putString(artistListView.getSelectionModel().getSelectedItem());
        dragBoard.setContent(clipboard);
    }
    
    @FXML private void handleAlbumListDragDetected(MouseEvent e)
    {
        if (albumListView.getSelectionModel().getSelectedItem() == null)
        {
            return;
        }
        Dragboard dragBoard = albumListView.startDragAndDrop(TransferMode.COPY);
        ClipboardContent clipboard = new ClipboardContent();
        clipboard.putString(albumListView.getSelectionModel().getSelectedItem());
        dragBoard.setContent(clipboard);
    }
    
    @FXML private void handleTrackListDragDetected(MouseEvent e)
    {
        if (trackListTableView.getSelectionModel().getSelectedItem() == null)
        {
            return;
        }
        
        Dragboard dragBoard = trackListTableView.startDragAndDrop(TransferMode.COPY);
        ClipboardContent clipboard = new ClipboardContent();
        clipboard.putString("TRACKLIST_DRAG");
        dragBoard.setContent(clipboard);
        dragList.clear();
        dragList.add(trackListTableView.getSelectionModel().getSelectedItem());
    }
    
    @FXML private void handleTrackListDragDropped(DragEvent e)
    {
        Dragboard dragBoard = e.getDragboard();
        if (dragBoard.hasString() && e.getGestureSource().equals(trackListTableView))
        {
            e.setDropCompleted(true);
            e.consume();
        }
        e.setDropCompleted(true);
        e.consume();
    }
    
    @FXML private void handlePlayListDragDropped(DragEvent e)
    {
        Dragboard dragBoard = e.getDragboard();
        if (dragBoard.hasString() && e.getGestureSource().equals(trackListTableView))
        {
            for (AudioFile track : dragList)
            {
                playlist.addTrack(track);
            }
            updatePlaylistDisplay();
        }
        else if (dragBoard.hasString() && e.getGestureSource().equals(genreListView))
        {
            String item = dragBoard.getString();
            for (AudioFile track : mediaLibrary.searchTrackList(false, item, "(All Artists)", "(All Albums)", "(All Tracks)"))
            {
                playlist.addTrack(track);
            }
            updatePlaylistDisplay();
        }
        else if (dragBoard.hasString() && e.getGestureSource().equals(artistListView))
        {
            String item = dragBoard.getString();
            for (AudioFile track : mediaLibrary.searchTrackList(false, genreListView.getSelectionModel().getSelectedItem(), item, "(All Albums)", "(All Tracks)"))
            {
                playlist.addTrack(track);
            }
            updatePlaylistDisplay();
        }
        else if (dragBoard.hasString() && e.getGestureSource().equals(albumListView))
        {
            String item = dragBoard.getString();
            for (AudioFile track : mediaLibrary.searchTrackList(false, genreListView.getSelectionModel().getSelectedItem(), artistListView.getSelectionModel().getSelectedItem(), item, "(All Tracks)"))
            {
                playlist.addTrack(track);
            }
            updatePlaylistDisplay();
        }
        e.setDropCompleted(true);
        e.consume();
    }
    
    @FXML private void handlePlayListDragOver(DragEvent e)
    {
        if ((e.getGestureSource() == playListTableView) && (e.getDragboard().hasString()))
        {
            e.acceptTransferModes(TransferMode.MOVE);
        }
        else if ((e.getGestureSource() != playListTableView) && (e.getDragboard().hasString()))
        {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }
    
    @FXML private void handleFileMenuPlayFile(ActionEvent e)
    {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        List<File> fHandles = fc.showOpenMultipleDialog(null);
        if (fHandles == null)
        {
            return;
        }
        
        ArrayList<AudioFile> newList = new ArrayList<>();
        int errCount = 0;
        int fileCount = 0;
        for (File fHandle : fHandles)
        {
            try
            {
                String contentType = Files.probeContentType(fHandle.toPath());
                if (contentType == null)
                {
                    continue;
                }
                System.out.println("Content Type: " + contentType);
                contentType = contentType.replace("/", "_");
                if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_mpeg.toString()))
                {
                    newList.add(new Mp3AudioFile(fHandle));
                    fileCount++;
                }
                else if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_m4a.toString()))
                {
                    newList.add(new M4aAudioFile(fHandle));
                    fileCount++;
                }
                else if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_wav.toString()))
                {
                    newList.add(new PCMAudioFile(fHandle));
                    fileCount++;
                }
            }
            catch (IOException | InvalidParameterException err)
            {
                if (err.getClass().equals(InvalidParameterException.class))
                {
                    errCount++;
                }
            }
        }
        
        if (errCount > 0)
        {
            if (errCount == fileCount)
            {
                errorMsg("Failed to add any items.", "Error");
                return;
            }
            else
            {
                errorMsg("Failed to add some items", "Error");
            }
        }
        
        if (fileCount == 0)
        {
            errorMsg("Failed to add any items.", "Error");
            return;
        }
        
        playlist.clearPlaylist();
        
        for (AudioFile track : newList)
        {
            playlist.addTrack(track);
        }
        initializeMediaPlayer();

        refreshScreen();
    }
    
    @FXML private void handleHelpMenuAbout(ActionEvent e)
    {
        handleBtnPause();
        createAboutWindow();
    }
    
    @FXML private void handleFileMenuPlayDirectory(ActionEvent e)
    {
        DirectoryChooser dc = new DirectoryChooser();
        File fHandle = dc.showDialog(null);
        if (fHandle == null)
        {
            return;
        }
        if (!fHandle.isDirectory())
        {
            return;
        }

        ArrayList<AudioFile> newList = new ArrayList<>();
        int errCount = 0;
        int fileCount = 0;
        File[] directoryList = fHandle.listFiles();
        for (File target : directoryList)
        {
            if (target.isFile())
            {
                try 
                {
                    String contentType = Files.probeContentType(target.toPath());
                    if (contentType == null)
                    {
                        continue;
                    }
                    contentType = contentType.replace("/", "_");
                    if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_mpeg.toString()))
                    {
                        fileCount++;
                        newList.add(new Mp3AudioFile(target));
                    }
                    else if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_m4a.toString()))
                    {
                        fileCount++;
                        newList.add(new M4aAudioFile(target));
                    }
                    else if (contentType.equals(AudioFile.SUPPORTED_TYPES.audio_wav.toString()))
                    {
                        fileCount++;
                        newList.add(new PCMAudioFile(target));
                    }
                }
                catch (IOException | InvalidParameterException err)
                {
                    if (err.getClass().equals(InvalidParameterException.class))
                    {
                        errCount++;
                    }
                }
            }
        }
        
        if (errCount > 0)
        {
            if (errCount == fileCount)
            {
                errorMsg("Failed to add any items.", "Error");
                return;
            }
            else
            {
                errorMsg("Failed to add some items", "Error");
            }
        }

        if (fileCount == 0)
        {
            errorMsg("Failed to add any items.", "Error");
            return;
        }
        
        playlist.clearPlaylist();
        for (AudioFile track : newList)
        {
            playlist.addTrack(track);
        }
        
        initializeMediaPlayer();
        refreshScreen();
    }
    
    @FXML private void handleFileMenuAddFile(ActionEvent e)
    {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        List<File> fHandles = fc.showOpenMultipleDialog(null);
        if (fHandles == null)
        {
            return;
        }
        
        int errCount = 0;
        int addedCount = 0;
        for (File fHandle : fHandles)
        {
            try
            {
                mediaLibrary.addFile(fHandle);
                addedCount++;
            }
            catch (IOException | InvalidParameterException | SQLException err)
            {
                if (err instanceof InvalidParameterException)
                {
                    errCount++;
                }
            }
        }
        
        if (errCount > 0)
        {
            if (errCount == fHandles.size())
            {
                errorMsg("Failed to add any items.", "Error");
                return;
            }
            else
            {
                errorMsg("Failed to add some items", "Error");
            }
        }
        else if (addedCount > 1)
        {
            successMsg("Successfully added " + addedCount + " items.", null);
        }
        else
        {
            successMsg("Successfully added "+ addedCount + " item.", null);
        }
        
        updateGenreList(false, "(All Genres)");
        updateArtistList(false, "(All Artists)");
        updateAlbumList(false, "(All Albums)");
      
    }
    
    @FXML private void handleFileMenuAddDirectory(ActionEvent e)
    {
        createAddDirectoryWindow();
        updateGenreList(false, "(All Genres)");
        updateArtistList(false, "(All Artists)");
        updateAlbumList(false, "(All Albums)");
    }
    
    @FXML private void handleFileMenuExit(ActionEvent e)
    {
        System.exit(0);
    }
    
    @FXML private void handleFileMenuOpenPlaylist(ActionEvent e)
    {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        File fHandle = fc.showOpenDialog(null);
        
        if (fHandle == null)
        {
            return;
        }
        
        playlist.clearPlaylist();
        playlist.loadM3UPlaylist(fHandle);
        initializeMediaPlayer();
  
        refreshScreen();
    }
    
    @FXML private void handleFileMenuSavePlaylist(ActionEvent e)
    {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        File fHandle = fc.showSaveDialog(null);
        if (fHandle == null)
        {
            return;
        }
        String m3uPlaylist = playlist.toM3UPlaylist();
        
        try
        {
            PrintWriter ostream = new PrintWriter(fHandle);
            ostream.printf(m3uPlaylist);
            ostream.flush();
            ostream.close();
        }
        catch (FileNotFoundException ex)
        {
            errorMsg("Failed to save playlist", "Error");
        }
    }
    
    @FXML private void handleOptionsMenuDeleteLibrary(ActionEvent e)
    {
        if (!Dialog("Are you sure you want to erase the library? This will not delete any files.", "Confirmation"))
        {
            return;
        }
        try
        {
            this.mediaLibrary.eraseLibrary();
            updateGenreList(false, "(All Genres)");
            updateArtistList(false, "(All Artists)");
            updateAlbumList(false, "(All Albums)");
            updateTrackList(false, "(All Tracks)");
        }
        catch (IOException | SQLException ex)
        {
            errorMsg("Failed to erase media library. " + ex.getMessage(), "Error");
        }
    }
    
    @FXML private void handlePlaylistRightClickClear(ActionEvent e)
    {
        playlist.clearPlaylist();
        updatePlaylistDisplay();
    }
    
    @FXML private void handlePlaylistRightClickRemove(ActionEvent e)
    {
        if (playListTableView.getSelectionModel().getSelectedItem() == null)
        {
            return;
        }
        playlist.removeTrackAt(playListTableView.getSelectionModel().getSelectedIndex());
        updatePlaylistDisplay();
    }
    
    @FXML private void handleTrackListRightClickRemove(ActionEvent e)
    {
        if (trackListTableView.getSelectionModel().getSelectedItem() == null)
        {
            return;
        }
        if (Dialog("Are you sure you want to remove this from the library?", "Confirmation"))
        {
            try 
            {
                mediaLibrary.deleteFile(trackListTableView.getSelectionModel().getSelectedItem());
            } 
            catch (SQLException ex) 
            {
                errorMsg("Failed to remove file from library", "Error");
            }
            updateTrackList(false, "(All Tracks)");
        }
    }
    
    @FXML private void handleTrackListRightClickEditTags(ActionEvent e)
    {
        if (trackListTableView.getSelectionModel().getSelectedItem() == null)
        {
            return;
        }
        createEditTagsWindow(trackListTableView.getSelectionModel().getSelectedItem());
    }
    
    // Your magical code in action.
    private void createAddDirectoryWindow()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryScanGUI.fxml"));
            AnchorPane page = (AnchorPane)loader.load();
            
            Stage childForm = new Stage();
            Scene scene = new Scene(page);
            
            childForm.setScene(scene);
            
            LibraryScanGUIController controller = loader.getController();
            controller.setStage(childForm);
            controller.setMediaLibrary(mediaLibrary);
            
            childForm.initModality(Modality.WINDOW_MODAL);
            childForm.setTitle("Directory Scan");
            childForm.initOwner(mainStage);
            childForm.showAndWait();
        }
        catch (IOException ex)
        {
            errorMsg(ex.getMessage(), "Error");
        }
    }
    
    private void createAboutWindow()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AboutWindowGUI.fxml"));
            AnchorPane page = (AnchorPane)loader.load();
            
            Stage childForm = new Stage();
            Scene scene = new Scene(page);
            
            childForm.setScene(scene);
            
            AboutWindowGUIController controller = loader.getController();
            controller.setStage(childForm);
            
            childForm.initModality(Modality.WINDOW_MODAL);
            childForm.setTitle("About this program");
            childForm.initOwner(mainStage);
            childForm.showAndWait();
        }
        catch (IOException ex)
        {
            errorMsg(ex.getMessage(), "Error");
        }
    }
    
    private void createEditTagsWindow(AudioFile targetTrack)
    {
        if (targetTrack == null)
        {
            return;
        }
        
        // This is a futile attempt to get MediaPlayer to release the file handle so that
        // the audio tagger can (rather stupidly) rename the file, write changes, then name it back.
        // It now works thanks to burnWithFire();
        boolean stopPlayer = false;
        if (mainPlayer != null)
        {
            if (mainPlayer.getCurrentTrack().equals(targetTrack))
            {
                stopPlayer = true;
                mainPlayer.stop();
                mainPlayer.burnWithFire();
                mainPlayer = null;
            }
        }
        
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditTagsWindowGUI.fxml"));
            AnchorPane page = (AnchorPane)loader.load();
            
            Stage childForm = new Stage();
            Scene scene = new Scene(page);
            
            childForm.setScene(scene);
            
            EditTagsWindowGUIController controller = loader.getController();
            controller.setStage(childForm);
            controller.setMediaLibrary(mediaLibrary);
            controller.setTargetTrack(targetTrack);
            
            childForm.initModality(Modality.WINDOW_MODAL);
            childForm.setTitle("File Info");
            childForm.initOwner(mainStage);
            childForm.showAndWait();
        }
        catch (IOException ex)
        {
            errorMsg(ex.getMessage(), "Error");
        }
        
        updateGenreList(false, "(All Genres)");
        updateArtistList(false, "(All Artists)");
        updateAlbumList(false, "(All Albums)");
        updateTrackList(false, "(All Tracks)");
        
        if (stopPlayer)
        {
            updatePlaylistDisplay();
            handleBtnPlay();
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