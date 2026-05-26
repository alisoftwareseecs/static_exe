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

public class PauseScreen implements Screen {

    private final StaticGame game;
    private final Screen     prev;
    private SpriteBatch batch;
    private BitmapFont  titleFont, itemFont;
    private GlyphLayout lay;
    private Texture     overlay;

    private static final String[] ITEMS = { "RESUME", "SETTINGS", "QUIT TO MENU" };
    private int   sel = 0;
    private float time = 0, inputCD = 0;
    private float slideY = -280f;

    public PauseScreen(StaticGame game, Screen prev) {
        this.game = game; this.prev = prev;
        batch     = new SpriteBatch();
        titleFont = new BitmapFont(); titleFont.getData().setScale(1.9f);
        itemFont  = new BitmapFont(); itemFont.getData().setScale(1.25f);
        lay       = new GlyphLayout();
        Pixmap pm = new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(0.02f,0.02f,0.06f,0.80f); pm.fill();
        overlay   = new Texture(pm); pm.dispose();
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        time += delta;
        inputCD = Math.max(0, inputCD - delta);
        slideY  = Math.min(0f, slideY + 750f*delta);

        if (inputCD <= 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)||Gdx.input.isKeyJustPressed(Input.Keys.W))
                { sel=(sel-1+ITEMS.length)%ITEMS.length; inputCD=0.11f; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)||Gdx.input.isKeyJustPressed(Input.Keys.S))
                { sel=(sel+1)%ITEMS.length; inputCD=0.11f; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)||Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                activate();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) game.setScreen(prev);
        }

        int sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        batch.begin();

        // Full-screen tint
        batch.setColor(Color.WHITE); batch.draw(overlay, 0, 0, sw, sh);

        float pw=300, ph=210, px=(sw-pw)/2f, py=(sh-ph)/2f+slideY;

        // Panel border
        batch.setColor(0.18f,0.75f,0.55f,0.48f);
        batch.draw(overlay, px-2, py-2, pw+4, ph+4);
        // Panel bg
        batch.setColor(0.03f,0.03f,0.09f,0.96f);
        batch.draw(overlay, px, py, pw, ph);
        batch.setColor(Color.WHITE);

        // Title
        float tp = 0.68f+0.32f*MathUtils.sin(time*2.4f);
        titleFont.setColor(0.25f, tp, 0.65f, 1f);
        titleFont.draw(batch, "PAUSED", px+18, py+ph-16);

        // Separator
        batch.setColor(0.18f,0.55f,0.42f,0.38f);
        batch.draw(overlay, px+16, py+ph-52, pw-32, 1);
        batch.setColor(Color.WHITE);

        // Items
        float iy = py+ph-76, iStep=44f;
        for (int i=0; i<ITEMS.length; i++) {
            boolean s=(i==sel);
            float ey=iy-i*iStep;
            if (s) {
                batch.setColor(0.08f,0.42f,0.28f,0.32f);
                batch.draw(overlay, px+10, ey-20, pw-20, 30);
                batch.setColor(Color.WHITE);
                float ap=(MathUtils.sin(time*6f)+1f)*0.5f;
                itemFont.setColor(0.25f,1f,0.68f,0.72f+0.28f*ap);
                itemFont.draw(batch, ">", px+16, ey);
            } else itemFont.setColor(0.52f,0.52f,0.72f,0.82f);
            lay.setText(itemFont,ITEMS[i]);
            itemFont.draw(batch, ITEMS[i], px+(pw-lay.width)/2f, ey);
        }

        batch.end();
    }

    private void activate() {
        switch (sel) {
            case 0: game.setScreen(prev); break;
            case 1: game.setScreen(new SettingsScreen(game, this)); break;
            case 2: game.setScreen(new MenuScreen(game)); break;
        }
    }

    @Override public void resize(int w,int h){}
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){batch.dispose();titleFont.dispose();itemFont.dispose();overlay.dispose();}
}
