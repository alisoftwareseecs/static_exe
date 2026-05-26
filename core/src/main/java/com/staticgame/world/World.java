package com.staticgame.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.staticgame.entities.Player;
import com.staticgame.entities.npcs.*;

import java.util.*;

/**
 * Three maps, each 60 cols x 14 rows (2400 x 560 px).
 * Loaded by index. GameScreen swaps maps on exit.
 *
 * Map legend:
 * .  air         #  ground      P  platform (one-way)
 * ~  POND        B  BRIDGE      M  MEMORY_WALL
 * L  DOOR_LOCKED X  exit        f  furniture (deco)  w  window (deco)
 * S  spawn       E  Elena(mum)  H  Hamza   V  Voss(neighbour)
 * R  Dr.Marsh    C  EliasEcho
 */
public class World {

    // ── MAP 0: The Apartment Block ─────────────────────────────────────────────
    private static final String[] MAP0 = {
            "############################################################",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#...w............................................w.........#",
            "#................PP.....................PP.................#",
            "#.......PP....................PP...........................#",
            "#.......................PP........PP.......PP..............#",
            "#.S.E.....H..~~~..V........MM....................L.......X.#",
            "############################################################",
    };

    // ── MAP 1: The University ──────────────────────────────────────────────────
    private static final String[] MAP1 = {
            "############################################################",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#...w............................w.........................#",
            "#................PP........................................#",
            "#.......PP.................................................#",
            "#........................................PP................#",
            "#......................PP...........................PP.....#",
            "#.S.......C...R............BBBBB..MM............L........X.#",
            "###########################.....############################", // Gap for the bridge puzzle!
    };

    // ── MAP 2: The Hospital ────────────────────────────────────────────────────
    private static final String[] MAP2 = {
            "############################################################",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#..........................................................#",
            "#......PP...........PP............PP.......................#",
            "#..........................................................#",
            "#...........PP..........................PP.................#",
            "#........................PP................................#",
            "#.S...f...........f.............f........................X.#",
            "############################################################",
    };

    private static final String[][] MAPS = { MAP0, MAP1, MAP2 };
    private static final int COLS = 60;
    private static final int ROWS = 14;

    private Tile[][]  tiles;
    private List<NPC> npcs;
    private float     spawnX, spawnY;
    private boolean   glitched = false;
    private int       mapIndex;
    private int       doorCol=-1, doorRow=-1;

    public World(int mapIndex) {
        this.mapIndex = mapIndex;
        tiles = new Tile[COLS][ROWS];
        npcs  = new ArrayList<>();
        parse(MAPS[mapIndex]);
    }

    private void parse(String[] map) {
        for (int row=0; row<ROWS; row++) {
            int    src  = ROWS-1-row;
            String line = (src<map.length) ? map[src] : "";
            for (int col=0; col<COLS && col<line.length(); col++) {
                char  c  = line.charAt(col);
                float wx = col*Tile.SIZE, wy = row*Tile.SIZE;
                switch(c) {
                    case '#': tiles[col][row]=new Tile(Tile.Type.GROUND,         wx,wy); break;
                    case 'P': tiles[col][row]=new Tile(Tile.Type.PLATFORM,       wx,wy); break;
                    case 'W': tiles[col][row]=new Tile(Tile.Type.WALL,           wx,wy); break;
                    case '~': tiles[col][row]=new Tile(Tile.Type.POND,           wx,wy); break;
                    case 'B': tiles[col][row]=new Tile(Tile.Type.BRIDGE,         wx,wy); break;
                    case 'M': tiles[col][row]=new Tile(Tile.Type.MEMORY_WALL,    wx,wy); break;
                    case 'L': tiles[col][row]=new Tile(Tile.Type.DOOR_LOCKED,    wx,wy);
                        doorCol=col; doorRow=row; break;
                    case 'X': tiles[col][row]=new Tile(Tile.Type.EXIT,           wx,wy); break;
                    case 'f': tiles[col][row]=new Tile(Tile.Type.DECO_FURNITURE, wx,wy); break;
                    case 'w': tiles[col][row]=new Tile(Tile.Type.DECO_WINDOW,    wx,wy); break;
                    case 'S': spawnX=wx; spawnY=wy+4; break;
                    case 'E': npcs.add(new MotherNPC(wx,wy+4)); break;
                    case 'H': npcs.add(new HamzaNPC(wx,wy+4)); break;
                    case 'V': npcs.add(new NeighborNPC(wx,wy+4)); break;
                    case 'R': npcs.add(new ProfessorNPC(wx,wy+4)); break;
                    case 'C': npcs.add(new EliasEchoNPC(wx,wy+4)); break;
                    default: break;
                }
                // NPC/spawn cells are air
                if ("SHEVRC".indexOf(c)>=0) tiles[col][row]=null;
            }
        }
    }

