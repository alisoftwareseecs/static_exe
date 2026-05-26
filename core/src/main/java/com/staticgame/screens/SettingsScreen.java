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

public class SettingsScreen implements Screen {

    private final StaticGame game;
    private final Screen     ret;
    private SpriteBatch batch;
    private BitmapFont headF,bodyF;
    private GlyphLayout lay;
    private Texture pix;

    private float vol;
    private float time=0f;

    private static final float SLX=220f,SLW=280f,SLH=11f;

    public SettingsScreen(StaticGame game,Screen ret){
        this.game=game;this.ret=ret;
        vol=game.saveManager.loadVolume();
        batch=new SpriteBatch();
        headF=new BitmapFont();headF.getData().setScale(1.7f);
        bodyF=new BitmapFont();bodyF.getData().setScale(0.98f);
        lay=new GlyphLayout();
        Pixmap pm=new Pixmap(1,1,Pixmap.Format.RGBA8888);pm.setColor(1,1,1,1);pm.fill();
        pix=new Texture(pm);pm.dispose();
    }

    @Override public void show(){}

    @Override
    public void render(float delta){
        time+=delta;
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) ||Gdx.input.isKeyPressed(Input.Keys.A)){vol=Math.max(0f,vol-0.5f*delta);apply();}
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)||Gdx.input.isKeyPressed(Input.Keys.D)){vol=Math.min(1f,vol+0.5f*delta);apply();}
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))game.setScreen(ret);

        int sw=Gdx.graphics.getWidth(),sh=Gdx.graphics.getHeight();
        Gdx.gl.glClearColor(0.04f,0.04f,0.08f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        headF.setColor(0.35f,0.9f,0.68f,1f);lay.setText(headF,"SETTINGS");
        headF.draw(batch,"SETTINGS",(sw-lay.width)/2f,sh-38f);
        batch.setColor(0.18f,0.55f,0.40f,0.35f);batch.draw(pix,55,sh-68,sw-110,1);batch.setColor(Color.WHITE);

        float ry=sh-118f;
        bodyF.setColor(0.28f,1f,0.7f,1f);bodyF.draw(batch,"Master Volume",55,ry);
        batch.setColor(0.12f,0.12f,0.20f,1f);batch.draw(pix,SLX,ry-SLH-3,SLW,SLH);
        batch.setColor(0.25f,1f,0.68f,1f);batch.draw(pix,SLX,ry-SLH-3,SLW*vol,SLH);
        batch.setColor(1f,1f,1f,1f);batch.draw(pix,SLX+SLW*vol-4,ry-SLH-7,8,SLH+8);
        batch.setColor(Color.WHITE);
        bodyF.setColor(0.55f,0.88f,0.68f,1f);bodyF.draw(batch,(int)(vol*100)+"%",SLX+SLW+10,ry);

        float ky=sh-230f;
        bodyF.setColor(0.35f,0.9f,0.68f,0.80f);bodyF.draw(batch,"CONTROLS",55,ky);
        ky-=8;batch.setColor(0.18f,0.45f,0.32f,0.28f);batch.draw(pix,55,ky-4,sw-110,1);batch.setColor(Color.WHITE);ky-=26;
        String[][]binds={{"A / Left Arrow","Move left"},{"D / Right Arrow","Move right"},{"W / Space / Up","Jump"},{"E","Talk to someone / interact"},{"R","Force into memory state"},{"Esc","Pause"},{"F3","Debug overlay"}};
        for(String[]row:binds){bodyF.setColor(0.52f,0.52f,0.72f,0.85f);bodyF.draw(batch,row[0],55,ky);bodyF.setColor(0.75f,0.75f,0.90f,0.90f);bodyF.draw(batch,row[1],270,ky);ky-=26;}

        float ha=(MathUtils.sin(time*2.4f)+1f)*0.5f;
        bodyF.setColor(0.45f,0.45f,0.62f,0.55f+0.35f*ha);
        String hint="[ ESC ] Back  --  Changes saved automatically";
        lay.setText(bodyF,hint);bodyF.draw(batch,hint,(sw-lay.width)/2f,28f);

        batch.end();
    }

    private void apply(){game.audioManager.setMasterVolume(vol);game.saveManager.saveVolume(vol);}

    @Override public void resize(int w,int h){}
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){batch.dispose();headF.dispose();bodyF.dispose();pix.dispose();}
}
