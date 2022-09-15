package nesemulator.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenUI extends JFrame {
    ImagePanel screenCanvas;

    public ScreenUI(String title) throws HeadlessException {
        super(title);
        this.screenCanvas = new ImagePanel();
        this.screenCanvas.setPreferredSize(new Dimension(256, 240));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.getContentPane().add(this.screenCanvas);
    }

    public void updateScreen(BufferedImage img) {
        screenCanvas.setImage(img);
        this.repaint();
    }
}

class ImagePanel extends JPanel {

    private BufferedImage image;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null)
            g.drawImage(image, 0, 0, this);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.invalidate();
    }
}
