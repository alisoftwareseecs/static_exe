package com.staticgame.entities.npcs;

import com.staticgame.entities.Player;
import com.staticgame.utils.GameState;

/**
 * ECHO — a version of Elias from before the disappearance.
 * Only visible in glitch state. Exists only on Map 2 (University).
 *
 * He doesn't know Nadia is gone yet. He's still the old Elias.
 * Talking to him forces the player to confront what they've lost —
 * not just Nadia, but who Elias was before this.
 *
 * Interacting with him enough triggers the accusedSelf flag
 * (Elias spirals into guilt, blaming himself for not noticing sooner).
 * That flag pushes toward the GUILT ending.
 */
public class EliasEchoNPC extends NPC {

    private static final String[] NORMAL = {
        "...",  // invisible in normal world
    };

    private static final String[] GLITCH = {
        "Echo: Hey. You look terrible.\nAre you sleeping?",
        "Echo: Nadia texted me. Did you see? She wants to\nmeet for coffee Saturday. You going?",
        "Echo: Something funny happened in lecture today.\nWait — why are you looking at me like that?",
        "Echo: ...You know, don't you.\nHow long have I got before I know?",
        "Echo: I miss not knowing.\nIs that terrible? I miss just being okay.",
    };

    public EliasEchoNPC(float x, float y) {
        super(4, "Echo (??)", x, y, 26, 46, NORMAL, GLITCH);
        texture = colorTex(160, 165, 200);
    }

    @Override
    public void update(float delta) {
        visible = isGlitched;
        super.update(delta);
    }

    @Override
    public void speak(boolean glitched, GameState gs) {
        if (!glitched) return;
        show(pick(GLITCH, stage));
        // Stage 3+ triggers self-blame
        if (stage >= 3 && !gs.accusedSelf) {
            gs.accusedSelf = true;
        }
        if (stage >= 4) gs.foundTruth();
    }

    @Override
    public void onInteract(Player p, GameState gs) {
        if (!gs.isGlitched) { show("There's no one here."); return; }
        speak(true, gs);
        stage = Math.min(stage+1, GLITCH.length-1);
    }
}
