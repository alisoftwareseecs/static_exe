import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Warrior Crouch Animation
 *
 * Sprite sheet layout:
 *   - Total size:  3312 x 4224 px
 *   - Columns:     2
 *   - Rows:        4  (last row has only 1 frame)
 *   - Total frames: 7
 *   - Frame size:  1656 x 1056 px
 *
 * Usage:
 *   javac WarriorCrouchAnimation.java
 *   java WarriorCrouchAnimation
 *
 * Place "Warrior_Crouch_1__1_.png" in the same directory as the .class files,
 * or pass the path as the first argument:
 *   java WarriorCrouchAnimation /path/to/Warrior_Crouch_1__1_.png
 */
public class WarriorCrouchAnimation extends JPanel implements ActionListener {

    // ── Sprite-sheet constants ────────────────────────────────────────────────
    private static final int SHEET_COLS   = 2;
    private static final int SHEET_ROWS   = 4;
    private static final int TOTAL_FRAMES = 7;          // last cell is empty
    private static final int FRAME_W      = 3312 / SHEET_COLS;  // 1656
    private static final int FRAME_H      = 4224 / SHEET_ROWS;  // 1056

    // ── Display constants ─────────────────────────────────────────────────────
    private static final int DISPLAY_W      = 200;        // window width
    private static final int DISPLAY_H      = 250;        // window height
    private static final int FPS            = 8;          // frames per second
    private static final int FRAME_DELAY_MS = 1000 / FPS;

    // ── State ─────────────────────────────────────────────────────────────────
    private final BufferedImage[] frames = new BufferedImage[TOTAL_FRAMES];
    private int   currentFrame = 0;
    private final Timer timer;
    private boolean loaded = false;

    // ── Constructor ───────────────────────────────────────────────────────────
    public WarriorCrouchAnimation(String imagePath) {
        setPreferredSize(new Dimension(DISPLAY_W, DISPLAY_H));
        setBackground(Color.BLACK);

        loadFrames(imagePath);

        timer = new Timer(FRAME_DELAY_MS, this);
        timer.start();
    }

    // ── Frame extraction ──────────────────────────────────────────────────────
    private void loadFrames(String imagePath) {
        BufferedImage sheet;
        try {
            sheet = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            System.err.println("ERROR: Could not load sprite sheet from: " + imagePath);
            System.err.println("       " + e.getMessage());
            return;
        }

        int extracted = 0;
        outer:
        for (int row = 0; row < SHEET_ROWS; row++) {
            for (int col = 0; col < SHEET_COLS; col++) {
                if (extracted >= TOTAL_FRAMES) break outer;

                int x = col * FRAME_W;
                int y = row * FRAME_H;

                // Guard against sheet edges
                int w = Math.min(FRAME_W, sheet.getWidth()  - x);
                int h = Math.min(FRAME_H, sheet.getHeight() - y);

                if (w <= 0 || h <= 0) break outer;

                frames[extracted] = sheet.getSubimage(x, y, w, h);
                extracted++;
            }
        }

        if (extracted == TOTAL_FRAMES) {
            loaded = true;
            System.out.println("Sprite sheet loaded: " + TOTAL_FRAMES + " frames extracted.");
        } else {
            System.err.println("WARNING: Expected " + TOTAL_FRAMES + " frames but only extracted " + extracted);
        }
    }

    // ── Timer tick ────────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
        repaint();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!loaded) {
            g.setColor(Color.RED);
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            g.drawString("Failed to load sprite sheet.", 10, getHeight() / 2);
            return;
        }

        BufferedImage frame = frames[currentFrame];
        if (frame == null) return;

        // Scale to fit the panel while preserving aspect ratio
        int panelW = getWidth();
        int panelH = getHeight();

        double scaleX = (double) panelW / frame.getWidth();
        double scaleY = (double) panelH / frame.getHeight();
        double scale  = Math.min(scaleX, scaleY);

        int drawW = (int) (frame.getWidth()  * scale);
        int drawH = (int) (frame.getHeight() * scale);
        int drawX = (panelW - drawW) / 2;
        int drawY = (panelH - drawH) / 2;

        Graphics2D g2 = (Graphics2D) g;
        // Nearest-neighbor scaling keeps pixel art crisp
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        g2.drawImage(frame, drawX, drawY, drawW, drawH, null);

        // Optional: frame counter overlay
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString("Frame: " + (currentFrame + 1) + "/" + TOTAL_FRAMES, 6, 14);
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Default: look in the current directory
        String imagePath = "Warrior_Crouch_1.png";
        if (args.length > 0) {
            imagePath = args[0];
        }

        final String path = imagePath;

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Warrior Crouch Animation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);

            WarriorCrouchAnimation anim = new WarriorCrouchAnimation(path);
            frame.add(anim);
            frame.pack();
            frame.setLocationRelativeTo(null);   // center on screen
            frame.setVisible(true);
        });
    }
}
