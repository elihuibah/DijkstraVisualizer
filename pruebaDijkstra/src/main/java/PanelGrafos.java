import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Panel que contiene la lógica del grafo: nodos, aristas, dibujo y eventos de mouse.
 */
public class PanelGrafos extends JPanel {

    public enum Mode { ADD_VERTEX, ADD_EDGE, MOVE_VERTEX, SHORTEST_PATH, CHANGE_WEIGHT }

    private Mode currentMode = Mode.ADD_VERTEX;
    private int requestedWeight = -1;

    private final List<Nodo> nodos = new ArrayList<>();
    private final List<Arista> aristas = new ArrayList<>();
    private Nodo nodoSeleccionadoParaArista = null;
    private Nodo nodoMoviendo = null;
    private Point offsetAlMover = null;
    private List<Nodo> caminoMasCorto = new ArrayList<>();
    private final int NODE_RADIUS = 12;


    private List<List<Nodo>> rutasAlternativas = new ArrayList<>();
    private List<Nodo> rutaSeleccionada = null;


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
        this.nodoMoviendo = null;
        this.caminoMasCorto.clear();
        repaint();
    }

    public void setRequestedWeight(int w) { this.requestedWeight = w; }

    private void handleClick(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        if (SwingUtilities.isLeftMouseButton(e)) {
            Nodo clicked = encontrarNodoEn(x, y);
            switch (currentMode) {
                case ADD_VERTEX:
                    if (clicked == null) {
                        Nodo n = new Nodo(x, y);
                        nodos.add(n);
                    }
                    break;
                case ADD_EDGE:
                    if (clicked != null) {
                        if (nodoSeleccionadoParaArista == null) {
                            nodoSeleccionadoParaArista = clicked;
                        } else if (nodoSeleccionadoParaArista != clicked) {
                            int peso = requestedWeight;
                            if (peso <= 0) {
                                String s = JOptionPane.showInputDialog(this, "Ingrese peso entero positivo:");
                                if (s == null) { nodoSeleccionadoParaArista = null; return; }
                                try { peso = Integer.parseInt(s.trim()); } catch (NumberFormatException ex) { peso = 1; }
                            }
                            // Añadir arista bidireccional (si quieres dirigida ajusta)
                            aristas.add(new Arista(nodoSeleccionadoParaArista, clicked, peso));
                            aristas.add(new Arista(clicked, nodoSeleccionadoParaArista, peso));
                            nodoSeleccionadoParaArista = null;
                        } else {
                            nodoSeleccionadoParaArista = null;
                        }
                    }
                    break;
                case SHORTEST_PATH:
                    // seleccionar inicio y fin para resaltar (alternativa a usar botón Dijkstra)
                    if (clicked != null) {
                        if (nodoSeleccionadoParaArista == null) {
                            nodoSeleccionadoParaArista = clicked;
                        } else {
                            Nodo inicio = nodoSeleccionadoParaArista;
                            Nodo destino = clicked;
                            Map<Nodo, List<Arista>> adj = getListaAdyacencia();
                            List<Nodo> camino = Dijkstra.encontrarCaminoMasCorto(adj, inicio, destino);
                            if (!camino.isEmpty()) setCaminoMasCorto(camino);
                            nodoSeleccionadoParaArista = null;
                        }
                    }
                    break;
                case CHANGE_WEIGHT:
                    // clic en arista cercana -> cambiar peso
                    Arista a = encontrarAristaEn(x, y);
                    if (a != null) {
                        String s = JOptionPane.showInputDialog(this, "Nuevo peso (entero):", a.getPonderacion());
                        if (s != null) {
                            try {
                                int neww = Integer.parseInt(s.trim());
                                // reemplazamos aristas en ambas direcciones si existe
                                reemplazarPesoArista(a.getOrigen(), a.getDestino(), neww);
                            } catch (NumberFormatException ex) { /* ignore */ }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // añade aristas entre todos los pares (si ya existe no duplica)
    public void addAllEdgesWithRandomWeights(int min, int max) {
        Random rnd = new Random();
        for (int i = 0; i < nodos.size(); i++) {
            for (int j = i+1; j < nodos.size(); j++) {
                Nodo a = nodos.get(i);
                Nodo b = nodos.get(j);
                if (!existeArista(a, b)) {
                    int w = rnd.nextInt(max - min + 1) + min;
                    aristas.add(new Arista(a, b, w));
                    aristas.add(new Arista(b, a, w));
                }
            }
        }
    }

    // randomiza pesos existentes
    public void randomizeEdgeWeights(int min, int max) {
        Random rnd = new Random();
        List<Arista> nuevas = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();
        for (Arista ar : aristas) {
            String key = ar.getOrigen().getId() + "-" + ar.getDestino().getId();
            int w = rnd.nextInt(max - min + 1) + min;
            nuevas.add(new Arista(ar.getOrigen(), ar.getDestino(), w));
            seenPairs.add(key);
        }
        aristas.clear();
        aristas.addAll(nuevas);
    }

    // reemplaza peso entre dos nodos en ambas direcciones si existen
    private void reemplazarPesoArista(Nodo a, Nodo b, int neww) {
        for (int i = 0; i < aristas.size(); i++) {
            Arista ar = aristas.get(i);
            if (ar.getOrigen().equals(a) && ar.getDestino().equals(b)) {
                aristas.set(i, new Arista(a, b, neww));
            }
            if (ar.getOrigen().equals(b) && ar.getDestino().equals(a)) {
                aristas.set(i, new Arista(b, a, neww));
            }
        }
        repaint();
    }

    private boolean existeArista(Nodo a, Nodo b) {
        for (Arista ar : aristas) {
            if (ar.getOrigen().equals(a) && ar.getDestino().equals(b)) return true;
        }
        return false;
    }

    // Método simple para resaltar MST (Prim). Resalta nodos en caminoMasCorto como forma de visualizar.
    public void highlightMST() {
        // construir adj y aplicar Prim (sobre nodos existentes)
        Map<Nodo, List<Arista>> adj = getListaAdyacencia();
        if (nodos.isEmpty()) return;

        Set<Nodo> inTree = new HashSet<>();
        List<Arista> mst = new ArrayList<>();
        Nodo start = nodos.get(0);
        inTree.add(start);
        PriorityQueue<Arista> pq = new PriorityQueue<>(Comparator.comparingInt(Arista::getPonderacion));
        pq.addAll(adj.get(start));

        while (!pq.isEmpty() && inTree.size() < nodos.size()) {
            Arista min = pq.poll();
            if (inTree.contains(min.getDestino())) continue;
            mst.add(min);
            Nodo newNode = min.getDestino();
            inTree.add(newNode);
            pq.addAll(adj.get(newNode));
        }

        // convertir mst a lista de nodos para resaltar (caminoMasCorto usado como resaltado)
        Set<Nodo> nodesInMst = new HashSet<>();
        for (Arista ar : mst) { nodesInMst.add(ar.getOrigen()); nodesInMst.add(ar.getDestino()); }
        this.caminoMasCorto = new ArrayList<>(nodesInMst);
        repaint();
    }

    public void setCaminoMasCorto(List<Nodo> camino) {
        this.caminoMasCorto = new ArrayList<>(camino);
        repaint();
    }

    public void limpiarCamino() {
        this.caminoMasCorto = new ArrayList<>();
        repaint();
    }

    public List<Nodo> getTodosLosNodos() {
        return Collections.unmodifiableList(nodos);
    }

    public Nodo encontrarNodoPorID(int id) {
        for (Nodo n : nodos) if (n.getId() == id) return n;
        return null;
    }

    public Map<Nodo, List<Arista>> getListaAdyacencia() {
        Map<Nodo, List<Arista>> adj = new HashMap<>();
        for (Nodo n : nodos) adj.put(n, new ArrayList<>());
        for (Arista ar : aristas) {
            adj.computeIfAbsent(ar.getOrigen(), k -> new ArrayList<>()).add(ar);
        }
        return adj;
    }

    public int getPonderacionBetween(Nodo a, Nodo b) {
        for (Arista ar : aristas) {
            if (ar.getOrigen().equals(a) && ar.getDestino().equals(b)) return ar.getPonderacion();
        }
        return 0;
    }

    private Arista encontrarAristaEn(int x, int y) {
        // buscamos arista cuya distancia al punto medio sea < tol
        int tol = 8;
        for (Arista ar : aristas) {
            Point p1 = ar.getOrigen().getPosicion();
            Point p2 = ar.getDestino().getPosicion();
            int mx = (p1.x + p2.x) / 2;
            int my = (p1.y + p2.y) / 2;
            if (Point.distance(mx, my, x, y) < tol) return ar;
        }
        return null;
    }

    private Nodo encontrarNodoEn(int x, int y) {
        for (Nodo n : nodos) {
            if (n.getPosicion().distance(x, y) <= NODE_RADIUS + 6) return n;
        }
        return null;
    }

    public void setRutasAlternativas (List<List<Nodo>> rutas) {
        this.rutasAlternativas = rutas;
    }
    public List<List<Nodo>> getRutasAlternativas() {
        return rutasAlternativas;
    }
    public void setRutaSeleccionada (List<Nodo> ruta) {
        this.rutaSeleccionada = ruta;
        this.caminoMasCorto.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // DIBUJAR aristas
        g2.setStroke(new BasicStroke(2));
        for (Arista ar : aristas) {
            Point p1 = ar.getOrigen().getPosicion();
            Point p2 = ar.getDestino().getPosicion();
            g2.setColor(Color.MAGENTA);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

            // peso
            int mx = (p1.x + p2.x) / 2;
            int my = (p1.y + p2.y) / 2;
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf(ar.getPonderacion()), mx, my - 6);
        }

        // DIBUJAR camino resaltado (si aplica)
        if (caminoMasCorto != null && caminoMasCorto.size() > 1) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3));
            for (int i = 0; i < caminoMasCorto.size() - 1; i++) {
                Point p1 = caminoMasCorto.get(i).getPosicion();
                Point p2 = caminoMasCorto.get(i + 1).getPosicion();
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        //DIBUJA la ruta seleccionada en rutas alternativas
        if (rutaSeleccionada != null && rutaSeleccionada.size() > 1) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(3));
            for (int i = 0; i < rutaSeleccionada.size() - 1; i++) {
                Point p1 = rutaSeleccionada.get(i).getPosicion();
                Point p2 = rutaSeleccionada.get(i + 1).getPosicion();
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // DIBUJAR nodos
        for (Nodo n : nodos) {
            Point p = n.getPosicion();
            boolean enCamino = caminoMasCorto.contains(n);
            if (n.equals(nodoSeleccionadoParaArista)) {
                g2.setColor(Color.RED);
            } else if (enCamino) {
                g2.setColor(Color.ORANGE);
            } else {
                g2.setColor(Color.BLACK);
            }
            g2.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(n.getId()), p.x - 4, p.y + 4);
        }

        g2.dispose();

    }
}
