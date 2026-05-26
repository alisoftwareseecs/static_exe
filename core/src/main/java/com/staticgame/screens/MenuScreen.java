package com.staticgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.staticgame.StaticGame;

public class MenuScreen implements Screen {

    private final StaticGame game;
    private SpriteBatch batch;
    private BitmapFont titleFont, bodyFont, smallFont;
    private GlyphLayout lay;
    private Texture pix;

    private static final String[] ITEMS = { "BEGIN", "SETTINGS", "QUIT" };
    private int sel = 0;
    private float time = 0, inputCD = 0;
    private int[] stripY = new int[6];
    private float stripT = 0;
    private String saveStats = "";

    public MenuScreen(StaticGame game) {
        this.game = game;
        batch     = new SpriteBatch();
        titleFont = new BitmapFont(); titleFont.getData().setScale(3.0f);
        bodyFont  = new BitmapFont(); bodyFont.getData().setScale(1.15f);
        smallFont = new BitmapFont(); smallFont.getData().setScale(0.78f);
        lay       = new GlyphLayout();
        Pixmap pm = new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(1,1,1,1); pm.fill(); pix = new Texture(pm); pm.dispose();
        shuffleStrips();
        saveStats = game.saveManager.getSummary();
    }

    private void shuffleStrips() {
        int sh = Math.max(1, Gdx.graphics.getHeight());
        for (int i = 0; i < stripY.length; i++) stripY[i] = MathUtils.random(0, sh);
    }

    @Override public void show() { game.audioManager.stopAll(); }

    @Override
    public void render(float delta) {
        time += delta;
        inputCD = Math.max(0, inputCD - delta);
        stripT  += delta; if (stripT > 0.11f) { stripT = 0; shuffleStrips(); }

        if (inputCD <= 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)   || Gdx.input.isKeyJustPressed(Input.Keys.W))
                { sel = (sel-1+ITEMS.length)%ITEMS.length; inputCD=0.12f; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S))
                { sel = (sel+1)%ITEMS.length; inputCD=0.12f; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)||Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                activate();
        }

        int sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        Gdx.gl.glClearColor(0.03f, 0.03f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Scanline flicker
        if (MathUtils.random() < 0.32f) {
            for (int sy : stripY) {
                batch.setColor(0.25f, 0.85f, 0.6f, 0.07f);
                batch.draw(pix, 0, sy, sw, MathUtils.random(1,5));
            }
            batch.setColor(Color.WHITE);
        }

        // Title
        float pulse = MathUtils.sin(time*2f)*0.08f;
        titleFont.setColor(0.28f+pulse, 1f, 0.68f, 1f);
        String t = "STATIC.EXE";
        lay.setText(titleFont, t);
        float tx = (sw-lay.width)/2f, ty = sh*0.80f;
        if (MathUtils.sin(time*8.5f) > 0.68f) {
            titleFont.setColor(1f, 0.18f, 0.82f, 0.36f);
            titleFont.draw(batch, t, tx+MathUtils.random(-5f,5f), ty+MathUtils.random(-3f,3f));
        }
        titleFont.setColor(0.28f+pulse, 1f, 0.68f, 1f);
        titleFont.draw(batch, t, tx, ty);

        // Subtitle
        bodyFont.setColor(0.45f, 0.45f, 0.72f, 0.72f);
        String sub = "Day 11  |  Find Nadia";
        lay.setText(bodyFont, sub);
        bodyFont.draw(batch, sub, (sw-lay.width)/2f, sh*0.64f);

        // Menu items
        float itemY = sh*0.50f, step = 46f;
        for (int i = 0; i < ITEMS.length; i++) {
            boolean s = (i==sel);
            float y   = itemY - i*step;
            if (s) {
                lay.setText(bodyFont, ITEMS[i]);
                batch.setColor(0.12f, 0.45f, 0.32f, 0.28f);
                batch.draw(pix, (sw-lay.width)/2f-18, y-22, lay.width+36, 30);
                batch.setColor(Color.WHITE);
                float ap = (MathUtils.sin(time*6f)+1f)*0.5f;
                bodyFont.setColor(0.25f, 1f, 0.68f, 0.7f+0.3f*ap);
            } else {
                bodyFont.setColor(0.52f, 0.52f, 0.72f, 0.8f);
            }
            lay.setText(bodyFont, ITEMS[i]);
            bodyFont.draw(batch, ITEMS[i], (sw-lay.width)/2f, y);
        }

        // Nav hint
        smallFont.setColor(0.32f, 0.32f, 0.50f, 0.65f);
        String nav = "Up / Down to navigate     Enter to select";
        lay.setText(smallFont, nav);
        smallFont.draw(batch, nav, (sw-lay.width)/2f, itemY-ITEMS.length*step-10f);

        // Save stats panel
        batch.setColor(0.06f, 0.06f, 0.11f, 0.82f);
        batch.draw(pix, 38, 14, sw-76, 68);
        batch.setColor(0.18f, 0.48f, 0.35f, 0.38f);
        batch.draw(pix, 38, 80, sw-76, 1);
        batch.setColor(Color.WHITE);
        smallFont.setColor(0.42f, 0.78f, 0.58f, 0.82f);
        lay.setText(smallFont, saveStats);
        smallFont.draw(batch, saveStats, (sw-lay.width)/2f, 66f);

        // Endings found row
        String endStr = "Endings:  "
            + (game.saveManager.endingsFound()>0?"[GUILT] ":"[     ] ")
            + (game.saveManager.endingsFound()>1?"[TRUTH] ":"[     ] ")
            + (game.saveManager.endingsFound()>2?"[FRACTURE]":"[        ]");
        smallFont.setColor(0.38f, 0.38f, 0.55f, 0.70f);
        lay.setText(smallFont, endStr);
        smallFont.draw(batch, endStr, (sw-lay.width)/2f, 44f);

        batch.end();
    }

    private void activate() {
        switch (sel) {
            case 0: game.gameState.reset(); game.setScreen(new NarrativeScreen(game)); break;
            case 1: game.setScreen(new SettingsScreen(game, this)); break;
            case 2: Gdx.app.exit(); break;
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() {
        batch.dispose(); titleFont.dispose(); bodyFont.dispose(); smallFont.dispose(); pix.dispose();
    }
}
