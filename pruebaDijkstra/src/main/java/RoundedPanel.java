import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    private int radius = 40;

    public RoundedPanel() { setOpaque(false); }
    public RoundedPanel(int r) { radius = r; setOpaque(false); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);

        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
