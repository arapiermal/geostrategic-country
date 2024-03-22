package com.erimali.cntrygame;
import javafx.scene.media.AudioClip;

import java.net.URISyntaxException;

public class GameAudio {
    private static final String DEFAUDIOLOC = "audio/";

    public static void playShortSound(String name) {
        try {
            AudioClip ac = new AudioClip(GameAudio.class.getResource(DEFAUDIOLOC + name).toExternalForm());
            ac.setVolume(GOptions.getVolume());
            ac.play();
        }
        catch(Exception e){

        }
    }

}
