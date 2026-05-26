import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * JoannaAnimation.java
 *
 * Animates the Joanna sprite sheet (7 frames, each 1536x1536px).
 *
 * HOW TO RUN:
 *   1. Place this file in the same folder as your sprite sheet image (Joanna.png)
 *      - The sprite sheet is the large horizontal image containing all 7 frames side by side.
 *      - You can export it from Joanna.json (the base64 image inside the "meta" > "image" field).
 *   2. Compile:  javac JoannaAnimation.java
 *   3. Run:      java JoannaAnimation
 *
 * CONTROLS:
 *   - The animation loops automatically.
 *   - Close the window to exit.
 */
public class JoannaAnimation extends JPanel implements Runnable {

    // Sprite sheet configuration (matches Joanna.json)
    private static final int FRAME_COUNT   = 7;
    private static final int FRAME_WIDTH   = 1536;
    private static final int FRAME_HEIGHT  = 1536;

    // Display scale — 1536px is huge; scale down to fit nicely on screen
    private static final double SCALE      = 0.25;
    private static final int DISPLAY_SIZE  = (int)(FRAME_WIDTH * SCALE); // 384px

    // Animation speed
    private static final int FPS           = 8; // frames per second
    private static final int FRAME_DELAY   = 1000 / FPS;

    private BufferedImage   spriteSheet;
    private BufferedImage[] frames;
    private int             currentFrame   = 0;
    private volatile boolean running       = false;

    // -------------------------------------------------------------------------

    public JoannaAnimation() {
        setPreferredSize(new Dimension(DISPLAY_SIZE, DISPLAY_SIZE));
        setBackground(new Color(30, 30, 40));

        loadSpriteSheet();
        sliceFrames();
    }

    /** Load the sprite sheet PNG from disk. */
    private void loadSpriteSheet() {
        try {
            File file = new File("Joanna.png");
            if (!file.exists()) {
                System.err.println("ERROR: Could not find 'Joanna.png' in the current directory.");
                System.err.println("       Export the sprite sheet image from Joanna.json and save it as Joanna.png.");
                System.exit(1);
            }
            spriteSheet = ImageIO.read(file);
            System.out.println("Loaded sprite sheet: " + spriteSheet.getWidth() + "x" + spriteSheet.getHeight());
        } catch (IOException e) {
            System.err.println("ERROR reading sprite sheet: " + e.getMessage());
            System.exit(1);
        }
    }

    /** Cut the sprite sheet into individual frames. */
    private void sliceFrames() {
        frames = new BufferedImage[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            // Each frame sits at x = i * FRAME_WIDTH, y = 0
            frames[i] = spriteSheet.getSubimage(
                i * FRAME_WIDTH,
                0,
                FRAME_WIDTH,
                FRAME_HEIGHT
            );
        }
        System.out.println("Sliced " + FRAME_COUNT + " frames.");
    }

    // -------------------------------------------------------------------------
    // Painting

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (frames == null || frames[currentFrame] == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Center the sprite in the panel
        int x = (getWidth()  - DISPLAY_SIZE) / 2;
        int y = (getHeight() - DISPLAY_SIZE) / 2;

        g2.drawImage(frames[currentFrame], x, y, DISPLAY_SIZE, DISPLAY_SIZE, null);

        // Debug: frame counter
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString("Frame: " + (currentFrame + 1) + " / " + FRAME_COUNT, 8, 16);
    }

    // -------------------------------------------------------------------------
    // Animation loop

    public void start() {
        running = true;
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        while (running) {
            currentFrame = (currentFrame + 1) % FRAME_COUNT;
            repaint();
            try {
                Thread.sleep(FRAME_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Entry point

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JoannaAnimation anim = new JoannaAnimation();
            anim.start();

            JFrame frame = new JFrame("Joanna Animation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(anim);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
