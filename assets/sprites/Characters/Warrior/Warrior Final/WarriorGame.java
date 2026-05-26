import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class WarriorGame extends JPanel implements ActionListener, KeyListener {

    // -----------------------------------------------------------------------
    // Enums
    // -----------------------------------------------------------------------
    enum State { IDLE, RUN, JUMP, CROUCH }

    // -----------------------------------------------------------------------
    // Display size - character drawn at exactly this size, preserving ratio
    // -----------------------------------------------------------------------
    private static final int CHAR_W = 96;
    private static final int CHAR_H = 96;

    // -----------------------------------------------------------------------
    // Window
    // -----------------------------------------------------------------------
    private static final int WIN_W  = 800;
    private static final int WIN_H  = 450;
    private static final int GROUND = 360; // Y of feet when standing

    // -----------------------------------------------------------------------
    // Physics
    // -----------------------------------------------------------------------
    private static final float GRAVITY    = 0.55f;
    private static final float JUMP_FORCE = -13.5f;
    private static final float RUN_SPEED  = 4.0f;

    // -----------------------------------------------------------------------
    // Animation timing (ms per frame)
    // -----------------------------------------------------------------------
    private static final int IDLE_MS   = 120;
    private static final int RUN_MS    = 80;
    private static final int JUMP_MS   = 110;
    private static final int CROUCH_MS = 110;

    // -----------------------------------------------------------------------
    // Sprite storage
    // -----------------------------------------------------------------------
    private BufferedImage[] idleFrames;
    private BufferedImage[] runFrames;
    private BufferedImage[] jumpFrames;
    private BufferedImage[] crouchFrames;

    // -----------------------------------------------------------------------
    // Character state
    // -----------------------------------------------------------------------
    private State   state       = State.IDLE;
    private float   x           = 100;
    private float   y           = GROUND;
    private float   velY        = 0;
    private boolean onGround    = true;
    private boolean facingRight = true;

    // Input flags
    private boolean keyLeft   = false;
    private boolean keyRight  = false;
    private boolean keyUp     = false;
    private boolean keyDown   = false;

    // Animation counters
    private int curFrame  = 0;
    private int animTick  = 0;

    // Game timer (~60 FPS)
    private Timer gameTimer;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public WarriorGame() {
        setPreferredSize(new Dimension(WIN_W, WIN_H));
        setBackground(new Color(20, 20, 35));
        setFocusable(true);
        addKeyListener(this);

        loadAllSprites();

        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    // -----------------------------------------------------------------------
    // Sprite loading helpers
    // -----------------------------------------------------------------------

    // Load a HORIZONTAL STRIP (all frames in one row)
    private BufferedImage[] loadStrip(String path, int frameCount) {
        BufferedImage sheet = loadImage(path);
        int fw = sheet.getWidth() / frameCount;
        int fh = sheet.getHeight();
        BufferedImage[] out = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            out[i] = scaleFrame(sheet.getSubimage(i * fw, 0, fw, fh));
        }
        return out;
    }

    // Load a GRID layout (frames arranged in columns x rows)
    private BufferedImage[] loadGrid(String path, int cols, int rows, int frameCount) {
        BufferedImage sheet = loadImage(path);
        int fw = sheet.getWidth()  / cols;
        int fh = sheet.getHeight() / rows;
        BufferedImage[] out = new BufferedImage[frameCount];
        int idx = 0;
        outer:
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (idx >= frameCount) break outer;
                out[idx++] = scaleFrame(sheet.getSubimage(col * fw, row * fh, fw, fh));
            }
        }
        return out;
    }

    // Scale a raw frame to CHAR_W x CHAR_H preserving original pixel art
    private BufferedImage scaleFrame(BufferedImage src) {
        BufferedImage dst = new BufferedImage(CHAR_W, CHAR_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, CHAR_W, CHAR_H, null);
        g.dispose();
        return dst;
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("ERROR loading: " + path);
            System.exit(1);
            return null;
        }
    }

    private void loadAllSprites() {
        // Idle  - horizontal strip, 6 frames
        idleFrames   = loadStrip("Warrior_Idle_1 (1).png", 6);

        // Jump  - 2 cols x 3 rows, 6 frames
        jumpFrames   = loadGrid("Warrior_Idle_1 (2).png", 2, 3, 6);

        // Crouch - 2 cols x 4 rows, 7 frames
        crouchFrames = loadGrid("Warrior_Crouch_1 (1).png", 2, 4, 7);

        // Run   - 2 cols x 4 rows, 8 frames
        runFrames    = loadGrid("Warrior_Run_1 (1).png", 2, 4, 8);

        System.out.println("All sprites loaded OK");
        System.out.println("Idle frames:   " + idleFrames.length);
        System.out.println("Jump frames:   " + jumpFrames.length);
        System.out.println("Crouch frames: " + crouchFrames.length);
        System.out.println("Run frames:    " + runFrames.length);
    }

    // -----------------------------------------------------------------------
    // Game loop
    // -----------------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        updateState();
        updatePhysics();
        updateAnimation();
        repaint();
    }

    private void updateState() {
        if (!onGround) {
            state = State.JUMP;
        } else if (keyDown) {
            state = State.CROUCH;
        } else if (keyLeft || keyRight) {
            state = State.RUN;
        } else {
            state = State.IDLE;
        }

        // Flip direction
        if (keyRight) facingRight = true;
        if (keyLeft)  facingRight = false;
    }

    private void updatePhysics() {
        // Horizontal movement (only when not crouching)
        if (!keyDown) {
            if (keyRight) x += RUN_SPEED;
            if (keyLeft)  x -= RUN_SPEED;
        }

        // Jump trigger
        if (keyUp && onGround) {
            velY     = JUMP_FORCE;
            onGround = false;
        }

        // Gravity
        if (!onGround) {
            velY += GRAVITY;
            y    += velY;
            if (y >= GROUND) {
                y        = GROUND;
                velY     = 0;
                onGround = true;
            }
        }

        // Keep inside window
        if (x < 0)            x = 0;
        if (x > WIN_W - CHAR_W) x = WIN_W - CHAR_W;
    }

    private void updateAnimation() {
        int delayMs = switch (state) {
            case IDLE   -> IDLE_MS;
            case RUN    -> RUN_MS;
            case JUMP   -> JUMP_MS;
            case CROUCH -> CROUCH_MS;
        };

        animTick++;
        if (animTick >= delayMs / 16) {
            animTick = 0;
            int len = currentFrames().length;
            curFrame = (curFrame + 1) % len;
        }
    }

    private BufferedImage[] currentFrames() {
        return switch (state) {
            case IDLE   -> idleFrames;
            case RUN    -> runFrames;
            case JUMP   -> jumpFrames;
            case CROUCH -> crouchFrames;
        };
    }

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Ground
        g2.setColor(new Color(55, 55, 75));
        g2.fillRect(0, GROUND, WIN_W, WIN_H - GROUND);
        g2.setColor(new Color(100, 100, 130));
        g2.fillRect(0, GROUND, WIN_W, 3);

        // Draw character
        BufferedImage[] frames = currentFrames();
        if (frames == null || frames.length == 0) return;

        int safe  = Math.min(curFrame, frames.length - 1);
        int drawX = (int) x;
        int drawY = (int) y - CHAR_H;

        if (facingRight) {
            g2.drawImage(frames[safe], drawX, drawY, CHAR_W, CHAR_H, null);
        } else {
            // Flip horizontally for left movement
            g2.drawImage(frames[safe],
                    drawX + CHAR_W, drawY, -CHAR_W, CHAR_H, null);
        }

        // HUD
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        g2.drawString("LEFT / RIGHT = Run    UP = Jump    DOWN = Crouch", 10, 22);

        g2.setColor(new Color(255, 220, 80));
        g2.drawString("State: " + state, 10, 42);
    }

    // -----------------------------------------------------------------------
    // Input
    // -----------------------------------------------------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT  -> keyLeft  = true;
            case KeyEvent.VK_RIGHT -> keyRight = true;
            case KeyEvent.VK_UP    -> keyUp    = true;
            case KeyEvent.VK_DOWN  -> keyDown  = true;
            case KeyEvent.VK_SPACE -> keyUp    = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT  -> keyLeft  = false;
            case KeyEvent.VK_RIGHT -> keyRight = false;
            case KeyEvent.VK_UP    -> keyUp    = false;
            case KeyEvent.VK_DOWN  -> keyDown  = false;
            case KeyEvent.VK_SPACE -> keyUp    = false;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Warrior Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            WarriorGame panel = new WarriorGame();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
