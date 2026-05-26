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

public class NarrativeScreen implements Screen {

    private final StaticGame game;
    private SpriteBatch batch;
    private BitmapFont body, spkFont, hintFont;
    private GlyphLayout lay;
    private Texture pix;

    private static final String[][] PAGES = {
        {"","[ Day 11. ]\n\n[ Nadia has been missing for eleven days. ]\n\n[ Nobody is looking for her the way I am. ]"},
        {"ELIAS","She's not the kind of person who disappears.\nThat's what I keep telling people and they\nkeep nodding in the wrong way.\n\nLike they already know something I don't."},
        {"ELIAS","I haven't slept in four days. Maybe five.\nTime is doing this thing where it folds.\nI'll look up and three hours have gone\nand I have no memory of them at all."},
        {"ELIAS","The world started looking wrong around day six.\nNot wrong like danger. Wrong like a copy.\nLike someone photographed everything\nand replaced it while I was blinking."},
        {"ELIAS","But I found I could push through it.\nWhen the edges stop looking solid — I can force\nmyself into the layer underneath.\n\nIt hurts. It's the only thing that feels real."},
        {"ELIAS","People keep saying different things.\nMum says Tuesday. Hamza says Thursday.\nMr Voss says he saw her leave at 4am.\n\nOne of them is lying.\nMaybe all of them. Maybe me."},
        {"ELIAS","I'm going back through every place she walked.\nEvery conversation. Every room.\nI'm going to find the version of events\nthat is actually true.\n\nEven if it breaks everything."},
        {"","[ A / D or Arrow Keys to Move ]\n[ Space or W to Jump ]\n[ E to Interact ]\n[ R to force into Memory State ]\n\n[ The truth is somewhere in the static. ]"},
    };

    private int   page=0, charIdx=0;
    private float typeT=0, time=0, fade=0, exitFade=0, stripT=0;
    private boolean done=false;
    private int[] stripY=new int[5];

    public NarrativeScreen(StaticGame game) {
        this.game=game;
        batch=new SpriteBatch();
        body=new BitmapFont(); body.getData().setScale(1.08f);
        spkFont=new BitmapFont(); spkFont.getData().setScale(0.88f);
        hintFont=new BitmapFont(); hintFont.getData().setScale(0.76f);
        lay=new GlyphLayout();
        Pixmap pm=new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(1,1,1,1);pm.fill();pix=new Texture(pm);pm.dispose();
        shuffleStrips();
    }

    private void shuffleStrips(){int sh=Math.max(1,Gdx.graphics.getHeight());for(int i=0;i<stripY.length;i++)stripY[i]=MathUtils.random(0,sh);}

    @Override public void show(){}

    @Override
    public void render(float delta){
        time+=delta; fade=Math.min(1f,fade+delta*2f);
        stripT+=delta; if(stripT>0.13f){stripT=0;shuffleStrips();}
        if(!done){typeT+=delta;int add=(int)(typeT*38f);if(add>0){charIdx=Math.min(charIdx+add,PAGES[page][1].length());typeT=0;}if(charIdx>=PAGES[page][1].length())done=true;}
        boolean adv=Gdx.input.isKeyJustPressed(Input.Keys.SPACE)||Gdx.input.isKeyJustPressed(Input.Keys.ENTER)||Gdx.input.isKeyJustPressed(Input.Keys.E);
        if(adv){if(!done){charIdx=PAGES[page][1].length();done=true;}else nextPage();}
        if(exitFade>0){exitFade+=delta*1.4f;if(exitFade>=1f){game.setScreen(new GameScreen(game));return;}}
        int sw=Gdx.graphics.getWidth(),sh=Gdx.graphics.getHeight();
        Gdx.gl.glClearColor(0.03f,0.03f,0.06f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        if(MathUtils.random()<0.28f){for(int sy:stripY){batch.setColor(0.28f,0.85f,0.65f,0.06f);batch.draw(pix,0,sy,sw,MathUtils.random(1,4));}}
        String sp=PAGES[page][0],full=PAGES[page][1];
        String shown=full.substring(0,Math.min(charIdx,full.length()));
        float tx=sw*0.12f,tw=sw*0.76f,cy=sh*0.62f;
        pageDots(sw,sh);
        if(!sp.isEmpty()){spkFont.setColor(0.25f,1f,0.7f,fade);batch.setColor(0.2f,0.65f,0.48f,0.32f*fade);batch.draw(pix,tx,cy+26,80,1);batch.setColor(Color.WHITE);spkFont.draw(batch,"[ "+sp+" ]",tx,cy+22);cy-=8;}
        if(sp.isEmpty())body.setColor(0.48f,0.48f,0.62f,0.82f*fade);else body.setColor(0.88f,0.88f,1f,fade);
        body.draw(batch,shown,tx,cy,tw,-1,true);
        if(!done){float blink=MathUtils.sin(time*11f)>0?1f:0f;body.setColor(0.25f,1f,0.7f,blink);body.draw(batch,"|",tx+3,cy-6);}
        if(done&&exitFade<=0){float a=(MathUtils.sin(time*2.8f)+1f)*0.5f;String pr=page==PAGES.length-1?"[ PRESS SPACE TO BEGIN ]":"[ PRESS SPACE TO CONTINUE ]";hintFont.setColor(0.38f,0.82f,0.6f,a*fade);lay.setText(hintFont,pr);hintFont.draw(batch,pr,(sw-lay.width)/2f,sh*0.13f);}
        if(exitFade>0){batch.setColor(0,0,0,Math.min(1f,exitFade));batch.draw(pix,0,0,sw,sh);}
        batch.setColor(Color.WHITE);batch.end();
    }

    private void pageDots(int sw,int sh){
        float ds=12f,spc=16f,tot=PAGES.length*spc,sx=(sw-tot)/2f,sy=sh*0.07f;
        for(int i=0;i<PAGES.length;i++){float dx=sx+i*spc;if(i==page)batch.setColor(0.25f,1f,0.7f,1f);else if(i<page)batch.setColor(0.18f,0.48f,0.35f,0.72f);else batch.setColor(0.12f,0.18f,0.18f,0.48f);batch.draw(pix,dx,sy,i==page?ds+2:ds,ds);}
        batch.setColor(Color.WHITE);
    }

    private void nextPage(){if(page<PAGES.length-1){page++;charIdx=0;typeT=0;done=false;fade=0;}else exitFade=0.01f;}

    @Override public void resize(int w,int h){}
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){batch.dispose();body.dispose();spkFont.dispose();hintFont.dispose();pix.dispose();}
}
