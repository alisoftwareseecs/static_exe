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

/**
 * Three endings:
 *
 * ENDING 1 — GUILT
 *   Elias didn't find enough truths, or spiralled into self-blame.
 *   He stops looking. He tells himself she'll come back.
 *   The loop continues.
 *
 * ENDING 2 — TRUTH
 *   Elias pieced it together. Nadia left deliberately. She left a note.
 *   She's alive. She needed to disappear to survive.
 *   He has to let her go.
 *
 * ENDING 3 — FRACTURE
 *   Elias forced himself into memory too many times.
 *   He can no longer tell which layer is real.
 *   He's still in the apartment. He never left.
 *   The text corrupts.
 */
public class EndingScreen implements Screen {

    private final StaticGame game;
    private SpriteBatch batch;
    private BitmapFont title, body, prompt;
    private GlyphLayout lay;
    private Texture pix;

    private static final String[] TITLES = {
        "", "ENDING I: GUILT", "ENDING II: TRUTH", "ENDING III: FRACTURE"
    };

    private static final String[] TEXTS = {
        "",
        // GUILT
        "He stopped going to the apartment building.\n\n" +
        "He told himself he needed rest.\n" +
        "He told himself she would come back.\n\n" +
        "He told himself so many things\n" +
        "that eventually they all blurred together\n" +
        "into a single story that was easier to live in.\n\n" +
        "He became very good at not remembering\n" +
        "what Hamza said.\n" +
        "What his mother's face looked like at 4am.\n" +
        "What the note on the fridge had said\n" +
        "before it disappeared.\n\n" +
        "Day 11 became Day 40 became a year.\n\n" +
        "He never found her.\n" +
        "He never stopped waiting.",

        // TRUTH
        "The notebook was in Dr. Marsh's desk.\n\n" +
        "The last page said:\n\n" +
        "     'If someone finds this --\n" +
        "      I'm not lost. I chose this.\n" +
        "      Please don't look.\n" +
        "      I need people to stop looking\n" +
        "      so I can figure out how to exist.'\n\n" +
        "Elias read it four times.\n\n" +
        "She was alive.\n" +
        "She had left because staying\n" +
        "was killing something in her\n" +
        "that she couldn't name yet.\n\n" +
        "He put the notebook back.\n\n" +
        "He went home.\n" +
        "He slept for the first time in two weeks.\n\n" +
        "He would wait.\n" +
        "Not the way he had been.\n" +
        "The other kind of waiting.\n" +
        "The kind that trusts.",

        // FRACTURE
        "He is still in the apartment.\n\n" +
        "He has been in the apartment the whole time.\n\n" +
        "The hallway. The university. The hospital.\n" +
        "He never left.\n\n" +
        "He pushed into the memory layer too many times\n" +
        "and somewhere in the shifting\n" +
        "he lost which side was real.\n\n" +
        "He is sitting on the floor of his room.\n" +
        "Day 11.\n\n" +
        "He will stand up.\n" +
        "He will walk into the hallway.\n" +
        "The world will look wrong.\n\n" +
        "He will begin again.\n\n" +
        "X X X X X X X X X X X X X X X\n" +
        "X X X X X X X X X X X X X X X"
    };

    private static final float[][] BG = {
        {}, {0.05f,0.04f,0.10f}, {0.02f,0.07f,0.05f}, {0.09f,0.01f,0.01f}
    };
    private static final float[][] TC = {
        {}, {0.65f,0.65f,0.9f}, {0.2f,1f,0.7f}, {1f,0.22f,0.22f}
    };

    private int    end;
    private String full, shown="";
    private int    ci=0; private float tt=0;
    private boolean textDone=false;
    private char[] corruptBuf;
    private float  corruptFlip=0, time=0, fadeIn=0;

    public EndingScreen(StaticGame game) {
        this.game=game;
        end=MathUtils.clamp(game.gameState.endingChoice,1,3);
        full=TEXTS[end]; corruptBuf=full.toCharArray();
        batch=new SpriteBatch();
        title=new BitmapFont(); title.getData().setScale(2f);
        body=new BitmapFont();  body.getData().setScale(1.02f);
        prompt=new BitmapFont();prompt.getData().setScale(0.88f);
        lay=new GlyphLayout();
        Pixmap pm=new Pixmap(1,1,Pixmap.Format.RGBA8888);pm.setColor(1,1,1,1);pm.fill();
        pix=new Texture(pm);pm.dispose();
    }

