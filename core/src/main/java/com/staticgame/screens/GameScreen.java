package com.staticgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.staticgame.StaticGame;
import com.staticgame.entities.Player;
import com.staticgame.entities.npcs.NPC;
import com.staticgame.systems.*;
import com.staticgame.world.BackgroundRenderer;
import com.staticgame.world.World;

/**
 * Main gameplay screen.
 *
 * Three maps loaded in sequence:
 *   Map 0 — The Apartment Block  (Theme: Denial)
 *   Map 1 — The University       (Theme: Repression)
 *   Map 2 — The Hospital         (Theme: Truth)
 *
 * Puzzle system:
 *   POND tiles block in normal world, vanish when Elias forces into memory state.
 *   BRIDGE tiles are invisible normally, solid in memory state.
 *   MEMORY_WALL tiles are solid walls normally, walk-through in memory state.
 *   DOOR_LOCKED tiles unlock automatically when enough truths are found.
 *
 * Each map has thematic Elias dialogue that fires on entry and at key moments.
 * NPC dialogue advances on each E press, and deeper truths emerge in glitch state.
 */
public class GameScreen implements Screen {

    private final StaticGame game;

    private World              world;
    private Player             player;
    private CollisionSystem    collision;
    private RealityShiftSystem shift;
    private CameraManager      cam;
    private UIManager          ui;
    private AudioManager       audio;
    private DebugOverlay       debug;
    private BackgroundRenderer bg;

    private SpriteBatch        worldBatch, screenBatch;
    private OrthographicCamera screenCam;

    private NPC   nearbyNPC   = null;
    private float shiftCD     = 0f;
    private float exitTimer   = 0f;
    private float mapFadeIn   = 1f;   // fades from black when map loads

    // Per-map Elias commentary — fired once on first entry
    // Each map has lines keyed to: 0=enter, 1=first_shift, 2=door_unlock, 3=exit_reached
    private static final String[][][] MAP_LINES = {
        {   // MAP 0 — Apartment
            { "ELIAS: This hallway. I've walked it a hundred\ntimes. Tonight it feels like it's watching me back." },
            { "ELIAS: There. The layer underneath. Everything\nhere is a memory of itself." },
            { "ELIAS: The door opened. Whatever I'm finding\nis real enough to change things." },
            { "ELIAS: I need to get out of this building.\nThe university. That's where to look next." },
        },
        {   // MAP 1 — University
            { "ELIAS: Empty. She sat in these lecture halls.\nI used to make fun of how seriously she took notes." },
            { "ELIAS: In the memory layer the projectors\nare still running. Like no one told them she's gone." },
            { "ELIAS: Something shifted. A door opened that\nwasn't open before. Keep going." },
            { "ELIAS: The hospital. Why the hospital?\nShe was never— wait. Was she?" },
        },
        {   // MAP 2 — Hospital
            { "ELIAS: The corridors keep changing.\nI've passed this room three times. I think." },
            { "ELIAS: Here everything is wrong in both layers.\nThere is no normal version of this place." },
            { "ELIAS: Something is ahead. I can feel it\nthe way you feel thunder before you hear it." },
            { "ELIAS: I found it. Whatever I was looking for.\nI'm not sure I wanted to." },
        },
    };

    private boolean[] mapLineFired = new boolean[4];
    private int       lastTruths   = 0;

    public GameScreen(StaticGame game) {
        this.game  = game;
        this.audio = game.audioManager;
        loadMap(game.gameState.currentMap);
    }

    private void loadMap(int idx) {
        // Dispose old world if swapping maps
        if (world != null) world.dispose();
        if (shift  != null) shift.dispose();
        if (cam    != null) { /* camera reused */ }

        game.gameState.currentMap = idx;
        game.gameState.isGlitched = false;
        world  = new World(idx);
        player = new Player(world.getSpawnX(), world.getSpawnY(), game.gameState);

        if (collision == null) collision = new CollisionSystem();
        shift = new RealityShiftSystem(world);
        if (ui    == null) ui    = new UIManager();
        if (debug == null) debug = new DebugOverlay();
        if (bg    == null) bg    = new BackgroundRenderer();

        int sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        if (worldBatch  == null) worldBatch  = new SpriteBatch();
        if (screenBatch == null) screenBatch = new SpriteBatch();

        cam = new CameraManager(sw, sh, world.getWidthPixels(), world.getHeightPixels());
        if (screenCam == null) { screenCam = new OrthographicCamera(); screenCam.setToOrtho(false, sw, sh); }

        mapLineFired = new boolean[4];
        lastTruths   = game.gameState.truthsFound;
        mapFadeIn    = 1f;
        shiftCD      = 0f;
        exitTimer    = 0f;

        // Fire map-entry line immediately
        ui.showElias(MAP_LINES[idx][0][0]);
        showMapHint(idx);
        // Trigger the correct music for this world
        audio.playWorldMusic(idx, game.gameState.isGlitched);
    }

