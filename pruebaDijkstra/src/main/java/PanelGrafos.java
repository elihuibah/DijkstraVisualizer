import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PanelGrafos extends JPanel {

    public enum Mode { ADD_VERTEX, ADD_EDGE, MOVE_VERTEX, SHORTEST_PATH, CHANGE_WEIGHT }

    private Mode currentMode = Mode.ADD_VERTEX;
    private Nodo nodoSeleccionadoParaArista = null;
    private Nodo nodoMoviendo = null;
    private Point offsetAlMover = null;
    private final List<Nodo> nodos = new ArrayList<>();
    private final List<Arista> aristas = new ArrayList<>();
    private List<Nodo> caminoMasCorto = new ArrayList<>();
    private final int NODE_RADIUS = 12;

    private List<List<Nodo>> rutasAlternativas = new ArrayList<>();
    private List<Nodo> rutaSeleccionada = null;

    private boolean isDarkMode = true;

    // Colores Oscuros
    private static final Color DARK_NODE_FILL = new Color(74, 101, 232);
    private static final Color DARK_NODE_BORDER = Color.WHITE;
    private static final Color DARK_EDGE_COLOR = new Color(150, 150, 150);
    private static final Color DARK_HIGHLIGHT = new Color(255, 179, 0);

    // Colores Claros
    private static final Color LIGHT_NODE_FILL = new Color(63, 81, 181);
    private static final Color LIGHT_NODE_BORDER = Color.BLACK;
    private static final Color LIGHT_EDGE_COLOR = new Color(80, 80, 80);
    private static final Color LIGHT_HIGHLIGHT = new Color(255, 179, 0);

    // **************** CLASES INTERNAS (Se mantienen iguales) ****************

    public static class Nodo {
        private static int idCounter = 0;
        private final int id;
        private final Point posicion;

        public Nodo(int x, int y) {
            this.id = idCounter++;
            this.posicion = new Point(x, y);
        }
        public int getId() { return id; }
        public Point getPosicion() { return posicion; }
        public static void resetIDCounter() { idCounter = 0; }
        @Override public String toString() { return "Nodo " + id; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; PanelGrafos.Nodo nodo = (PanelGrafos.Nodo) o; return id == nodo.id; }
        @Override public int hashCode() { return Objects.hash(id); }
    }

    public static class Arista {
        private final Nodo origen;
        private final Nodo destino;
        private int ponderacion;

        public Arista(Nodo o, Nodo d, int p) {
            this.origen = o;
            this.destino = d;
            this.ponderacion = p;
        }
        public Nodo getOrigen() { return origen; }
        public Nodo getDestino() { return destino; }
        public int getPonderacion() { return ponderacion; }
        public void setPonderacion(int p) { this.ponderacion = p; }
        @Override public String toString() { return "Arista " + origen.getId() + " -→ " + destino.getId() + " (" + ponderacion + ")"; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Arista arista = (Arista) o;
            return (origen.equals(arista.origen) && destino.equals(arista.destino)) ||
                    (origen.equals(arista.destino) && destino.equals(arista.origen));
        }
        @Override public int hashCode() { return Objects.hash(origen, destino, ponderacion); }
    }


    // **************** MÉTODOS DE INICIALIZACIÓN Y CONTROL (Se mantienen iguales) ****************

    public PanelGrafos() {
        setOpaque(false);
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e);
                repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentMode == Mode.MOVE_VERTEX) {
                    nodoMoviendo = encontrarNodoEn(e.getX(), e.getY());
                    if (nodoMoviendo != null) {
                        Point p = nodoMoviendo.getPosicion();
                        offsetAlMover = new Point(e.getX() - p.x, e.getY() - p.y);
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                nodoMoviendo = null;
                offsetAlMover = null;
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentMode == Mode.MOVE_VERTEX && nodoMoviendo != null) {
                    nodoMoviendo.getPosicion().setLocation(e.getX() - offsetAlMover.x, e.getY() - offsetAlMover.y);
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        this.nodoSeleccionadoParaArista = null;
        repaint();
    }

    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        repaint();
    }

    // Métodos de acceso a datos y manipulación del grafo.
    public List<Nodo> getTodosLosNodos() { return Collections.unmodifiableList(nodos); }
    public List<Arista> getTodasLasAristas(){ return Collections.unmodifiableList(aristas); }

    public void eliminarNodo(Nodo nodo){
        aristas.removeIf(ar -> ar.getOrigen().equals(nodo) || ar.getDestino().equals(nodo));
        nodos.remove(nodo);
        repaint();
    }

    public void eliminarArista(Arista arista){
        aristas.removeIf(ar -> ar.equals(arista));
        repaint();
    }

    public void limpiarGrafo(){
        nodos.clear();
        aristas.clear();
        caminoMasCorto.clear();
        rutasAlternativas.clear();
        rutaSeleccionada = null;
        Nodo.resetIDCounter();
        repaint();
    }

    public void setCaminoMasCorto(List<Nodo> camino) {
        this.caminoMasCorto = camino;
        this.rutasAlternativas.clear();
        this.rutaSeleccionada = null;
        repaint();
    }

    public void limpiarCamino() {
        this.caminoMasCorto.clear();
        this.rutasAlternativas.clear();
        this.rutaSeleccionada = null;
        repaint();
    }

    public void setRutasAlternativas(List<List<Nodo>> rutas) {
        this.rutasAlternativas = rutas;
        this.caminoMasCorto.clear();
        this.rutaSeleccionada = null;
        repaint();
    }

    public List<List<Nodo>> getRutasAlternativas() {
        return rutasAlternativas;
    }

    public void setRutaSeleccionada(List<Nodo> ruta) {
        this.rutaSeleccionada = ruta;
        repaint();
    }

    public int getPonderacionBetween(Nodo n1, Nodo n2) {
        for (Arista ar : aristas) {
            if (ar.getOrigen().equals(n1) && ar.getDestino().equals(n2)) {
                return ar.getPonderacion();
            }
        }
        return -1;
    }

    public Nodo encontrarNodoPorID(int id) {
        return nodos.stream().filter(n -> n.getId() == id).findFirst().orElse(null);
    }

    public Map<Nodo, List<Arista>> getListaAdyacencia() {
        Map<Nodo, List<Arista>> grafo = new HashMap<>();
        for (Nodo n : nodos) {
            grafo.put(n, new ArrayList<>());
        }
        for (Arista ar : aristas) {
            grafo.get(ar.getOrigen()).add(ar);
        }
        return grafo;
    }

    public void addAllEdgesWithRandomWeights(int minWeight, int maxWeight) {
        Random rand = new Random();
        for (int i = 0; i < nodos.size(); i++) {
            for (int j = i + 1; j < nodos.size(); j++) {
                Nodo n1 = nodos.get(i);
                Nodo n2 = nodos.get(j);

                boolean exists = aristas.stream().anyMatch(ar -> ar.equals(new Arista(n1, n2, 0)));
                if (!exists) {
                    int weight = rand.nextInt(maxWeight - minWeight + 1) + minWeight;
                    aristas.add(new Arista(n1, n2, weight));
                    aristas.add(new Arista(n2, n1, weight));
                }
            }
        }
    }

    public void randomizeEdgeWeights(int minWeight, int maxWeight) {
        Random rand = new Random();
        for (int i = 0; i < aristas.size(); i += 2) {
            int newWeight = rand.nextInt(maxWeight - minWeight + 1) + minWeight;
            aristas.get(i).setPonderacion(newWeight);
            if (i + 1 < aristas.size()) {
                aristas.get(i + 1).setPonderacion(newWeight);
            }
        }
        repaint();
    }

    private void handleClick(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Nodo clickedNode = encontrarNodoEn(x, y);

        switch (currentMode) {
            case ADD_VERTEX:
                if (clickedNode == null) {
                    Nodo n = new Nodo(x, y);
                    nodos.add(n);
                    logMessage("Se ha agregado el nodo " + n.getId() + " en (" + x + ", " + y + ").");
                }
                break;

            case ADD_EDGE:
                if (clickedNode != null) {
                    if (nodoSeleccionadoParaArista == null) {
                        nodoSeleccionadoParaArista = clickedNode;
                    } else if (!nodoSeleccionadoParaArista.equals(clickedNode)) {
                        Nodo origen = nodoSeleccionadoParaArista;
                        Nodo destino = clickedNode;
                        int weight = -1;

                        // 1. Verificar si la arista ya existe (A->B)
                        Optional<Arista> existingEdgeAB = aristas.stream()
                                .filter(ar -> ar.getOrigen().equals(origen) && ar.getDestino().equals(destino))
                                .findFirst();

                        // 2. Verificar si la arista simétrica ya existe (B->A)
                        Optional<Arista> existingEdgeBA = aristas.stream()
                                .filter(ar -> ar.getOrigen().equals(destino) && ar.getDestino().equals(origen))
                                .findFirst();

                        // 3. Determinar el título del diálogo (Modificar vs. Añadir)
                        String title = existingEdgeAB.isPresent() ? "Modificar Ponderación" : "Añadir Ponderación";

                        String[] opcionesPonderacion = {"Ingresar Manualmente", "Ponderación Automática", "Cancelar"};
                        int opcionSeleccionada = JOptionPane.showOptionDialog(this, "Seleccione la opción de ponderación para la arista (" + origen.getId() + " - " + destino.getId() + "):",  title,  JOptionPane.YES_NO_CANCEL_OPTION,  JOptionPane.QUESTION_MESSAGE, null, opcionesPonderacion, opcionesPonderacion[0]);

                        switch (opcionSeleccionada) {
                            case 0: // Ingresar Manualmente
                                weight = ponderacionManual(title);
                                break;
                            case 1: // Ponderación Automática
                                weight = ponderacionAutomatica(origen, destino);
                                logMessage("Ponderación Automática de " + origen.getId() + " <---> " + destino.getId() + " con un valor de: " + weight + ".");
                                break;
                            case 2: // Cancelar o cerrar la opción
                            case JOptionPane.CLOSED_OPTION:
                            default:
                                weight = -1;
                                break;
                        }

                        if (weight > 0) {
                            if (existingEdgeAB.isPresent() && existingEdgeBA.isPresent()) {
                                // Modificación de aristas existentes
                                existingEdgeAB.get().setPonderacion(weight);
                                existingEdgeBA.get().setPonderacion(weight);
                                logMessage("Se ha actualizada la arista: " + origen.getId() + " <---> " + destino.getId() + ", con una nueva ponderación de " + weight + ".");
                                JOptionPane.showMessageDialog(this, "Ponderación de arista modificada correctamente a " + weight, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                // Crear nuevas aristas
                                aristas.add(new Arista(origen, destino, weight));
                                aristas.add(new Arista(destino, origen, weight));
                                logMessage("Se ha agregado una nueva arista: " + origen.getId() + " <---> " + destino.getId() + ", con una ponderación de " + weight + ".");
                            }
                            limpiarCamino();
                        } else if (opcionSeleccionada != JOptionPane.CLOSED_OPTION && opcionSeleccionada != 2) {
                            // Si no fue cancelación y weight <= 0, fue un error manual.
                            JOptionPane.showMessageDialog(this, "Ponderación inválida. Debe ser un número entero positivo.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                        }

                        nodoSeleccionadoParaArista = null;
                    }

                }
                break;
        }
    }

    private Nodo encontrarNodoEn(int x, int y) {
        for (Nodo n : nodos) {
            Point p = n.getPosicion();
            if (p.distance(x, y) <= NODE_RADIUS) {
                return n;
            }
        }
        return null;
    }

    private int ponderacionAutomatica(Nodo origen, Nodo destino){
        Point p1 = origen.getPosicion();
        Point p2 = destino.getPosicion();

        double distancia = Math.sqrt(Math.pow((p2.x - p1.x), 2) + Math.pow((p2.y - p1.y), 2));
        int peso = (int) Math.round(distancia);
        //Asegurar peso mínimo si la distancia es 0
        return peso > 0 ? peso : 1;
    }

    private int ponderacionManual(String title){
        String ponderacionStr = JOptionPane.showInputDialog(null, "Ingrese la ponderación para la arista:", "Ponderación de la arista", JOptionPane.PLAIN_MESSAGE);
        try {
            if(ponderacionStr == null) return -1; //Cancelar
            int ponderacion = Integer.parseInt(ponderacionStr.trim());
            return ponderacion > 0 ? ponderacion : -1;
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número entero positivo.","Error",JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    //Mecanismo de callback
    private DefaultListModel<String> logListModel;
    //Modelo de lista donde se registran eventos
    public void setLogListModel(DefaultListModel<String> model) { //
        this.logListModel = model;
    }

    private void logMessage(String message){
        if(logListModel != null) {
            logListModel.addElement(message);
        }
    }


    // **************** LÓGICA DE DIBUJO ****************

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Dibujar Aristas
        for (Arista ar : aristas) {
            if (ar.getOrigen().getId() < ar.getDestino().getId()) {
                drawEdge(g2d, ar);
            }
        }

        // 2. Dibujar Nodos
        for (Nodo n : nodos) {
            drawNode(g2d, n);
        }

        // 3. Dibujar Arista temporal
        if (currentMode == Mode.ADD_EDGE && nodoSeleccionadoParaArista != null) {
            Point p = nodoSeleccionadoParaArista.getPosicion();
            g2d.setColor(isDarkMode ? DARK_HIGHLIGHT : LIGHT_HIGHLIGHT);
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{9}, 0));
            Point mousePos = getMousePosition();
            if (mousePos != null) {
                g2d.drawLine(p.x, p.y, mousePos.x, mousePos.y);
            }
        }

        g2d.dispose();
    }

    private void drawEdge(Graphics2D g2d, Arista ar) {
        Point p1 = ar.getOrigen().getPosicion();
        Point p2 = ar.getDestino().getPosicion();

        Color colorArista = isDarkMode ? DARK_EDGE_COLOR : LIGHT_EDGE_COLOR;
        Color colorPesoTexto = isDarkMode ? Color.WHITE : Color.BLACK;

        float thickness = 2;

        boolean isCaminoCorto = isEdgeInPath(caminoMasCorto, ar);
        boolean isRutaSeleccionada = isEdgeInPath(rutaSeleccionada, ar);

        if (isCaminoCorto) {
            colorArista = Color.RED;
            thickness = 4;
            colorPesoTexto = Color.WHITE;
        } else if (isRutaSeleccionada) {
            colorArista = new Color(0, 150, 255);
            thickness = 4;
            colorPesoTexto = Color.WHITE;
        }

        // Dibuja la línea
        g2d.setColor(colorArista);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

        // --- Dibujar Ponderación (Peso) sin fondo ---
        int midX = (p1.x + p2.x) / 2;
        int midY = (p1.y + p2.y) / 2;

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String weightStr = String.valueOf(ar.getPonderacion());

        FontMetrics fm = g2d.getFontMetrics();
        int w = fm.stringWidth(weightStr);
        int h = fm.getHeight();

        // Detalle Clave: Mover la posición vertical hacia arriba (restamos un offset)
        int offset = 10; // Distancia para elevar el texto
        int textY = midY + h / 4 - offset;

        // Dibuja el texto del peso
        g2d.setColor(colorPesoTexto);
        g2d.drawString(weightStr, midX - w / 2, textY);
    }

    private boolean isEdgeInPath(List<Nodo> path, Arista ar) {
        if (path == null || path.size() < 2) return false;
        for (int i = 0; i < path.size() - 1; i++) {
            Nodo u = path.get(i);
            Nodo v = path.get(i + 1);
            if (ar.equals(new Arista(u, v, 0))) {
                return true;
            }
        }
        return false;
    }

    private void drawNode(Graphics2D g2d, Nodo n) {
        Point p = n.getPosicion();
        int x = p.x - NODE_RADIUS;
        int y = p.y - NODE_RADIUS;

        Color nodeFill = isDarkMode ? DARK_NODE_FILL : LIGHT_NODE_FILL;
        Color nodeBorder = isDarkMode ? DARK_NODE_BORDER : LIGHT_NODE_BORDER;

        if (n.equals(nodoSeleccionadoParaArista)) {
            nodeFill = isDarkMode ? DARK_HIGHLIGHT.darker() : LIGHT_HIGHLIGHT.darker();
        } else if (caminoMasCorto.contains(n)) {
            nodeFill = Color.RED.brighter();
        } else if (rutaSeleccionada != null && rutaSeleccionada.contains(n)) {
            nodeFill = new Color(0, 150, 255).brighter();
        }

        // Dibujar relleno
        g2d.setColor(nodeFill);
        g2d.fillOval(x, y, 2 * NODE_RADIUS, 2 * NODE_RADIUS);

        // Dibujar borde
        g2d.setColor(nodeBorder);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x, y, 2 * NODE_RADIUS, 2 * NODE_RADIUS);

        // Dibujar ID del nodo
        String idStr = String.valueOf(n.getId());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int w = g2d.getFontMetrics().stringWidth(idStr);
        int h = g2d.getFontMetrics().getHeight();
        g2d.drawString(idStr, p.x - w / 2, p.y + h / 4);
    }
}