    @Override public void show(){game.audioManager.stopAll();}

    @Override
    public void render(float delta){
        time+=delta; fadeIn=Math.min(1f,fadeIn+delta*0.5f);
        advanceType(delta);
        if(end==3&&textDone){corruptFlip+=delta;if(corruptFlip>0.06f){corruptFlip=0;corruptChar();}}

        float[] bg=BG[end];
        Gdx.gl.glClearColor(bg[0],bg[1],bg[2],1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int sw=Gdx.graphics.getWidth(),sh=Gdx.graphics.getHeight();
        batch.begin();

        vignette(sw,sh);
        if(end==3)scanlines(sw,sh);

        // Title
        float[] tc=TC[end];
        float ta=fadeIn*(end==3&&MathUtils.sin(time*10f)>0.7f?0.28f:1f);
        title.setColor(tc[0],tc[1],tc[2],ta);
        lay.setText(title,TITLES[end]);title.draw(batch,TITLES[end],(sw-lay.width)/2f,sh*0.85f);

        // Body
        if(end==3){float f=MathUtils.sin(time*8f)>0.4f?1f:0.7f;body.setColor(1f,0.35f*f,0.35f*f,fadeIn);}
        else if(end==2) body.setColor(0.85f,1f,0.9f,fadeIn);
        else            body.setColor(0.78f,0.78f,0.92f,fadeIn);
        String drawn=(end==3&&textDone)?new String(corruptBuf):shown;
        body.draw(batch,drawn,sw*0.14f,sh*0.76f,sw*0.72f,-1,true);
        body.setColor(Color.WHITE);

        if(textDone){
            // Stats
            prompt.setColor(0.38f,0.38f,0.55f,0.82f);
            prompt.draw(batch,"Truths found: "+game.gameState.truthsFound+"/5   Shifts: "+game.gameState.shiftCount+"   Runs: "+game.saveManager.loadSessions(),sw*0.14f,sh*0.18f);
            // Prompt
            float blink=(MathUtils.sin(time*3.2f)+1f)*0.5f;
            prompt.setColor(0.68f,0.68f,0.48f,0.5f+0.5f*blink);
            String pr="[ ENTER ] Play Again     [ M ] Main Menu";
            lay.setText(prompt,pr);prompt.draw(batch,pr,(sw-lay.width)/2f,sh*0.10f);
        }

        batch.end();

        if(textDone){
            if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)||Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
                game.gameState.reset(); game.setScreen(new GameScreen(game));
            }
            if(Gdx.input.isKeyJustPressed(Input.Keys.M)||Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
                game.gameState.reset(); game.setScreen(new MenuScreen(game));
            }
        }
    }

    private void advanceType(float delta){
        if(textDone)return;
        tt+=delta;int add=(int)(tt*26f);
        if(add>0){ci=Math.min(ci+add,full.length());shown=full.substring(0,ci);tt=0;}
        if(ci>=full.length()){textDone=true;shown=full;System.arraycopy(full.toCharArray(),0,corruptBuf,0,full.length());}
    }

    private void corruptChar(){
        if(corruptBuf.length==0)return;
        int pos=MathUtils.random(0,corruptBuf.length-1);
        if(corruptBuf[pos]=='\n'||corruptBuf[pos]==' ')return;
        if(MathUtils.random()<0.32f)corruptBuf[pos]=full.charAt(pos);
        else{char[]g={'X','#','?','!','*','%','/','\\'};corruptBuf[pos]=g[MathUtils.random(g.length-1)];}
    }

    private void vignette(int sw,int sh){
        int vw=sw/5,vh=sh/5;
        batch.setColor(0,0,0,0.55f);
        batch.draw(pix,0,0,vw,sh);batch.draw(pix,sw-vw,0,vw,sh);
        batch.draw(pix,0,0,sw,vh);batch.draw(pix,0,sh-vh,sw,vh);
        batch.setColor(Color.WHITE);
    }

    private void scanlines(int sw,int sh){
        batch.setColor(0f,0.55f,0.28f,0.04f);
        for(int y=0;y<sh;y+=3)batch.draw(pix,0,y,sw,1);
        batch.setColor(Color.WHITE);
    }

    @Override public void resize(int w,int h){}
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){batch.dispose();title.dispose();body.dispose();prompt.dispose();pix.dispose();}
}
