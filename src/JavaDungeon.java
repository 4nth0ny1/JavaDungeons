import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;

public class JavaDungeon extends JPanel {

    private double playerX = 50, playerY = 50;
    private final int playerWidth = 32, playerHeight = 32;
    private boolean up, down, left, right;

    // ★ Camera (world-space origin that’s at the top-left of what you see)
    private int camX = 0, camY = 0; // ★

    private final int[][] map = {
            // 20 wide × many tall — vertical scroll will work now; add more columns for horizontal scroll
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,2,0,0,1,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,1,2,0,0,1,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    // Derived map size
    private final int MAP_H = map.length;
    private final int MAP_W = map[0].length;

    private final Timer loop = new Timer(16, e -> { update(); repaint(); });

    public JavaDungeon() {
        setPreferredSize(new Dimension(640, 480));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        // Make sure we actually get keyboard focus right away
        setFocusable(true); // ★
        addHierarchyListener(e -> { // ★
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                requestFocusInWindow();
            }
        });

        bind("pressed W", "up-pressed", () -> up = true);
        bind("released W", "up-released", () -> up = false);
        bind("pressed S", "down-pressed", () -> down = true);
        bind("released S", "down-released", () -> down = false);
        bind("pressed A", "left-pressed", () -> left = true);
        bind("released A", "left-released", () -> left = false);
        bind("pressed D", "right-pressed", () -> right = true);
        bind("released D", "right-released", () -> right = false);

        loop.start();
    }

    private void bind(String stroke, String name, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(stroke), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); }
        });
    }

    private void update() {
        final int tile = Globals.TILE_PIXEL_SIZE;
        final int worldWidth  = MAP_W * tile;   // ★ world size in pixels
        final int worldHeight = MAP_H * tile;   // ★
        final int viewWidth = Math.max(1, getWidth());
        final int viewHeight = Math.max(1, getHeight());

        double dx = 0, dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;

        // normalize diagonal so speed is consistent
        if (dx != 0 && dy != 0) {
            double inv = 1.0 / Math.sqrt(2);
            dx *= inv; dy *= inv;
        }

        // apply speed (pixels per tick)
        playerX += dx * Globals.PLAYER_SPEED;
        playerY += dy * Globals.PLAYER_SPEED;

        // ★ Clamp player to the WORLD, not the view
        playerX = Math.max(0, Math.min(playerX, worldWidth  - playerWidth));  // ★
        playerY = Math.max(0, Math.min(playerY, worldHeight - playerHeight)); // ★

        // ★ Center camera on player, then clamp to world so it doesn’t show outside
        int pcx = (int)Math.round(playerX) + playerWidth / 2;
        int pcy = (int)Math.round(playerY) + playerHeight / 2;
        camX = Math.max(0, Math.min(pcx - viewWidth / 2,  worldWidth  - viewWidth));  // ★
        camY = Math.max(0, Math.min(pcy - viewHeight / 2, worldHeight - viewHeight)); // ★
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final int tile = Globals.TILE_PIXEL_SIZE;
        final int viewWidth = Math.max(1, getWidth());
        final int viewHeight = Math.max(1, getHeight());

        // ★ Visible tile range (half-open [start, end))
        int startCol = Math.max(0, camX / tile);                                      // ★
        int endCol   = Math.min(MAP_W, (camX + viewWidth  + tile - 1) / tile);        // ★
        int startRow = Math.max(0, camY / tile);                                      // ★
        int endRow   = Math.min(MAP_H, (camY + viewHeight + tile - 1) / tile);        // ★

        // ★ Draw tiles in screen space (world-to-screen via -camX/-camY)
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int screenX = c * tile - camX; // ★
                int screenY = r * tile - camY; // ★

                int cell = map[r][c];
                if (cell == 1) g.setColor(new Color(40, 40, 40));      // wall
                else if (cell == 2) g.setColor(new Color(80, 80, 80)); // obstacle
                else               g.setColor(new Color(170, 170, 170)); // floor
                g.fillRect(screenX, screenY, tile, tile);
            }
        }

        // Draw player (world -> screen)
        int drawX = (int)Math.round(playerX) - camX;
        int drawY = (int)Math.round(playerY) - camY;
        g.setColor(Color.BLUE);
        g.fillRect(drawX, drawY, playerWidth, playerHeight);

        // Debug overlay
        g.setColor(Color.WHITE);
        g.drawString("Cam: (" + camX + "," + camY + ") View: " + viewWidth + "x" + viewHeight, 10, 20);
        g.drawString("Tiles: " + MAP_W + "x" + MAP_H + "  TilePx: " + tile, 10, 36);
    }

    public static void main (String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Java Dungeon");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.setContentPane(new JavaDungeon());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
