package com.staticgame.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.staticgame.entities.Player;
import com.staticgame.utils.GameState;

public class DebugOverlay {
    private boolean visible=false;
    private BitmapFont font; private SpriteBatch b; private Texture bg;
    private float[] fps=new float[30]; private int fi=0; private float avg=60;

    public DebugOverlay() {
        font=new BitmapFont(); font.getData().setScale(0.8f);
        b=new SpriteBatch();
        Pixmap pm=new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(0,0,0,0.72f);pm.fill();bg=new Texture(pm);pm.dispose();
    }

    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) visible=!visible;
        fps[fi++%30]=1f/Math.max(delta,0.0001f); float s=0; for(float f:fps)s+=f; avg=s/30;
    }

    public void render(Player p, GameState gs, float delta) {
        if (!visible) return;
        int sw=Gdx.graphics.getWidth();
        String[] lines={
            "[ F3 to hide ]",
            String.format("FPS: %.0f  dt:%.3f",avg,delta),
            String.format("POS  x:%.1f  y:%.1f",p.getX(),p.getY()),
            String.format("VEL  x:%.1f  y:%.1f",p.getVelocityX(),p.getVelocityY()),
            "Grounded: "+p.isGrounded(),
            "Glitched: "+gs.isGlitched,
            String.format("Intensity: %.2f",gs.glitchIntensity),
            "Shifts: "+gs.shiftCount,
            "Truths: "+gs.truthsFound+"/5",
            "AccusedSelf: "+gs.accusedSelf,
            "Map: "+gs.currentMap,
            "Ending: "+(gs.shiftCount>=15?"FRACTURE":gs.truthsFound>=4&&!gs.accusedSelf?"TRUTH":"GUILT"),
        };
        float pw=210,ph=lines.length*15+8,px=sw-pw-8,py=8;
        b.begin();
        b.setColor(Color.WHITE); b.draw(bg,px,py,pw,ph);
        for (int i=0;i<lines.length;i++) {
            float ty=py+ph-8-i*15;
            font.setColor(i==0?new Color(0.3f,1f,0.7f,0.9f):new Color(0.85f,0.85f,0.95f,0.85f));
            font.draw(b,lines[i],px+6,ty);
        }
        b.setColor(Color.WHITE); b.end();
    }

    public void dispose(){ font.dispose();b.dispose();bg.dispose(); }
}
