package com.staticgame.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    // Array to hold music for each world
    private Music[] worldMusic = new Music[3];
    private Music glitchBg;
    private Sound shiftSfx, interactSfx;
    private float vol = 0.6f;
    private int currentWorldIndex = -1;

    public AudioManager() { load(); }

    private void load() {
        try {
            // Loading your specific files for each world
            // Note: In LibGDX, files should be in the 'assets' folder
            worldMusic[0] = Gdx.audio.newMusic(Gdx.files.internal("audio/world 1.mp3"));
            worldMusic[1] = Gdx.audio.newMusic(Gdx.files.internal("audio/world 2.mp3"));
            worldMusic[2] = Gdx.audio.newMusic(Gdx.files.internal("audio/world 3.mp3"));

            for (Music m : worldMusic) {
                if (m != null) {
                    m.setLooping(true);
                    m.setVolume(vol * 0.5f);
                }
            }

            if (Gdx.files.internal("audio/ambient_glitch.ogg").exists()) {
                glitchBg = Gdx.audio.newMusic(Gdx.files.internal("audio/ambient_glitch.ogg"));
                glitchBg.setLooping(true);
                glitchBg.setVolume(vol * 0.5f);
            }

            // SFX Loading
            shiftSfx = Gdx.audio.newSound(Gdx.files.internal("audio/shift.ogg"));
            interactSfx = Gdx.audio.newSound(Gdx.files.internal("audio/interact.ogg"));

        } catch (Exception e) {
            System.out.println("[Audio] File missing - ensure mp3s are in assets/audio/");
        }
    }

    /** Switches music based on the world index and glitch state */
    public void playWorldMusic(int worldIdx, boolean isGlitched) {
        // Stop previous music if switching worlds
        if (currentWorldIndex != -1 && currentWorldIndex != worldIdx) {
            stop(worldMusic[currentWorldIndex]);
        }

        currentWorldIndex = worldIdx;

        if (isGlitched) {
            stop(worldMusic[worldIdx]);
            play(glitchBg);
        } else {
            stop(glitchBg);
            play(worldMusic[worldIdx]);
        }
    }

    public void playShiftSound()    { if (shiftSfx != null) shiftSfx.play(vol); }
    public void playInteractSound() { if (interactSfx != null) interactSfx.play(vol * 0.7f); }

    public void stopAll() {
        stop(glitchBg);
        for (Music m : worldMusic) stop(m);
    }

    private void play(Music m) { if (m != null && !m.isPlaying()) m.play(); }
    private void stop(Music m) { if (m != null && m.isPlaying()) m.stop(); }

    public void setMasterVolume(float v) {
        vol = v;
        if (glitchBg != null) glitchBg.setVolume(vol * 0.5f);
        for (Music m : worldMusic) if (m != null) m.setVolume(vol * 0.5f);
    }

    // ... rest of your AudioManager class above ...

    public void dispose() {
        if (glitchBg != null) glitchBg.dispose();
        if (shiftSfx != null) shiftSfx.dispose();
        if (interactSfx != null) interactSfx.dispose();

        // Loop through and dispose of the new world music array
        for (Music m : worldMusic) {
            if (m != null) m.dispose();
        }
    }
}