    /**
     * Unlock the locked door when enough truths are found.
     * Map 0 needs 2, Map 1 needs 4.
     */
    public void checkDoorUnlock(int truths) {
        int need = (mapIndex==0) ? 2 : 4;
        if (doorCol<0||doorRow<0) return;
        Tile d = tiles[doorCol][doorRow];
        if (d!=null && d.isDoorLocked() && truths>=need) {
            d.dispose();
            tiles[doorCol][doorRow]=new Tile(Tile.Type.DOOR_OPEN, doorCol*Tile.SIZE, doorRow*Tile.SIZE);
        }
    }

    public void update(float delta) {
        for (NPC n : npcs) { n.setGlitched(glitched); n.update(delta); }
    }

    public void render(SpriteBatch batch, float delta) {
        for (int c=0;c<COLS;c++)
            for (int r=0;r<ROWS;r++)
                if (tiles[c][r]!=null) tiles[c][r].render(batch,glitched,delta);
        for (NPC n : npcs) n.render(batch);
    }

    public void shiftState(boolean g) {
        glitched=g;
        for (NPC n : npcs) n.setGlitched(g);
    }

    public List<Tile> getSolidTilesIn(Rectangle area) {
        List<Tile> out=new ArrayList<>();
        int c0=Math.max(0,(int)(area.x/Tile.SIZE)-1);
        int c1=Math.min(COLS-1,(int)((area.x+area.width)/Tile.SIZE)+1);
        int r0=Math.max(0,(int)(area.y/Tile.SIZE)-1);
        int r1=Math.min(ROWS-1,(int)((area.y+area.height)/Tile.SIZE)+1);
        for(int c=c0;c<=c1;c++) for(int r=r0;r<=r1;r++){
            Tile t=tiles[c][r];
            if(t!=null&&t.isSolid(glitched)) out.add(t);
        }
        return out;
    }

    public NPC getNPCNear(Player p) {
        float range=p.getInteractRange();
        float px=p.getX()+p.getWidth()/2f, py=p.getY()+p.getHeight()/2f;
        NPC best=null; float bd=Float.MAX_VALUE;
        for(NPC n:npcs){
            if(!n.isVisible()) continue;
            float dx=(n.getX()+n.getWidth()/2f)-px;
            float dy=(n.getY()+n.getHeight()/2f)-py;
            float d=(float)Math.sqrt(dx*dx+dy*dy);
            if(d<=range&&d<bd){bd=d;best=n;}
        }
        return best;
    }

    public boolean playerOnExit(Player p) {
        Rectangle pb=p.getBounds();
        for(int c=0;c<COLS;c++) for(int r=0;r<ROWS;r++){
            Tile t=tiles[c][r];
            if(t!=null&&t.isExit()&&t.getBounds().overlaps(pb)) return true;
        }
        return false;
    }

    public List<NPC> getNpcs()    { return npcs; }
    public float getSpawnX()      { return spawnX; }
    public float getSpawnY()      { return spawnY; }
    public boolean isGlitched()   { return glitched; }
    public int getWidthPixels()   { return COLS*Tile.SIZE; }
    public int getHeightPixels()  { return ROWS*Tile.SIZE; }
    public int getMapIndex()      { return mapIndex; }

    public void dispose() {
        for(int c=0;c<COLS;c++) for(int r=0;r<ROWS;r++) if(tiles[c][r]!=null) tiles[c][r].dispose();
        for(NPC n:npcs) n.dispose();
    }
}