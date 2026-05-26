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
 * MUM — Elias's mother. Elena.
 *
 * Normal world: exhausted, avoidant. Makes tea. Checks her phone.
 * Changes the subject. Contradicts herself on what day Nadia left.
 * Won't look Elias in the eye.
 *
 * Glitch state: the mask slips. She's replaying the same hour over and over —
 * the hour before Nadia left. You realise she knew something was wrong
 * and said nothing. She carries that.
 */
public class MotherNPC extends NPC {

    private static final String[] NORMAL = {
            "Elena: She'll come back. You know how she gets. Just\nneed space.",
            "Elena: Stop asking about it, Elias. You're making it\nworse.",
            "Elena: She messaged me Tuesday. Or... Monday. I don't\nknow. One of them.",
            "Elena: Do you want tea? I'll make tea.\n...\nWhere did I put the— never mind.",
            "Elena: She was fine. She was completely fine.\n*She won't meet your eyes.*",
    };

    private static final String[] GLITCH = {
            "Elena: She said she needed to talk to me that morning.\nI told her later. I always say later.",
            "Elena: I heard her crying through the wall the night\nbefore. I didn't knock. I didn't knock.",
            "Elena: She left a note on the fridge. I threw it away\nbefore I read it. I don't know why.",
            "Elena: It wasn't Monday. It was Thursday. I've been\nlying. I don't know why I keep lying.",
            "Elena: She said 'I love you' when she left. Like she\nmeant it as a goodbye. I knew. I knew.",
    };

    // --- Animation Variables ---
    private Texture sheet;
    private Animation<TextureRegion> idleAnim;
    private float stateTime = 0f;

    // The visual drawing size (square to prevent squishing the 1536x1536 frames)
    private static final float DRAW_SIZE = 100f;

    public MotherNPC(float x, float y) {
        super(0, "Elena (Mum)", x, y, 28, 46, NORMAL, GLITCH);

        try {
            // 1. Load the sprite sheet
            sheet = new Texture(Gdx.files.internal("sprites/Characters/Joanna/Mother.png"));

            // 2. Split the sheet into 1536x1536 chunks
            TextureRegion[][] tmp = TextureRegion.split(sheet, 1536, 1536);

            // 3. Flatten the array to extract exactly 7 frames, regardless of row/col layout
            TextureRegion[] frames = new TextureRegion[7];
            int index = 0;
            for (int i = 0; i < tmp.length; i++) {
                for (int j = 0; j < tmp[i].length; j++) {
                    if (index < 7) {
                        frames[index++] = tmp[i][j];
                    }
                }
            }

            // 4. Create the animation (0.12f feels like a natural breathing/idle speed)
            idleAnim = new Animation<>(0.12f, frames);
            idleAnim.setPlayMode(Animation.PlayMode.LOOP);

        } catch (Exception e) {
            System.out.println("==== SPRITE LOADING FAILED ====");
            e.printStackTrace(); // This prints the exact reason it failed!
            this.texture = colorTex(180, 140, 160); // Safety fallback
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        // Advance the animation timer
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

        // Get the correct frame of the animation based on how much time has passed
        TextureRegion currentFrame = idleAnim.getKeyFrame(stateTime);

        // Center the 120px wide sprite directly over the 28px wide physical hitbox
        float drawX = x + (width / 2f) - (DRAW_SIZE / 2f);

        // Anchor the drawing to the bottom of the hitbox so her feet touch the floor
        float drawY = y - 15f;

        // Keep the psychological horror glitch effect on the actual sprite!
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
        // Prevent memory leaks by destroying the heavy texture when the map unloads
        if (sheet != null) sheet.dispose();
    }

    @Override
    public void speak(boolean glitched, GameState gs) {
        show(pick(glitched ? GLITCH : NORMAL, stage));
        if (glitched && stage >= 3) gs.foundTruth();
    }

    @Override
    public void onInteract(Player p, GameState gs) {
        speak(gs.isGlitched, gs);
        stage = Math.min(stage + 1, Math.max(NORMAL.length, GLITCH.length) - 1);
    }
}