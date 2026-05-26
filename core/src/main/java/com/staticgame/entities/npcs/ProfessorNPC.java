package com.staticgame.entities.npcs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.staticgame.entities.Player;
import com.staticgame.utils.GameState;

/**
 * DR. MARSH — Elias's uni professor. Detached, academic.
 * Normal: talks about coursework like nothing's wrong. Mildly irritated.
 * Glitch: Nadia was in one of his lectures. She left something behind.
 * He never handed it in. He doesn't know why.
 */
public class ProfessorNPC extends NPC {

    private static final String[] NORMAL = {
            "Dr. Marsh: Mr. Elias. You missed last Thursday.\nThat's the second time.",
            "Dr. Marsh: I don't offer extensions for personal\ncircumstances. It sets a precedent.",
            "Dr. Marsh: She wasn't in my seminar group, so\nI couldn't speak to that.",
            "Dr. Marsh: You look unwell. You should go home.",
    };

    private static final String[] GLITCH = {
            "Dr. Marsh: She sat in the back row. She used to\nstay after everyone left.",
            "Dr. Marsh: She left a notebook here.\nI put it in my desk. I don't know why I didn't return it.",
            "Dr. Marsh: She wrote something on the last page.\nI read it. I shouldn't have.\nIt wasn't for me.",
            "Dr. Marsh: She wrote: 'If someone finds this —\nI'm not lost. I chose this. Please don't look.'",
    };

    // --- Animation Variables ---
    private Texture sheet;
    private Animation<TextureRegion> idleAnim;
    private float stateTime = 0f;

    // Tweak this to make him bigger/smaller next to Elias!
    private static final float DRAW_SIZE = 120f;

    public ProfessorNPC(float x, float y) {
        super(3, "Dr. Marsh", x, y, 28, 46, NORMAL, GLITCH);

        try {
            // 1. Load the sprite sheet (Update this path to wherever Idle_2.png is!)
            sheet = new Texture(Gdx.files.internal("sprites/Characters/Jean/Idle_2.png"));

            // 2. Split the sheet into 128x128 pixel chunks
            TextureRegion[][] tmp = TextureRegion.split(sheet, 128, 128);

            // 3. Extract exactly 13 frames (1664 / 128 = 13)
            int totalFrames = 13;
            TextureRegion[] frames = new TextureRegion[totalFrames];
            int index = 0;
            for (int i = 0; i < tmp.length; i++) {
                for (int j = 0; j < tmp[i].length; j++) {
                    if (index < totalFrames) {
                        frames[index++] = tmp[i][j];
                    }
                }
            }

            // 4. Create the animation (0.12f speed)
            idleAnim = new Animation<>(0.12f, frames);
            idleAnim.setPlayMode(Animation.PlayMode.LOOP);

        } catch (Exception e) {
            System.out.println("==== PROFESSOR SPRITE LOADING FAILED ====");
            e.printStackTrace();
            this.texture = colorTex(130, 145, 160); // Safety fallback
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;

        if (idleAnim == null) {
            super.render(batch);
            return;
        }

        TextureRegion currentFrame = idleAnim.getKeyFrame(stateTime);

        // Center the sprite over the physical hitbox
        float drawX = x + (width / 2f) - (DRAW_SIZE / 2f);

        // Anchor the drawing to the floor (Tweaked downward by 10 pixels)
        float drawY = y - 5f;

        // Glitch Horror Effect
        if (isGlitched) {
            batch.setColor(0.15f, 1f, 0.7f, 0.25f);
            batch.draw(currentFrame, drawX - 3, drawY + 2, DRAW_SIZE, DRAW_SIZE);
            batch.setColor(0.85f, 0.25f, 1f, 0.5f);
        } else {
            batch.setColor(Color.WHITE);
        }

        batch.draw(currentFrame, drawX, drawY, DRAW_SIZE, DRAW_SIZE);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (sheet != null) sheet.dispose();
    }

    @Override
    public void speak(boolean glitched, GameState gs) {
        show(pick(glitched ? GLITCH : NORMAL, stage));
        if (glitched && stage >= 2) {
            gs.foundTruth();
            gs.readNadiaNote = true;
        }
    }

    @Override
    public void onInteract(Player p, GameState gs) {
        speak(gs.isGlitched, gs);
        stage = Math.min(stage+1, Math.max(NORMAL.length, GLITCH.length)-1);
    }
}