package com.staticgame.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.staticgame.entities.npcs.NPC;

import java.util.List;

/**
 * Draws a small floating indicator above every NPC the player hasn't spoken to.
 *
 * Renders in WORLD SPACE (uses the world camera's combined matrix) so the
 * indicators scroll with the level rather than being pinned to the screen.
 *
 * Visual behaviour:
 *  - Unseen NPC     → pulsing "!" diamond icon, white/yellow
 *  - Glitch state   → same icon but cyan, flickers
 *  - Already spoken → no indicator (don't clutter the world after interaction)
 *
 * The SpriteBatch passed to render() must already use the world camera
 * projection matrix.
 */
public class NPCIndicator {

    private Texture  iconTex;        // small square, drawn as a rotated diamond
    private Texture  pixelTex;
    private BitmapFont labelFont;

    private float time = 0f;

    public NPCIndicator() {
        // 8×8 icon texture — bright centre
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 0.6f, 1f);
        // Draw a filled diamond
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int dx = Math.abs(x - 3);
                int dy = Math.abs(y - 3);
                if (dx + dy <= 3) pm.drawPixel(x, y);
            }
        }
        iconTex = new Texture(pm);
        pm.dispose();

        pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1); pm.fill();
        pixelTex = new Texture(pm);
        pm.dispose();

        labelFont = new BitmapFont();
        labelFont.getData().setScale(0.7f);
    }

    /**
     * Update animation timers. Call once per frame.
     */
    public void update(float delta) {
        time += delta;
    }

    /**
     * Draw indicators above all un-met NPCs.
     *
     * @param batch      active world-space SpriteBatch
     * @param npcs       full list of NPCs in the level
     * @param glitched   current world state
     * @param camera     world camera (used to convert world→screen if needed)
     */
    public void render(SpriteBatch batch, List<NPC> npcs, boolean glitched,
                       OrthographicCamera camera) {
        for (NPC npc : npcs) {
            if (!npc.isVisible()) continue;

            float bob     = MathUtils.sin(time * 3f) * 4f;
            float pulse   = (MathUtils.sin(time * 5f) + 1f) * 0.5f;

            float iconX   = npc.getX() + npc.getWidth()  / 2f - 4f;
            float iconY   = npc.getY() + npc.getHeight() + 8f + bob;
            float iconW   = 8f;
            float iconH   = 8f;

            if (glitched) {
                // Cyan flicker
                if (MathUtils.sin(time * 12f) > 0.3f) {
                    batch.setColor(0.2f, 1f, 0.7f, 0.7f + 0.3f * pulse);
                } else {
                    batch.setColor(1f, 0.2f, 0.9f, 0.5f);
                }
            } else {
                batch.setColor(1f, 0.95f, 0.4f, 0.65f + 0.35f * pulse);
            }

            // Rotate 45° by drawing four triangles manually using pixel draws
            // (LibGDX SpriteBatch.draw can rotate)
            batch.draw(iconTex, iconX, iconY, 4f, 4f, iconW, iconH,
                       1f, 1f, 45f,
                       0, 0, 8, 8, false, false);

            // "!" label below the diamond
            labelFont.setColor(glitched ? new Color(0.3f, 1f, 0.8f, 0.9f)
                                        : new Color(1f, 1f, 0.6f, 0.9f));
            labelFont.draw(batch, "!", iconX + 2f, iconY - 2f);

            // Glow ring (thin pixel border a bit larger)
            float ringAlpha = 0.15f + 0.1f * pulse;
            batch.setColor(glitched ? 0.2f : 1f,
                           glitched ? 1f   : 0.95f,
                           glitched ? 0.7f : 0.4f,
                           ringAlpha);
            batch.draw(pixelTex, iconX - 3, iconY - 3, iconW + 6, iconH + 6);
        }

        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        iconTex.dispose();
        pixelTex.dispose();
        labelFont.dispose();
    }
}
