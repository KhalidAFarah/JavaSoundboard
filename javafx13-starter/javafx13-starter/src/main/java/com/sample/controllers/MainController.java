package com.sample.controllers;

import com.sample.soundboardpack.Fileplayer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.util.converter.DoubleStringConverter;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.sound.sampled.*;
import javax.sound.sampled.Control;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.JOptionPane.*;
public class MainController implements Initializable{

    @FXML
    private ComboBox micselector;

    @FXML
    private ChoiceBox micselector2;

    @FXML
    private Slider inputvolumebar;

    private Mixer mixer;

    private Thread stopper;

    private ArrayList<Fileplayer> fileplayers;

    @FXML
    private AnchorPane soundboardpane;

    @FXML
    private AnchorPane miccontroller;

    @FXML
    private ScrollPane soundboardpane_parent;

    @FXML
    private ScrollPane miccontroller_parent;



    private void fillmic(){
        for(int i = 0; i < AudioSystem.getMixerInfo().length; i++) {

            micselector.getItems().add(AudioSystem.getMixerInfo()[i]);
            System.out.println(AudioSystem.getMixerInfo()[i]);
        }
        System.out.println("----------------------------");



        micselector.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mixer = AudioSystem.getMixer((Mixer.Info) micselector.getValue());

                //stopper.start();




                System.out.println(mixer.getTargetLineInfo().length);

                Control[][] controls;

                for (int i = 0; i < mixer.getTargetLineInfo().length; i++) {
                    Line line;
                    try {
                        line = AudioSystem.getLine(mixer.getTargetLineInfo()[i]);
                        line.open();
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                        line = null;
                    }
                    controls = new Control[line.getControls().length][0];
                    System.out.println("controls: " + line.getControls().length);
                    int counter = 0;
                    String utskrift = "";
                    for (int j = 0; j < line.getControls().length; j++) {




                        if(line.getControls()[j] instanceof CompoundControl){
                            controls[j] = ((CompoundControl) line.getControls()[j]).getMemberControls();
                            utskrift += j + " : " + line.getControls()[j].toString() + "\n";

                        }


                    }
                    int val = Integer.parseInt(showInputDialog(utskrift));

                    for(Control c : controls[val]){
                        if(c instanceof FloatControl){

                            Slider s = new Slider();
                            Label txt = new Label(c.toString());

                            s.setMin(((FloatControl) c).getMinimum());
                            s.setMax(((FloatControl) c).getMaximum());
                            s.setValue(((FloatControl) c).getValue());
                            s.setStyle("-fx-color: #33b2d6;");

                            s.setLayoutY(40 + 100*counter);
                            txt.setLayoutY(10 + 100*counter);
                            s.setLayoutX(75);
                            txt.setLayoutX(10);

                            txt.setTextFill(Paint.valueOf("white"));


                            miccontroller.getChildren().add(txt);
                            miccontroller.getChildren().add(s);

                            counter++;





                        }else if(c instanceof BooleanControl){

                        }


                    }
                }
            }

        });
    }

    public void initializesoundboard(){
        fileplayers = new ArrayList<>();

    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        miccontroller.setMinWidth(miccontroller_parent.getWidth());
        miccontroller.setMinHeight(miccontroller_parent.getHeight());

        soundboardpane.setMinWidth(miccontroller_parent.getWidth());
        soundboardpane.setMinHeight(miccontroller_parent.getHeight());


        fillmic();
        initializesoundboard();
        JNativeHook_KeyPressed();



    }
    public void JNativeHook_KeyPressed(){
        //remove logging from JNativeHook
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }

        NativeKeyListener k = new NativeKeyListener() {
            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

            }

            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                if(fileplayers != null){
                    for(int i = 0; i < fileplayers.size(); i++){
                        try {
                            fileplayers.get(i).check_pressed(NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode()), nativeKeyEvent.getKeyLocation());
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                            System.out.println("---------------------");
                            System.err.println("unable to play audio file!");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

            }
        };
        GlobalScreen.addNativeKeyListener(k);


    }

    //@FXML
    public void Keypressed(KeyEvent keyEvent) {

        System.out.println(keyEvent.getCode());
        if(fileplayers != null){
            for(int i = 0; i < fileplayers.size(); i++){
                try {
                    fileplayers.get(i).check_pressed(keyEvent.getCode().toString(), 0); //set 0 to avoid error
                } catch (IOException e) {
                    System.err.println("unable to play audio file!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void fillsoundboard(){
        soundboardpane.getChildren().clear();// remove all then re add them

        for(int i = 0; i < fileplayers.size(); i++){
            Label lblname = new Label(fileplayers.get(i).getFilename().replace(".mp3", ""));
            lblname.setLayoutX(10);
            lblname.setLayoutY(10 + 90*i);
            lblname.setStyle("-fx-text-fill: white;");



            TextField txtkey = new TextField();
            txtkey.setPromptText(fileplayers.get(i).getKeybind());
            txtkey.setLayoutX(10);
            txtkey.setLayoutY(40 + 90*i);

            Button btnupdate = new Button("Update keybind");
            btnupdate.setLayoutX(201);
            btnupdate.setLayoutY(40 + 90*i);

            btnupdate.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    fileplayers.get((int) ((btnupdate.getLayoutY()-40)/90)).setKeybind(txtkey.getText());
                    //fileplayers.get((int) ((btnupdate.getLayoutY()-40)/90)).setKeybind(null);
                }
            });

            Button btnremove = new Button("Remove");
            btnremove.setLayoutX(330);
            btnremove.setLayoutY(40 + 90*i);


            btnremove.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    fileplayers.remove((int) ((btnremove.getLayoutY()-40)/90));
                    fillsoundboard();
                }
            });

            soundboardpane.getChildren().addAll(lblname, txtkey, btnupdate, btnremove);
        }
    }

    public void addclip(ActionEvent event) throws URISyntaxException {
        FileChooser filechooser  = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("mp3 files", "*.mp3"));
        File choosenfile = filechooser.showOpenDialog(null);
        if(choosenfile != null){
            //adding new clip on the scrollpane

            String num = ""+(9 - fileplayers.size());
            fileplayers.add(new Fileplayer(choosenfile.getName(), choosenfile.getAbsolutePath(), num, 4)); //default 4 as numpad

            fillsoundboard();


        }
    }
}
