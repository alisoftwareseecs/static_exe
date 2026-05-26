package com.staticgame.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * One 40x40 world tile.
 *
 * Puzzle tile types and how they work:
 *
 *  POND         — solid (blocks) in NORMAL world. Vanishes when glitched.
 *                 Puzzle: reach the other side by shifting.
 *
 *  BRIDGE       — invisible/passable normally. Solid when glitched.
 *                 Puzzle: gap in normal world — shift to reveal the bridge.
 *
 *  MEMORY_WALL  — solid in normal world. In glitch state reveals a ghost
 *                 image of a room beyond. Walk through when glitched.
 *
 *  DOOR_LOCKED  — always solid until a truth is found (GameState.truthsFound
 *                 threshold). GameScreen checks this each frame.
 *
 *  WINDOW       — decorative. Shows different scene in each state.
 */
public class Tile {

    public static final int SIZE = 40;

    public enum Type {
        EMPTY, GROUND, PLATFORM, WALL,
        POND,           // blocks normally, gone when glitched
        BRIDGE,         // passable normally, solid when glitched
        MEMORY_WALL,    // blocks normally, walk-through when glitched
        DOOR_LOCKED,    // always solid until truth threshold met
        DOOR_OPEN,      // open version (swapped by GameScreen)
        EXIT,
        DECO_WINDOW,
        DECO_FURNITURE,
    }

    public Type  type;
    public float x, y;
    private Texture normalTex, glitchTex;
    private float   anim = 0f;

    public Tile(Type type, float x, float y) {
        this.type=type; this.x=x; this.y=y;
        build();
    }

    private void build() {
        switch (type) {
            case GROUND:       normalTex=t(55,50,72);   glitchTex=t(12,140,85);  break;
            case WALL:         normalTex=t(42,38,58);   glitchTex=t(8,110,68);   break;
            case PLATFORM:     normalTex=t(80,72,105);  glitchTex=t(20,170,110); break;
            case POND:
                // Blue rippling water in normal; nothing in glitch
                normalTex=t(25,65,185); glitchTex=null; break;
            case BRIDGE:
                // Nothing in normal; glowing teal in glitch
                normalTex=null; glitchTex=t(0,200,165); break;
            case MEMORY_WALL:
                normalTex=t(55,45,70); glitchTex=t(80,20,120); break;
            case DOOR_LOCKED:  normalTex=t(90,60,40);  glitchTex=t(150,40,20);  break;
            case DOOR_OPEN:    normalTex=t(55,40,25);  glitchTex=t(90,25,15);   break;
            case EXIT:         normalTex=t(230,230,255);glitchTex=t(255,50,50); break;
            case DECO_WINDOW:  normalTex=t(80,110,155); glitchTex=t(20,180,140);break;
            case DECO_FURNITURE:normalTex=t(95,75,55); glitchTex=t(130,50,90); break;
            default: break;
        }
    }

    private Texture t(int r,int g,int b){
        Pixmap pm=new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(r/255f,g/255f,b/255f,1f);pm.fill();
        Texture tx=new Texture(pm);pm.dispose();return tx;
    }

    public void render(SpriteBatch batch, boolean glitched, float delta) {
        anim += delta;
        Texture tx = glitched ? glitchTex : normalTex;
        if (tx==null) return;

        switch (type) {
            case POND:
                float wave = MathUtils.sin(anim*2.8f+x*0.04f)*0.12f;
                batch.setColor(0.1f+wave, 0.26f+wave, 0.73f, 0.88f);
                break;
            case BRIDGE:
                float bp=(MathUtils.sin(anim*3.5f)+1f)*0.5f;
                batch.setColor(0f, 0.78f+0.22f*bp, 0.65f, 0.75f+0.25f*bp);
                break;
            case MEMORY_WALL:
                if (glitched) {
                    float mp=(MathUtils.sin(anim*5f)+1f)*0.5f;
                    batch.setColor(0.3f+0.4f*mp, 0.05f, 0.45f+0.3f*mp, 0.85f);
                } else batch.setColor(Color.WHITE);
                break;
            case EXIT:
                float ep=(MathUtils.sin(anim*2.5f)+1f)*0.5f;
                if (glitched) batch.setColor(1f,0.18f*ep,0.18f*ep,1f);
                else          batch.setColor(1f,1f,0.55f+0.45f*ep,1f);
                break;
            case DOOR_LOCKED:
                float flk=MathUtils.sin(anim*6f)>0.5f?1f:0.7f;
                batch.setColor(flk,0.6f*flk,0.3f*flk,1f);
                break;
            default: batch.setColor(Color.WHITE);
        }

        batch.draw(tx, x, y, SIZE, SIZE);
        batch.setColor(Color.WHITE);
    }

    public boolean isSolid(boolean glitched) {
        switch (type) {
            case GROUND: case WALL: case PLATFORM: case DOOR_LOCKED: return true;
            case MEMORY_WALL: return !glitched;
            case POND:        return !glitched;
            case BRIDGE:      return glitched;
            case DOOR_OPEN:   return false;
            case EXIT:        return false;
            default:          return false;
        }
    }

    public boolean isExit()       { return type==Type.EXIT; }
    public boolean isDoorLocked() { return type==Type.DOOR_LOCKED; }
    public Rectangle getBounds()  { return new Rectangle(x,y,SIZE,SIZE); }
    public float getX()           { return x; }
    public float getY()           { return y; }

    public void dispose() {
        if (normalTex!=null) normalTex.dispose();
        if (glitchTex!=null) glitchTex.dispose();
    }
}