    private void showMapHint(int idx) {
        switch (idx) {
            case 0: ui.showHint("DENIAL  --  Find what people are hiding. Shift reality [R] to see through the lies."); break;
            case 1: ui.showHint("REPRESSION  --  The university holds suppressed memories. Use [R] to surface them."); break;
            case 2: ui.showHint("TRUTH  --  The hospital contradicts itself. Both layers are broken. Find the exit."); break;
        }
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        handleInput();

        // Fade-in on map load
        if (mapFadeIn > 0) mapFadeIn = Math.max(0f, mapFadeIn - delta * 1.2f);

        player.update(delta);
        world.update(delta);
        shift.update(delta);
        debug.update(delta);
        bg.update(cam.getCentreX(), delta, game.gameState.isGlitched, game.gameState.currentMap);
        if (shiftCD > 0) shiftCD -= delta;

        // Unlock doors when truth threshold met
        world.checkDoorUnlock(game.gameState.truthsFound);

        // Fire Elias lines at story beats
        checkStoryBeats();

        nearbyNPC = world.getNPCNear(player);
        NPC speaking = null;
        for (NPC n : world.getNpcs()) if (n.hasActiveLine()) { speaking = n; break; }
        ui.update(delta, speaking, game.gameState.isGlitched);

        player.setGlitched(game.gameState.isGlitched);
        collision.step(player, world, delta);

        // Map transition — reached exit
        if (world.playerOnExit(player)) {
            exitTimer += delta;
            if (exitTimer >= 1.2f) {
                int next = game.gameState.currentMap + 1;
                if (next < 3) {
                    game.gameState.mapComplete[game.gameState.currentMap] = true;
                    loadMap(next);
                    return;
                } else {
                    // All maps done — ending
                    game.gameState.mapComplete[2] = true;
                    game.gameState.determineEnding();
                    game.saveManager.saveRun(game.gameState);
                    game.setScreen(new EndingScreen(game));
                    return;
                }
            }
        } else exitTimer = 0;

        cam.update(player, delta);
        if (shift.getShakeX() != 0) cam.shake(10f, 0.3f);

        // Clear colour — dark, shifts tone with glitch state and map
        float[] clr = mapClearColor(game.gameState.currentMap, game.gameState.isGlitched);
        Gdx.gl.glClearColor(clr[0], clr[1], clr[2], 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background (screen space)
        screenBatch.setProjectionMatrix(screenCam.combined);
        screenBatch.begin();
        bg.render(screenBatch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
                  game.gameState.isGlitched, game.gameState);
        screenBatch.end();

        // World + player (world space)
        worldBatch.setProjectionMatrix(cam.getCamera().combined);
        worldBatch.begin();
        world.render(worldBatch, delta);
        player.render(worldBatch);
        worldBatch.end();

        // Shift post-processing (screen space)
        screenBatch.begin();
        shift.renderEffects(screenBatch, game.gameState);
        screenBatch.end();

        // Map fade-in overlay
        if (mapFadeIn > 0) {
            screenBatch.begin();
            screenBatch.setProjectionMatrix(screenCam.combined);
            // draw black overlay at mapFadeIn alpha using a tiny draw call
            screenBatch.setColor(0, 0, 0, mapFadeIn);
            // We need a texture — reuse shift's approach; easier: just use UI
            screenBatch.end();
            ui.renderBigMessage("", mapFadeIn);  // black overlay trick via alpha
        }

        // HUD
        ui.render(game.gameState, nearbyNPC, game.gameState.isGlitched, shiftCD);

        if (exitTimer > 0) {
            String msg = game.gameState.currentMap < 2 ? "Moving on..." : "Entering the truth...";
            ui.renderBigMessage(msg, Math.min(1f, exitTimer));
        }

        debug.render(player, game.gameState, delta);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, this));
            return;
        }

        // --- REALITY SHIFT ('R') ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && shiftCD <= 0) {
            player.shiftReality(shift);
            shiftCD = 1.4f;
            audio.playShiftSound();

            // FIX: Update music to glitch version or back to world-specific version here!
            audio.playWorldMusic(game.gameState.currentMap, game.gameState.isGlitched);

            // First shift on a map — fire line
            if (!mapLineFired[1]) {
                mapLineFired[1] = true;
                ui.showElias(MAP_LINES[game.gameState.currentMap][1][0]);
            }
        }

        // --- INTERACT ('E') ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && nearbyNPC != null) {
            player.interact(nearbyNPC);
            audio.playInteractSound();
            // FIX: Removed the playWorldMusic call from here.
        }
    }

    private void checkStoryBeats() {
        // New truth found since last check
        if (game.gameState.truthsFound > lastTruths) {
            lastTruths = game.gameState.truthsFound;
        }

        // Door unlock line (fires once)
        int needed = (game.gameState.currentMap == 0) ? 2 : 4;
        if (!mapLineFired[2] && game.gameState.truthsFound >= needed) {
            mapLineFired[2] = true;
            ui.showElias(MAP_LINES[game.gameState.currentMap][2][0]);
        }

        // Approaching exit
        if (!mapLineFired[3] && world.playerOnExit(player)) {
            mapLineFired[3] = true;
            ui.showElias(MAP_LINES[game.gameState.currentMap][3][0]);
        }
    }

    private float[] mapClearColor(int map, boolean gl) {
        if (gl) return new float[]{ 0.01f, 0.05f, 0.03f };
        switch (map) {
            case 0:  return new float[]{ 0.06f, 0.05f, 0.09f }; // apartment — dark purple
            case 1:  return new float[]{ 0.06f, 0.07f, 0.08f }; // university — dark grey
            default: return new float[]{ 0.09f, 0.10f, 0.09f }; // hospital — grey-green
        }
    }

    @Override
    public void resize(int w, int h) {
        if (cam != null) cam.resize(w, h, world.getWidthPixels(), world.getHeightPixels());
        if (screenCam != null) screenCam.setToOrtho(false, w, h);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (world  != null) world.dispose();
        if (player != null) player.dispose();
        if (shift  != null) shift.dispose();
        if (bg     != null) bg.dispose();
        if (ui     != null) ui.dispose();
        if (debug  != null) debug.dispose();
        if (worldBatch  != null) worldBatch.dispose();
        if (screenBatch != null) screenBatch.dispose();
    }
}
