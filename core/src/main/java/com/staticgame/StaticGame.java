package com.staticgame;

import com.badlogic.gdx.Game;
import com.staticgame.screens.MenuScreen;
import com.staticgame.systems.AudioManager;
import com.staticgame.utils.GameState;
import com.staticgame.utils.SaveManager;

public class StaticGame extends Game {

    public AudioManager audioManager;
    public GameState    gameState;
    public SaveManager  saveManager;

    @Override
    public void create() {
        // Initialize managers
        audioManager = new AudioManager();
        saveManager  = new SaveManager();
        gameState    = new GameState();

        // Load settings
        audioManager.setMasterVolume(saveManager.loadVolume());

        // Start the game
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render(); // Critical: Tells LibGDX to render the active screen
    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
    }

    @Override
    public void dispose() {
        // 1. Tell the audio manager to clean up its own internal tracks
        audioManager.dispose();

        // 2. Clean up the active screen
        if (getScreen() != null) {
            getScreen().dispose();
        }

        // 3. Let the base Game class finish the cleanup
        super.dispose();
    }
}