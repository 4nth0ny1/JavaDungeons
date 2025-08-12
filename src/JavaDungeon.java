import javax.swing.*;
import java.awt.*;

public class JavaDungeon extends JPanel {

    private double playerX = 50, playerY = 50;
    private final int playerWidth = 32, playerHeight = 32;
    private boolean up, down, left, right;

    private final int[][] map = {
        // 20 wide × 15 tall (20 cols, 15 rows)
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
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    // Derived map size
    private final int MAP_H = map.length;
    private final int MAP_W = map[0].length;

    private final Timer loop = new Timer(16, e -> {
        update();
        repaint();
    });

    public JavaDungeon() {
        setPreferredSize(new Dimension(640, 480));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);

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
        double playerDeltaX = 0, playerDeltaY = 0;
        if (up) playerDeltaY -= 1;
        if (down) playerDeltaY += 1;
        if (left) playerDeltaX -= 1;
        if (right) playerDeltaX += 1;

        // normalize diagonal so speed is consistent
        if (playerDeltaX != 0 && playerDeltaY != 0) {
            double inv = 1.0 / Math.sqrt(2);
            playerDeltaX *= inv; playerDeltaY *= inv;
        }

        // apply speed (pixels per tick)
        playerX += playerDeltaX * Globals.PLAYER_SPEED;
        playerY += playerDeltaY * Globals.PLAYER_SPEED;

        // clamp player to the map
        playerX = Math.max(0, Math.min(playerX, getWidth()  - playerWidth));
        playerY = Math.max(0, Math.min(playerY, getHeight() - playerHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw map
        for (int tileRowIndex = 0; tileRowIndex < MAP_H; tileRowIndex++) {
            for (int tileColIndex = 0; tileColIndex < MAP_W; tileColIndex++) {
                int TileLocX = tileColIndex * Globals.TILE_PIXEL_SIZE; // if 1 * 32, then the tile goes 32 pixels from 0
                int TileLocY = tileRowIndex * Globals.TILE_PIXEL_SIZE;
                if (map[tileRowIndex][tileColIndex] == 1) g.setColor(new Color(40, 40, 40));   // wall
                else if (map[tileRowIndex][tileColIndex] == 2) g.setColor(new Color(80,80,80));
                else                  g.setColor(new Color(170,170,170));  // floor
                g.fillRect(TileLocX, TileLocY, Globals.TILE_PIXEL_SIZE, Globals.TILE_PIXEL_SIZE);
            }
        }

        g.setColor(Color.BLUE);
        g.fillRect((int)Math.round(playerX), (int)Math.round(playerY), playerWidth, playerHeight);
    }

    public static void main (String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Java Dungeon");
            f.setContentPane(new JavaDungeon());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}