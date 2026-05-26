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
 * MR. VOSS — Neighbour. Retired. Watches everything from his doorway.
 * Normal: paranoid about the building, passive-aggressive, deflects.
 * Glitch: saw Nadia leave. Describes it in detail. Didn't think to mention it.
 */
public class NeighborNPC extends NPC {

    private static final String[] NORMAL = {
            "Voss: The lift's been broken three weeks. I've\nlogged it twice. Nobody reads the logs.",
            "Voss: I see everything from this doorway. This\nbuilding has no secrets from me.",
            "Voss: Your flat makes noise at 3am. I've noted\nit. I have a file.",
            "Voss: I'm not saying anything happened. I'm just\nsaying what I observed. That's all.",
    };

    private static final String[] GLITCH = {
            "Voss: She left at 4:17am. I know because I\ncouldn't sleep. I can't sleep anymore.",
            "Voss: She had a bag. Not packed — just one thing\ninside. Something flat. An envelope maybe.",
            "Voss: She looked up at your window before she left.\nStood there for almost a minute.",
            "Voss: I should have said something to you sooner.\nI told myself it wasn't my business.\nI'm sorry.",
    };

    // --- Animation Variables ---
    private Texture sheet;
    private Animation<TextureRegion> idleAnim;
    private float stateTime = 0f;

    // Tweak this to make him bigger/smaller next to Elias!
    private static final float DRAW_SIZE = 100f;

    public NeighborNPC(float x, float y) {
        super(2, "Mr. Voss (Neighbour)", x, y, 28, 46, NORMAL, GLITCH);

        try {
            // 1. Load the sprite sheet (Update this path if it's different!)
            sheet = new Texture(Gdx.files.internal("sprites/Characters/Blacksmith/BLACKSMITH.png"));

            // 2. Split the sheet into 96x96 pixel chunks (672 / 7 = 96)
            TextureRegion[][] tmp = TextureRegion.split(sheet, 96, 96);

            // 3. Extract exactly 7 frames
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
            System.out.println("==== NEIGHBOR SPRITE LOADING FAILED ====");
            e.printStackTrace();
            this.texture = colorTex(140, 135, 120); // Safety fallback
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

        // Anchor the drawing to the floor (Tweaked downward by 10 pixels to stop floating!)
        float drawY = y - 10f;

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
        if (glitched && stage >= 1) gs.foundTruth();
    }

    @Override
    public void onInteract(Player p, GameState gs) {
        speak(gs.isGlitched, gs);
        stage = Math.min(stage+1, Math.max(NORMAL.length, GLITCH.length)-1);
    }
}