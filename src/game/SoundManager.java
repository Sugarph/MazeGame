package game;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private final Map<String, Clip> soundClips = new HashMap<>();

    public SoundManager() {
        loadSound("lightSwitch", "/sfx/light-switch.wav", 0.9f);
        loadSound("walking", "/sfx/walking.wav", 0.75f);
        loadSound("heartbeat", "/sfx/heart-beat.wav", 0.75f);
        loadSound("fast heartbeat", "/sfx/fast-heartbeat.wav", 0.85f);
        loadSound("shadow", "/sfx/jump-scare.wav", 0.80f);
    }

    private void loadSound(String name, String filePath, float initialVolume) {
        try {
            InputStream audioSrc = getClass().getResourceAsStream(filePath);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(audioSrc));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            soundClips.put(name, clip);
            setVolume(name, initialVolume);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }


    public void playSound(String name, boolean loop, boolean resetIfPlaying) {
        Clip clip = soundClips.get(name);
        if (clip != null) {
            if (resetIfPlaying) {
                clip.setFramePosition(0);
                clip.stop();
            }

            if (!clip.isRunning()) {
                clip.start();
                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
            }
        }
    }

    public void stopSound(String name) {
        Clip clip = soundClips.get(name);
        if (clip != null) {
            clip.stop();
        }
    }

    public void setVolume(String name, float volume) {
        Clip clip = soundClips.get(name);
        if (clip != null) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float newVolume = Math.max(min, Math.min(max, min + (max - min) * volume));
            volumeControl.setValue(newVolume);
        }
    }
}
