package com.erimali.cntrygame;

import javafx.scene.media.AudioClip;

import java.net.URL;

public class GameAudio {
    private static final String DEF_AUDIO_LOC = "audio/";

    public static void playShortSound(String name) {
        try {
            URL url = GameAudio.class.getResource(DEF_AUDIO_LOC + name);
            if (url != null) {
                AudioClip ac = new AudioClip(url.toExternalForm());
                ac.setVolume(GOptions.getVolume());
                ac.play();
            } else{
                ErrorLog.logError("ERROR: NO SUCH SOUND FILE FOUND");
            }
        } catch (Exception e) {
            ErrorLog.logError(e);
        }
    }

}
