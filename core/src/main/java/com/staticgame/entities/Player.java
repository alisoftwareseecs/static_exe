package com.staticgame.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.staticgame.entities.npcs.NPC;
import com.staticgame.systems.RealityShiftSystem;
import com.staticgame.utils.GameState;

public class Player extends Entity {

    private static final float SPEED         = 190f;
    private static final float JUMP_FORCE    = 520f;
    private static final float INTERACT_RANGE = 85f;

    private boolean isGrounded    = false;
    private boolean alive         = true;
    private float   interactCD    = 0f;
    private float   shiftCD       = 0f;
    private float   animTimer     = 0f;
    private float   shakeX, shakeY, shakeTimer;
    private GameState gs;


    public Player(float x, float y, GameState gs) {
        super(x, y, 26, 46);
        this.gs  = gs;
        texture  = colorTex(210, 215, 240);
    }

    @Override
    public void update(float delta) {
        if (!alive) return;
        if (interactCD > 0) interactCD -= delta;
        if (shiftCD    > 0) shiftCD    -= delta;
        animTimer += delta * 5f;

        // Horizontal input
        velocityX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  velocityX = -SPEED;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velocityX =  SPEED;

        // Jump
        if (isGrounded &&
           (Gdx.input.isKeyJustPressed(Input.Keys.W)     ||
            Gdx.input.isKeyJustPressed(Input.Keys.UP)    ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            velocityY  = JUMP_FORCE;
            isGrounded = false;
        }

        // Glitch body shake
        if (isGlitched) {
            shakeTimer += delta;
            if (shakeTimer > 0.04f) {
                shakeTimer = 0;
                float m = 2.5f * gs.glitchIntensity;
                shakeX = MathUtils.random(-m,m);
                shakeY = MathUtils.random(-m,m);
            }
        } else { shakeX=0; shakeY=0; }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!alive) return;
        float dx = x+shakeX, dy = y+shakeY;

        // Glitch ghost
        if (isGlitched && MathUtils.sin(animTimer)>0.5f) {
            batch.setColor(0.15f,1f,0.7f,0.28f);
            batch.draw(texture, dx+5, dy-2, width, height);
            batch.setColor(1f,0.1f,0.8f,0.18f);
            batch.draw(texture, dx-4, dy+1, width, height);
        }

        if (isGlitched) {
            float p=(MathUtils.sin(animTimer*0.4f)+1f)*0.5f;
            batch.setColor(0.65f+0.35f*p, 0.7f, 1f, 1f);
        } else batch.setColor(Color.WHITE);

        batch.draw(texture, dx, dy, width, height);
        batch.setColor(Color.WHITE);
    }

    public void interact(NPC npc) {
        if (interactCD>0||npc==null) return;
        float dx=(npc.getX()+npc.getWidth()/2f)-(x+width/2f);
        float dy=(npc.getY()+npc.getHeight()/2f)-(y+height/2f);
        if (Math.sqrt(dx*dx+dy*dy)<=INTERACT_RANGE) {
            npc.onInteract(this, gs);
            interactCD=0.5f;
        }
    }

    public void shiftReality(RealityShiftSystem rss) {
        if (shiftCD>0) return;
        rss.shift(gs);
        shiftCD=1.4f;
    }

    // Called by CollisionSystem
    public void setGrounded(boolean g)  { isGrounded=g; }
    public void setVelocityY(float vy)  { velocityY=vy; }
    public void setVelocityX(float vx)  { velocityX=vx; }

    public boolean isGrounded()         { return isGrounded; }
    public boolean isAlive()            { return alive; }
    public float   getVelocityX()       { return velocityX; }
    public float   getVelocityY()       { return velocityY; }
    public float   getInteractRange()   { return INTERACT_RANGE; }
    public float   getShiftCD()         { return shiftCD; }
}
