import javax.swing.*;
import java.awt.*;

// Clase necesaria para el diseño redondeado del panel de dibujo.
public class RoundedPanel extends JPanel {
    private final int cornerRadius;

    public RoundedPanel(int r) {
        this.cornerRadius = r;
        setOpaque(false); // Necesario para que el fondo redondeado se vea.
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(getBackground());
        // Dibuja el rectángulo redondeado que llena el panel
        graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
        graphics.setColor(getForeground());
    }
}