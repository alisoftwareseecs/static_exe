import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class WarriorJumpAnimation extends JPanel implements ActionListener, KeyListener {

    private static final int TOTAL_FRAMES  = 6;
    private static final int ANIM_DELAY_MS = 120;
    private static final int WINDOW_WIDTH  = 600;
    private static final int WINDOW_HEIGHT = 400;
    private static final int GROUND_Y      = 340;

    // Target display size for the character (pixels on screen)
    private static final int DISPLAY_W = 80;
    private static final int DISPLAY_H = 80;

    // Physics
    private static final float GRAVITY    = 0.55f;
    private static final float JUMP_FORCE = -13.0f;

    private BufferedImage[] frames;

    private int     currentFrame = 0;
    private int     animTick     = 0;

    private float   characterX   = 100;
    private float   characterY   = GROUND_Y;
    private float   velocityY    = 0;
    private boolean isJumping    = false;

    private Timer gameTimer;

    public WarriorJumpAnimation() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadSprite();
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    private void loadSprite() {
        BufferedImage sheet;
        try {
            sheet = ImageIO.read(new File("Warrior_Idle_1 (3).png"));
        } catch (IOException e) {
            System.err.println("ERROR: Could not load 'Warrior_Idle_1 (3).png'");
            System.exit(1);
            return;
        }

        int frameW = sheet.getWidth()  / TOTAL_FRAMES;
        int frameH = sheet.getHeight();

        System.out.println("Sheet: " + sheet.getWidth() + "x" + sheet.getHeight());
        System.out.println("Frame: " + frameW + "x" + frameH);
        System.out.println("Display size: " + DISPLAY_W + "x" + DISPLAY_H);

        frames = new BufferedImage[TOTAL_FRAMES];
        for (int i = 0; i < TOTAL_FRAMES; i++) {
            // Slice the raw frame
            BufferedImage raw = sheet.getSubimage(i * frameW, 0, frameW, frameH);
            // Scale it down to DISPLAY_W x DISPLAY_H
            BufferedImage scaled = new BufferedImage(DISPLAY_W, DISPLAY_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D sg = scaled.createGraphics();
            sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            sg.drawImage(raw, 0, 0, DISPLAY_W, DISPLAY_H, null);
            sg.dispose();
            frames[i] = scaled;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updatePhysics();
        updateAnimation();
        repaint();
    }

    private void updatePhysics() {
        if (isJumping) {
            velocityY  += GRAVITY;
            characterY += velocityY;
            if (characterY >= GROUND_Y) {
                characterY = GROUND_Y;
                velocityY  = 0;
                isJumping  = false;
            }
        }
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= ANIM_DELAY_MS / 16) {
            animTick = 0;
            currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (frames == null) return;

        // Ground line
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(0, GROUND_Y, WINDOW_WIDTH, 3);

        // Draw sprite: feet at characterY, so top = characterY - DISPLAY_H
        int drawX = (int) characterX;
        int drawY = (int) characterY - DISPLAY_H;
        g2.drawImage(frames[currentFrame], drawX, drawY, null);

        // HUD
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("Press SPACE or UP to Jump", 20, 25);
        if (!isJumping) {
            g2.setColor(new Color(180, 230, 180));
            g2.drawString("[ ready ]", 20, 45);
        } else {
            g2.setColor(new Color(255, 200, 80));
            g2.drawString("[ jumping! ]", 20, 45);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if ((key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) && !isJumping) {
            velocityY = JUMP_FORCE;
            isJumping = true;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Warrior Jump Animation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            WarriorJumpAnimation panel = new WarriorJumpAnimation();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
