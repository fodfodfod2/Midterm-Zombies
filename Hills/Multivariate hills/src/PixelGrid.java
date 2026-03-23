import javax.swing.*;
import java.awt.*;

public class PixelGrid extends JPanel {
	private int[][] arr;
    // Example 2D array
    public PixelGrid(int[][] arg, int pixelSize) {
    	this.arr =arg;
    	this.PIXEL_SIZE = pixelSize;
    }

    // Define colors
    private Color[] colorMap = {
        Color.WHITE,  // 0
        Color.BLACK,  // 1
        Color.RED,    // 2
        Color.BLUE,   // 3 (optional)
        Color.MAGENTA,   // 4 (optional)
        Color.ORANGE, // 5
        Color.LIGHT_GRAY
    };

    private int PIXEL_SIZE = 1;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw pixels
        for (int y = 0; y < arr.length; y++) {
            for (int x = 0; x < arr[y].length; x++) {
                int val = arr[y][x];
                Color color = (val >= 0 && val < colorMap.length)
                        ? colorMap[val]
                        : Color.GRAY; // fallback
                g.setColor(color);
                g.fillRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
            }
        }
    }

}
