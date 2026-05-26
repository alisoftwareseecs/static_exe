package com.staticgame.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align; // Fix: Added proper alignment import
import com.staticgame.entities.npcs.NPC;
import com.staticgame.utils.GameState;

public class UIManager {

    private BitmapFont  font, small, name;
    private SpriteBatch ui;
    private GlyphLayout lay;
    private Texture     panel, bar, pix;

    // NPC typewriter
    private String shown="", target="";
    private float  typeT=0; int typeIdx=0;
    private static final float CPS=40f;

    // Elias inner voice
    private String eliasLine="";
    private float  eliasTimer=0;
    private static final float ELIAS_DUR=5.5f;

    // Zone hint
    private String hint="";
    private float  hintTimer=0;
    private static final float HINT_DUR=7f;

    private float flicker=0;

    public UIManager() {
        ui    = new SpriteBatch();
        font  = new BitmapFont(); font.getData().setScale(1.0f);
        small = new BitmapFont(); small.getData().setScale(0.78f);
        name  = new BitmapFont(); name.getData().setScale(0.85f);
        lay   = new GlyphLayout();

        panel = makeTex(0.04f,0.04f,0.09f,0.88f);
        bar   = makeTex(0.18f,1f,0.58f,1f);
        pix   = makeTex(1f,1f,1f,1f);
    }

    private Texture makeTex(float r,float g,float b,float a) {
        Pixmap pm=new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(r,g,b,a); pm.fill();
        Texture t=new Texture(pm); pm.dispose(); return t;
    }

    public void update(float delta, NPC npc, boolean glitched) {
        flicker+=delta;
        if (eliasTimer>0) eliasTimer-=delta;
        if (hintTimer >0) hintTimer -=delta;

        // NPC typewriter
        if (npc!=null && npc.hasActiveLine()) {
            String raw="["+npc.getName()+"]  "+npc.getActiveLine();
            if (!raw.equals(target)) { target=raw; shown=""; typeIdx=0; typeT=0; }
            if (typeIdx<target.length()) {
                typeT+=delta;
                int add=(int)(typeT*CPS);
                if (add>0){ typeIdx=Math.min(typeIdx+add,target.length()); shown=target.substring(0,typeIdx); typeT=0; }
            }
        } else { target=""; shown=""; typeIdx=0; }
    }

    /** Elias speaks — inner monologue, amber box. */
    public void showElias(String line) { eliasLine=line; eliasTimer=ELIAS_DUR; }

    /** Puzzle hint — teal box mid-screen. */
    public void showHint(String h) { hint=h; hintTimer=HINT_DUR; }

    public void render(GameState gs, NPC nearby, boolean glitched, float shiftCD) {
        int sw=Gdx.graphics.getWidth(), sh=Gdx.graphics.getHeight();
        ui.begin();

        // Controls strip (top-left)
        small.setColor(0.3f,0.3f,0.45f,0.5f);
        small.draw(ui,"A/D Move   Space Jump   E Interact   R Shift Reality   F3 Debug",8,sh-8);

        // State label (top-centre)
        String st=glitched?"[ MEMORY STATE ]":"[ WAKING STATE ]";
        float sa=0.5f+0.4f*MathUtils.sin(flicker*3f);
        font.setColor(glitched?new Color(0.15f,1f,0.7f,sa):new Color(0.65f,0.65f,0.9f,0.42f));
        lay.setText(font,st); font.draw(ui,st,(sw-lay.width)/2f,sh-8);

        if (shiftCD>0){ small.setColor(1f,0.3f,0.3f,0.9f); lay.setText(small,"SHIFT RECHARGING"); small.draw(ui,"SHIFT RECHARGING",(sw-lay.width)/2f,sh-26); }

        // Truths found (top-right)
        String truthStr="TRUTHS: "+gs.truthsFound+"/5";
        small.setColor(0.4f,0.85f,0.6f,0.8f);
        lay.setText(small,truthStr); small.draw(ui,truthStr,sw-lay.width-10,sh-8);

        // Glitch bar
        float bw=110,bh=8,bx=sw-bw-10,by=sh-26;
        ui.setColor(0.08f,0.08f,0.14f,0.82f); ui.draw(panel,bx-2,by-2,bw+4,bh+4);
        float ri=0.2f+0.8f*gs.glitchIntensity, gi=1f-0.6f*gs.glitchIntensity;
        ui.setColor(ri,gi,0.5f,0.95f); ui.draw(bar,bx,by,bw*gs.glitchIntensity,bh);
        ui.setColor(Color.WHITE);
        small.setColor(0.4f,0.75f,0.55f,0.65f); small.draw(ui,"MIND FRAG.",bx,by-2);

        // Puzzle hint
        if (hintTimer>0) {
            float ha=Math.min(1f,hintTimer);
            float hw=sw*0.52f, hx=(sw-hw)/2f, hy=sh*0.74f;
            ui.setColor(0.03f,0.03f,0.08f,0.84f*ha); ui.draw(panel,hx,hy-26,hw,36);
            ui.setColor(0.18f,0.88f,0.58f,0.5f*ha); ui.draw(bar,hx,hy+8,hw,2);
            ui.setColor(Color.WHITE);
            small.setColor(0.35f,0.92f,0.65f,ha);
            lay.setText(small, hint);
            small.draw(ui, hint, (sw - lay.width) / 2f, hy);
        }

        // Interact prompt
        if (nearby!=null && !nearby.hasActiveLine()) {
            font.setColor(1f,1f,0.45f,0.9f);
            String pr="[E] Talk to "+nearby.getName();
            lay.setText(font,pr); font.draw(ui,pr,(sw-lay.width)/2f,110);
        }

        // NPC dialogue box (bottom)
        if (!shown.isEmpty()) drawNPCBox(shown,glitched,sw,sh);

        // Elias inner voice (above NPC box)
        if (eliasTimer>0 && !eliasLine.isEmpty()) drawEliasBox(eliasLine,Math.min(1f,eliasTimer),!shown.isEmpty(),sw,sh);

        ui.setColor(Color.WHITE);
        ui.end();
    }

