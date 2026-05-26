import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Warrior Run Animation
 *
 * Sprite sheet layout:
 *   - Total size:  3072 x 4224 px
 *   - Columns:     2
 *   - Rows:        4
 *   - Total frames: 8
 *   - Frame size:  1536 x 1056 px
 *
 * HOW TO RUN:
 *   1. Place "Warrior_Run_1__1_.png" in the SAME folder as this file
 *   2. javac WarriorRunAnimation.java
 *   3. java WarriorRunAnimation
 *
 *   Or pass a custom path:
 *   java WarriorRunAnimation "C:\path\to\Warrior_Run_1__1_.png"
 */
public class WarriorRunAnimation extends JPanel implements ActionListener {

    // ── Sprite-sheet constants ────────────────────────────────────────────────
    private static final int SHEET_COLS   = 2;
    private static final int SHEET_ROWS   = 4;
    private static final int TOTAL_FRAMES = 8;
    private static final int FRAME_W      = 3072 / SHEET_COLS;  // 1536
    private static final int FRAME_H      = 4224 / SHEET_ROWS;  // 1056

    // ── Display / animation constants ─────────────────────────────────────────
    private static final int DISPLAY_W      = 300;
    private static final int DISPLAY_H      = 300;
    private static final int FPS            = 10;
    private static final int FRAME_DELAY_MS = 1000 / FPS;

    // ── State ─────────────────────────────────────────────────────────────────
    private final BufferedImage[] frames = new BufferedImage[TOTAL_FRAMES];
    private int     currentFrame = 0;
    private boolean loaded       = false;
    private final Timer timer;

    // ── Constructor ───────────────────────────────────────────────────────────
    public WarriorRunAnimation(String imagePath) {
        setPreferredSize(new Dimension(DISPLAY_W, DISPLAY_H));
        setBackground(Color.BLACK);
        loadFrames(imagePath);
        timer = new Timer(FRAME_DELAY_MS, this);
        timer.start();
    }

    // ── Load & slice sprite sheet ─────────────────────────────────────────────
    private void loadFrames(String imagePath) {
        File file = new File(imagePath);

        if (!file.exists()) {
            System.err.println("ERROR: File not found: " + file.getAbsolutePath());
            return;
        }

        BufferedImage sheet;
        try {
            sheet = ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("ERROR: Could not read image: " + e.getMessage());
            return;
        }

        if (sheet == null) {
            System.err.println("ERROR: ImageIO returned null (unsupported format?)");
            return;
        }

        int extracted = 0;
        outer:
        for (int row = 0; row < SHEET_ROWS; row++) {
            for (int col = 0; col < SHEET_COLS; col++) {
                if (extracted >= TOTAL_FRAMES) break outer;

                int x = col * FRAME_W;
                int y = row * FRAME_H;
                int w = Math.min(FRAME_W, sheet.getWidth()  - x);
                int h = Math.min(FRAME_H, sheet.getHeight() - y);

                if (w <= 0 || h <= 0) break outer;

                frames[extracted++] = sheet.getSubimage(x, y, w, h);
            }
        }

        if (extracted == TOTAL_FRAMES) {
            loaded = true;
            System.out.println("Loaded " + TOTAL_FRAMES + " frames from: " + file.getAbsolutePath());
        } else {
            System.err.println("WARNING: Expected " + TOTAL_FRAMES
                    + " frames but extracted " + extracted);
        }
    }

    // ── Timer tick ────────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
        repaint();
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!loaded) {
            g.setColor(Color.RED);
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String msg = "Sprite sheet not found!";
            g.drawString(msg,
                    (getWidth()  - fm.stringWidth(msg)) / 2,
                    (getHeight() + fm.getAscent())       / 2);
            return;
        }

        BufferedImage frame = frames[currentFrame];
        if (frame == null) return;

        // Scale to fit panel, preserving aspect ratio
        double scale  = Math.min((double) getWidth()  / frame.getWidth(),
                                 (double) getHeight() / frame.getHeight());
        int drawW = (int) (frame.getWidth()  * scale);
        int drawH = (int) (frame.getHeight() * scale);
        int drawX = (getWidth()  - drawW) / 2;
        int drawY = (getHeight() - drawH) / 2;

        Graphics2D g2 = (Graphics2D) g;
        // Nearest-neighbor keeps pixel art crisp
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_SPEED);

        g2.drawImage(frame, drawX, drawY, drawW, drawH, null);

        // Frame counter overlay
        g2.setColor(new Color(255, 255, 255, 160));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString("Frame " + (currentFrame + 1) + "/" + TOTAL_FRAMES, 6, 14);
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Default: look for the PNG in the same directory the program is run from
        String imagePath = args.length > 0 ? args[0] : "Warrior_Run_1 (1).png";

        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Warrior Run Animation");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(true);
            window.add(new WarriorRunAnimation(imagePath));
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
