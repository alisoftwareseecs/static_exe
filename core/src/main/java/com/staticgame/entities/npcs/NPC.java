package com.staticgame.entities.npcs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.staticgame.entities.Entity;
import com.staticgame.entities.Player;
import com.staticgame.utils.GameState;

/**
 * Base for every character Elias encounters.
 * Each NPC has:
 *  - normalLines[]  — what they say in the real world (avoidant, contradictory, deflecting)
 *  - glitchLines[]  — what leaks out in memory state (raw, terrified, revealing)
 *  - stage          — conversation advances each interaction
 */
public abstract class NPC extends Entity {

    protected String   name;
    protected int      id;
    protected String[] normalLines;
    protected String[] glitchLines;
    protected int      stage        = 0;
    protected boolean  visible      = true;

    // Active dialogue
    protected String   activeLine   = null;
    protected float    lineTimer    = 0f;
    protected static final float LINE_DURATION = 5f;

    protected float    bobTimer     = 0f;

    public NPC(int id, String name, float x, float y, float w, float h,
               String[] normalLines, String[] glitchLines) {
        super(x, y, w, h);
        this.id=id; this.name=name;
        this.normalLines=normalLines; this.glitchLines=glitchLines;
    }

    /** Show appropriate line based on glitch state and stage. */
    public abstract void speak(boolean glitched, GameState gs);

    /** Called when player presses E nearby. */
    public abstract void onInteract(Player p, GameState gs);

    @Override
    public void update(float delta) {
        bobTimer += delta;
        if (lineTimer > 0) { lineTimer -= delta; if (lineTimer<=0) activeLine=null; }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        float bob = MathUtils.sin(bobTimer*1.6f)*3f;
        if (isGlitched) {
            batch.setColor(0.15f,1f,0.7f,0.25f);
            batch.draw(texture, x-3, y+bob+2, width, height);
            batch.setColor(0.85f,0.25f,1f,0.5f);
        } else batch.setColor(Color.WHITE);
        batch.draw(texture, x, y+bob, width, height);
        batch.setColor(Color.WHITE);
    }

    protected void show(String line) { activeLine=line; lineTimer=LINE_DURATION; }
    protected String pick(String[] lines, int idx) {
        if (lines==null||lines.length==0) return "...";
        return lines[Math.min(idx, lines.length-1)];
    }

    public String  getActiveLine()       { return activeLine; }
    public boolean hasActiveLine()       { return activeLine!=null; }
    public String  getName()             { return name; }
    public boolean isVisible()           { return visible; }
    public void    setVisible(boolean v) { visible=v; }
    public int     getId()               { return id; }
    public int     getStage()            { return stage; }
}
