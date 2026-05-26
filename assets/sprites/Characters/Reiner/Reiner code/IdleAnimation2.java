import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// HOW TO RUN:
//   1. Put IdleAnimation2.java and Idle.png in the same folder
//   2. Compile: javac IdleAnimation2.java
//   3. Run:     java IdleAnimation2
//
// CONTROLS:
//   SPACE = pause / resume
//   +     = speed up
//   -     = slow down

public class IdleAnimation2 extends JPanel implements Runnable {

    private static final String FILE_NAME   = "Idle.png";
    private static final int    FRAME_COUNT = 7;
    private static final int    FRAME_W     = 128;
    private static final int    FRAME_H     = 128;
    private static final int    SCALE       = 3;
    private static final int    DISP_W      = FRAME_W * SCALE;
    private static final int    DISP_H      = FRAME_H * SCALE;

    private BufferedImage[]  frames       = new BufferedImage[FRAME_COUNT];
    private int              currentFrame = 0;
    private boolean          paused       = false;
    private int              fps          = 10;
    private volatile boolean running      = false;

    public IdleAnimation2() {
        setPreferredSize(new Dimension(DISP_W + 40, DISP_H + 50));
        setBackground(new Color(20, 20, 30));
        setFocusable(true);
        loadFrames();
        setupKeys();
    }

    private void loadFrames() {
        BufferedImage sheet;
        try {
            File f = new File(FILE_NAME);
            if (!f.exists()) {
                System.err.println("ERROR: " + FILE_NAME + " not found. Put it in the same folder.");
                System.exit(1);
            }
            sheet = ImageIO.read(f);
            System.out.println("Loaded: " + sheet.getWidth() + "x" + sheet.getHeight());
        } catch (IOException e) {
            System.err.println("ERROR loading image: " + e.getMessage());
            System.exit(1);
            return;
        }

        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = sheet.getSubimage(i * FRAME_W, 0, FRAME_W, FRAME_H);
        }
        System.out.println("Sliced " + FRAME_COUNT + " frames of " + FRAME_W + "x" + FRAME_H);
    }

    private void setupKeys() {
        InputMap  im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("SPACE"), "pause");
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
                fps = Math.min(fps + 2, 30);
                repaint();
            }
        });

        im.put(KeyStroke.getKeyStroke("MINUS"),    "slower");
        im.put(KeyStroke.getKeyStroke("SUBTRACT"), "slower");
        am.put("slower", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fps = Math.max(fps - 2, 2);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);

        int x = (getWidth()  - DISP_W) / 2;
        int y = (getHeight() - DISP_H) / 2 - 10;
        g2.drawImage(frames[currentFrame], x, y, DISP_W, DISP_H, null);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(160, 160, 190));
        String hud = "Frame " + (currentFrame + 1) + "/" + FRAME_COUNT
                   + "  |  " + fps + " FPS"
                   + "  |  " + (paused ? "PAUSED" : "PLAYING")
                   + "  |  SPACE=pause  +/-=speed";
        g2.drawString(hud, 6, getHeight() - 8);
    }

    public void start() {
        running = true;
        Thread t = new Thread(this, "idle-anim-2");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IdleAnimation2 anim = new IdleAnimation2();
            anim.start();

            JFrame frame = new JFrame("Idle Animation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(anim);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
