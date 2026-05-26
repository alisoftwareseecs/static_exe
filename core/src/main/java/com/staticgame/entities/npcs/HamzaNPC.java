package com.staticgame.entities.npcs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.staticgame.entities.Player;
import com.staticgame.utils.GameState;

/**
 * HAMZA — Elias's best friend. 22. Sarcastic armour over real fear.
 *
 * Normal world: deflects with jokes. Changes subject fast.
 * Gets irrationally angry when Elias mentions Nadia.
 * Contradicts what he said last time like the conversation never happened.
 *
 * Glitch state: the jokes stop. He remembers something he hasn't told Elias.
 * He and Nadia argued the week before she vanished. He said things.
 * He thinks it's his fault. They both do. That's the horror.
 */
public class HamzaNPC extends NPC {

    private static final String[] NORMAL = {
            "Hamza: Bro forget it. She's probably at her friend's.\nYou're spiralling again, man.",
            "Hamza: Can we just— can we not do this right now?\nI've got an exam Thursday.",
            "Hamza: I haven't seen her. Why does everyone keep\nasking me? I haven't seen her.",
            "Hamza: *He laughs at something on his phone.*\nSorry, what?",
            "Hamza: Okay you need to sleep. You look like actual\ndeath. When did you last eat?",
    };

    private static final String[] GLITCH = {
            "Hamza: We argued. The Wednesday. I said some stuff\nI— it wasn't serious. I didn't mean it.",
            "Hamza: She texted me after. I left it on read.\nI was angry. I was being stupid.",
            "Hamza: She said 'I feel like I'm disappearing and\nnobody notices.' I sent a thumbs up. A thumbs up.",
            "Hamza: I think she was asking for help.\nI think I knew and I made it a joke.",
            "Hamza: Why do you keep bringing her up?\n*He's shaking.*\nWhy won't you let me forget?",
    };

    // --- Animation Variables ---
    private Texture sheet;
    private Animation<TextureRegion> idleAnim;
    private float stateTime = 0f;

    // The visual drawing size (square to prevent squishing)
    private static final float DRAW_SIZE = 140f;

    public HamzaNPC(float x, float y) {
        super(1, "Hamza", x, y, 28, 46, NORMAL, GLITCH);

        try {
            // 1. Load the sprite sheet. PUT YOUR PATH HERE:
            sheet = new Texture(Gdx.files.internal("sprites/Characters/Reiner/Idle.png"));

            // 2. Split the sheet. Change 1536 if his frames are a different size!
            TextureRegion[][] tmp = TextureRegion.split(sheet, 128, 128);

            // 3. Flatten the array. Change the '7' if he has more/less frames!
            int totalFrames = 7;
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
            System.out.println("==== HAMZA SPRITE LOADING FAILED ====");
            e.printStackTrace();
            this.texture = colorTex(160, 175, 140); // Safety fallback
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

        // If the image failed to load, fall back to the old solid block logic
        if (idleAnim == null) {
            super.render(batch);
            return;
        }

        float bob = 0;

        // Get the correct frame of the animation
        TextureRegion currentFrame = idleAnim.getKeyFrame(stateTime);

        // Center the sprite over the physical hitbox
        float drawX = x + (width / 2f) - (DRAW_SIZE / 2f);

        // Anchor the drawing to the bottom so his feet touch the floor
        float drawY = y + bob;

        // Apply the glitch horror effect to the sprite
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
        if (glitched) {
            gs.heardHamza = true;
            if (stage >= 2) gs.foundTruth();
        }
    }

    @Override
    public void onInteract(Player p, GameState gs) {
        speak(gs.isGlitched, gs);
        stage = Math.min(stage+1, Math.max(NORMAL.length, GLITCH.length)-1);
    }
}