    private void drawNPCBox(String text, boolean glitched, int sw, int sh) {
        float bw=sw*0.74f,bh=72,bx=(sw-bw)/2f,by=16;
        ui.setColor(Color.WHITE); ui.draw(panel,bx,by,bw,bh);
        if (glitched) ui.setColor(0.12f,1f,0.65f,0.9f);
        else          ui.setColor(0.4f,0.4f,0.78f,0.9f);
        ui.draw(pix,bx,by,3,bh); ui.draw(bar,bx+3,by+bh-2,bw-3,2);
        ui.setColor(Color.WHITE);
        if (glitched && MathUtils.sin(flicker*13f)>0.72f) font.setColor(1f,0.2f,0.88f,0.9f);
        else if (glitched) font.setColor(0.2f,1f,0.72f,1f);
        else font.setColor(0.88f,0.88f,1f,1f);

        // Fix: Use Align.left instead of -1
        font.draw(ui,text,bx+10,by+bh-10,bw-20,Align.left,true);
        font.setColor(Color.WHITE);
    }

    private void drawEliasBox(String text, float alpha, boolean npcActive, int sw, int sh) {
        float bw=sw*0.62f,bh=56,bx=(sw-bw)/2f,by=npcActive?96:16;
        ui.setColor(0.06f,0.04f,0.13f,0.9f*alpha); ui.draw(panel,bx,by,bw,bh);
        ui.setColor(1f,0.75f,0.18f,0.85f*alpha); ui.draw(pix,bx,by,3,bh);
        ui.setColor(Color.WHITE);
        name.setColor(1f,0.82f,0.28f,alpha); name.draw(ui,"ELIAS",bx+10,by+bh-6);
        font.setColor(0.95f,0.88f,0.68f,alpha);

        // Fix: Use Align.left instead of -1
        font.draw(ui,text,bx+10,by+bh-22,bw-20,Align.left,true);
        font.setColor(Color.WHITE);
    }

    public void renderBigMessage(String msg, float alpha) {
        int sw=Gdx.graphics.getWidth(),sh=Gdx.graphics.getHeight();
        ui.begin();

        // Fix: Actually render the black overlay when an empty string is passed
        if (msg.isEmpty() && alpha > 0f) {
            ui.setColor(0f, 0f, 0f, alpha);
            ui.draw(pix, 0, 0, sw, sh);
            ui.setColor(Color.WHITE);
        } else if (!msg.isEmpty()) {
            lay.setText(font,msg); font.setColor(1,1,1,alpha);
            font.draw(ui,msg,(sw-lay.width)/2f,sh/2f+20);
            font.setColor(Color.WHITE);
        }

        ui.end();
    }

    public void dispose() {
        ui.dispose(); font.dispose(); small.dispose(); name.dispose();
        panel.dispose(); bar.dispose(); pix.dispose();
    }
}