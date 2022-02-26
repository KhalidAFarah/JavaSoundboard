package com.sample.soundboardpack;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

public class Fileplayer {
    private String filename;
    private String filepath;
    private String command;
    private Media media;
    private MediaPlayer mediaplayer;
    private String keybind;
    private int keylocation;
    private Process previous;

    public Fileplayer(String filename, String filepath, String keybind, int keylocation) throws URISyntaxException {
        this.filename = filename;
        this.keybind = keybind;
        this.keylocation = keylocation;

        this.filepath = filepath;
        this.command = "powershell.exe -noexit -ExecutionPolicy Bypass -file player.ps1 \"" + filepath+"\"";


        File f = new File(filepath);
        this.media = new Media(f.toURI().toString());
    }

    public void play() throws IOException {
        if(previous != null) {//removing previous powershell
            previous.destroy();
            previous = null;
            mediaplayer.stop();
            mediaplayer = null;

        }else {

            //playing through powershell
            previous = Runtime.getRuntime().exec(command);

            //playing through java
            mediaplayer = new MediaPlayer(media);
            mediaplayer.play();

        }


    }
    public void pause(){
        mediaplayer.pause();
    }

    public String getKeybind() {
        return keybind;
    }
    public void setKeybind(String keybind) {
        this.keybind = keybind;
    }

    public int getKeylocation() {
        return keylocation;
    }
    public void setKeylocation(int keylocation) {
        this.keylocation = keylocation;
    }

    public void check_pressed(String keybind, int keylocation) throws IOException, InterruptedException {

        if(this.keybind != null || keybind != null) {
            if (this.keybind.toLowerCase().equals(keybind.toLowerCase())) {

                play();
            }
        }
    }

    public String getFilename() {
        return filename;
    }
}
