package com.staticgame.systems;

import com.badlogic.gdx.math.Rectangle;
import com.staticgame.entities.Player;
import com.staticgame.world.Tile;
import com.staticgame.world.World;

public class CollisionSystem {

    private static final float GRAVITY  = -900f;
    private static final float MAX_FALL = -560f;

    public void step(Player player, World world, float delta) {
        // Gravity
        if (!player.isGrounded()) {
            float vy = player.getVelocityY() + GRAVITY * delta;
            player.setVelocityY(Math.max(vy, MAX_FALL));
        }

        // Move X → resolve X
        player.setPosition(player.getX() + player.getVelocityX()*delta, player.getY());
        for (Tile t : world.getSolidTilesIn(player.getBounds())) resolveX(player, t);

        // Move Y → resolve Y
        player.setPosition(player.getX(), player.getY() + player.getVelocityY()*delta);
        player.setGrounded(false);
        for (Tile t : world.getSolidTilesIn(player.getBounds())) resolveY(player, t);

        // World bounds
        float minX = Tile.SIZE;
        float maxX = world.getWidthPixels() - Tile.SIZE - player.getWidth();
        if (player.getX() < minX) { player.setPosition(minX, player.getY()); }
        if (player.getX() > maxX) { player.setPosition(maxX, player.getY()); }
        if (player.getY() < 0)    { player.setPosition(player.getX(), 0); player.setGrounded(true); }
    }

    private void resolveX(Player player, Tile tile) {
        Rectangle pb=player.getBounds(), tb=tile.getBounds();
        if (!pb.overlaps(tb)) return;
        float oR=(pb.x+pb.width)-tb.x, oL=(tb.x+tb.width)-pb.x;
        if (oR < oL) player.setPosition(player.getX()-oR, player.getY());
        else         player.setPosition(player.getX()+oL, player.getY());
    }

    private void resolveY(Player player, Tile tile) {
        Rectangle pb=player.getBounds(), tb=tile.getBounds();
        if (!pb.overlaps(tb)) return;
        float oTop=(pb.y+pb.height)-tb.y, oBot=(tb.y+tb.height)-pb.y;
        if (oBot < oTop) {
            if (tile.type==Tile.Type.PLATFORM && player.getVelocityY()>0) return;
            player.setPosition(player.getX(), player.getY()+oBot);
            player.setGrounded(true); player.setVelocityY(0);
        } else {
            if (tile.type==Tile.Type.PLATFORM) return;
            player.setPosition(player.getX(), player.getY()-oTop);
            player.setVelocityY(0);
        }
    }
}
