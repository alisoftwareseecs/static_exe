package com.staticgame.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.staticgame.entities.Player;
import com.staticgame.utils.GameState;

/**
 * An interactive puzzle object (e.g., a suppressed memory or clue).
 * The player must overlap it in both world states (normal then glitch, or vice versa)
 * to piece the fragment together.
 * Solving it increments truthsFound, unlocking map doors and pushing toward the TRUTH ending.
 */
public class PuzzleElement {

    public enum State { UNSOLVED, PART_A_DONE, PART_B_DONE, SOLVED }

    private float x, y;
    private static final float SIZE = Tile.SIZE;

    private State   state     = State.UNSOLVED;
    private String  hintText;
    private float   animTimer = 0f;
    private float   solvedGlow = 0f;

    // 0=unsolved(dim), 1=partial(active), 2=solved(revealed)
    private Texture[] textures = new Texture[3];

    public PuzzleElement(float x, float y, String hintText) {
        this.x        = x;
        this.y        = y;
        this.hintText = hintText;
        textures[0]   = makeTex(200, 160, 40);
        textures[1]   = makeTex(255, 130, 20);
        textures[2]   = makeTex(80, 255, 160);
    }

    private Texture makeTex(int r, int g, int b) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(r / 255f, g / 255f, b / 255f, 1f);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    public void update(float delta, Player player, GameState gs) {
        animTimer += delta;
        if (state == State.SOLVED) {
            solvedGlow = Math.min(1f, solvedGlow + delta * 2f);
            return;
        }

        Rectangle pb = player.getBounds();
        Rectangle tb = new Rectangle(x, y, SIZE, SIZE);
        if (!pb.overlaps(tb)) return;

        boolean g = gs.isGlitched;

        if      (!g && state == State.UNSOLVED)    state = State.PART_A_DONE;
        else if (!g && state == State.PART_B_DONE) solve(gs);
        else if ( g && state == State.UNSOLVED)    state = State.PART_B_DONE;
        else if ( g && state == State.PART_A_DONE) solve(gs);
    }

    private void solve(GameState gs) {
        state = State.SOLVED;
        // Fix: Hook into the new narrative progression system
        gs.foundTruth();
    }

    public void render(SpriteBatch batch, boolean glitched) {
        float pulse = (MathUtils.sin(animTimer * 3f) + 1f) * 0.5f;
        switch (state) {
            case UNSOLVED:
                batch.setColor(1f, 1f, 1f, 0.7f + 0.3f * pulse);
                batch.draw(textures[0], x, y, SIZE, SIZE);
                break;
            case PART_A_DONE:
            case PART_B_DONE:
                if (glitched) batch.setColor(0.2f, 1f, 0.8f, 0.8f + 0.2f * pulse);
                else          batch.setColor(1f, 0.6f + 0.4f * pulse, 0.2f, 1f);
                batch.draw(textures[1], x, y, SIZE, SIZE);
                break;
            case SOLVED:
                float g = 0.6f + 0.4f * solvedGlow;
                batch.setColor(g, 1f, g, 1f);
                batch.draw(textures[2], x, y, SIZE, SIZE);
                float exp = solvedGlow * 8f;
                batch.setColor(0.4f, 1f, 0.6f, (1f - solvedGlow) * 0.4f);
                batch.draw(textures[2], x - exp, y - exp, SIZE + exp * 2f, SIZE + exp * 2f);
                break;
        }
        batch.setColor(Color.WHITE);
    }

    /** Returns hint text if player is within 100px, else null. */
    public String getHintIfNear(Player player) {
        if (state == State.SOLVED) return null;
        float dx = (player.getX() + player.getWidth() / 2f)  - (x + SIZE / 2f);
        float dy = (player.getY() + player.getHeight() / 2f) - (y + SIZE / 2f);
        return Math.sqrt(dx * dx + dy * dy) < 100f ? hintText : null;
    }

    public boolean isSolved() { return state == State.SOLVED; }
    public float getX()       { return x; }
    public float getY()       { return y; }

    public void dispose() {
        for (Texture t : textures) {
            if (t != null) t.dispose();
        }
    }
}