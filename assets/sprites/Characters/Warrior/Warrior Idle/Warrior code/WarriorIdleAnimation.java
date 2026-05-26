import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * WarriorIdleAnimation.java
 *
 * Animates the Warrior_Idle_1 sprite atlas.
 * Frame layout is read from Warrior_Idle_1.json — a 2-column grid:
 *
 *   Col 0      Col 1
 *   [Frame 0][Frame 1]  row 0  (y=0)
 *   │ Frm 0  │ Frm 1  │
 *   [Frame 2][Frame 3]  row 1  (y=1056)
 *   │ Frm 2  │ Frm 3  │
*   [Frame 4][Frame 5]  row 2  (y=2112)
 *   │ Frm 4  │ Frm 5  │
 *   +---------+---------+
 *
 * HOW TO RUN:
 *   1. Place this file in the same folder as:
 *         Warrior_Idle_1.png   (the sprite atlas — 3072x3168 px)
 *   2. Compile:  javac WarriorIdleAnimation.java
 *   3. Run:      java WarriorIdleAnimation
 *
 * CONTROLS:
 *   SPACE     → Pause / resume
 *   + / =     → Speed up  (max 30 FPS)
 *   -         → Slow down (min  2 FPS)
 *   Close     → Exit
 */
public class WarriorIdleAnimation extends JPanel implements Runnable {

    // ── Atlas config (matches Warrior_Idle_1.json exactly) ───────────────────
    private static final String ATLAS_PATH   = "Warrior_Idle_1.png";
    private static final int    FRAME_COUNT  = 6;
    private static final int    FRAME_W      = 1536;
    private static final int    FRAME_H      = 1056;

    // Frame positions derived from the JSON  (x, y)
    private static final int[][] FRAME_COORDS = {
        {    0,    0 },   // Warrior_Idle_10  frame 0
        { 1536,    0 },   // Warrior_Idle_11  frame 1
        {    0, 1056 },   // Warrior_Idle_12  frame 2
        { 1536, 1056 },   // Warrior_Idle_13  frame 3
        {    0, 2112 },   // Warrior_Idle_14  frame 4
        { 1536, 2112 },   // Warrior_Idle_15  frame 5
    };

    // ── Display ───────────────────────────────────────────────────────────────
    private static final double SCALE  = 0.25;                        // 1536*0.25 = 384
    private static final int    DISP_W = (int)(FRAME_W * SCALE);     // 384
    private static final int    DISP_H = (int)(FRAME_H * SCALE);     // 264
    private static final int    WIN_W  = DISP_W + 60;
    private static final int    WIN_H  = DISP_H + 60;

    // ── State ─────────────────────────────────────────────────────────────────
    private BufferedImage[]  frames;
    private int              currentFrame = 0;
    private volatile boolean running      = false;
    private volatile boolean paused       = false;
    private int              fps          = 8;

    // ─────────────────────────────────────────────────────────────────────────

    public WarriorIdleAnimation() {
        setPreferredSize(new Dimension(WIN_W, WIN_H));
        setBackground(new Color(18, 18, 28));
        setFocusable(true);
        loadAndSlice();
        setupKeys();
    }

    // ── Load atlas and cut frames ─────────────────────────────────────────────
    private void loadAndSlice() {
        BufferedImage atlas;
        try {
            File f = new File(ATLAS_PATH);
            if (!f.exists()) {
                System.err.println("ERROR: '" + ATLAS_PATH + "' not found in the current directory.");
                System.err.println("       Place Warrior_Idle_1.png next to this .java file and rerun.");
                System.exit(1);
            }
            atlas = ImageIO.read(f);
            System.out.printf("Loaded atlas: %dx%d%n", atlas.getWidth(), atlas.getHeight());
        } catch (IOException e) {
            System.err.println("ERROR reading atlas: " + e.getMessage());
            System.exit(1);
            return;
        }

        frames = new BufferedImage[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            int x = FRAME_COORDS[i][0];
            int y = FRAME_COORDS[i][1];
            frames[i] = atlas.getSubimage(x, y, FRAME_W, FRAME_H);
            System.out.printf("  Frame %d: x=%-5d y=%d%n", i, x, y);
        }
        System.out.println("Ready — " + FRAME_COUNT + " frames sliced.");
    }

    // ── Key bindings ──────────────────────────────────────────────────────────
    private void setupKeys() {
        InputMap  im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("SPACE"),    "pause");
        am.put("pause", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                paused = !paused;
                repaint();
            }
        });

        im.put(KeyStroke.getKeyStroke("EQUALS"), "faster");
        im.put(KeyStroke.getKeyStroke("ADD"),    "faster");
        am.put("faster", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fps = Math.min(fps + 2, 30); repaint();
            }
        });

        im.put(KeyStroke.getKeyStroke("MINUS"),    "slower");
        im.put(KeyStroke.getKeyStroke("SUBTRACT"), "slower");
        am.put("slower", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fps = Math.max(fps - 2, 2); repaint();
            }
        });
    }

    // ── Painting ──────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Nearest-neighbour keeps pixel art crisp when scaled
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);

        // Draw current frame centred
        if (frames != null) {
            int x = (getWidth()  - DISP_W) / 2;
            int y = (getHeight() - DISP_H) / 2 - 10;
            g2.drawImage(frames[currentFrame], x, y, DISP_W, DISP_H, null);
        }

        // HUD
        String hud = String.format("Frame %d/%d  |  %d FPS  |  %s  |  [SPACE] pause  [+/-] speed",
                currentFrame + 1, FRAME_COUNT, fps, paused ? "PAUSED" : "PLAYING");
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(160, 160, 190));
        g2.drawString(hud, 8, getHeight() - 8);
    }

    // ── Animation loop ────────────────────────────────────────────────────────
    public void start() {
        running = true;
        Thread t = new Thread(this, "warrior-idle-anim");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        while (running) {
            if (!paused) {
                currentFrame = (currentFrame + 1) % FRAME_COUNT;
                repaint();
            }
            try {
                Thread.sleep(1000 / fps);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WarriorIdleAnimation anim = new WarriorIdleAnimation();
            anim.start();

            JFrame frame = new JFrame("Warrior Idle Animation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(anim);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
