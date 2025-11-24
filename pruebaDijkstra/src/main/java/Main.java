import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;

public class Main extends JFrame {

    private final PanelGrafos panelGrafos;

    public Main() {
        setTitle("Graph GUI — Simulador de Grafos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // PANEL IZQUIERDO (CONTROLES)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBackground(new Color(245, 245, 245));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Radio buttons (modos)
        JRadioButton addVertexBtn = new JRadioButton("Add Vertex");
        JRadioButton addEdgeBtn = new JRadioButton("Add Edges");
        JRadioButton moveVertexBtn = new JRadioButton("Move Vertex");
        JRadioButton shortestPathBtn = new JRadioButton("Shortest Path");
        JRadioButton changeWeightBtn = new JRadioButton("Change a Weight to:");

        ButtonGroup group = new ButtonGroup();
        group.add(addVertexBtn);
        group.add(addEdgeBtn);
        group.add(moveVertexBtn);
        group.add(shortestPathBtn);
        group.add(changeWeightBtn);

        // Campo peso
        JTextField weightField = new JTextField(6);

        leftPanel.add(addVertexBtn);
        leftPanel.add(addEdgeBtn);
        leftPanel.add(moveVertexBtn);
        leftPanel.add(shortestPathBtn);

        JPanel weightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        weightPanel.setBackground(new Color(245, 245, 245));
        weightPanel.add(changeWeightBtn);
        weightPanel.add(weightField);
        leftPanel.add(weightPanel);

        leftPanel.add(Box.createVerticalStrut(10));

        // Botones grandes
        JButton btnAddAll = new JButton("Add All Edges");
        JButton btnRandom = new JButton("Random Weight");
        JButton btnHelp = new JButton("Help");
        JButton btnMST = new JButton("Minimal Spanning Tree");
        JButton btnAlternativas = new JButton("Alternatives routes");


        Dimension big = new Dimension(200, 36);
        btnAddAll.setMaximumSize(big);
        btnRandom.setMaximumSize(big);
        btnHelp.setMaximumSize(big);
        btnMST.setMaximumSize(big);
        btnAlternativas.setPreferredSize(big);


        leftPanel.add(btnAddAll);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnRandom);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnHelp);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnMST);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnAlternativas);


        add(leftPanel, BorderLayout.WEST);

        // PANEL DERECHO: RoundedPanel que contiene PanelGrafos
        RoundedPanel panelDibujo = new RoundedPanel(40);
        panelDibujo.setLayout(new BorderLayout());
        panelDibujo.setPreferredSize(new Dimension(680, 600));
        panelDibujo.setBackground(Color.WHITE);

        panelGrafos = new PanelGrafos();
        panelGrafos.setOpaque(false); // para que el fondo redondeado sea visible
        panelDibujo.add(panelGrafos, BorderLayout.CENTER);

        add(panelDibujo, BorderLayout.CENTER);

        // PANEL INFERIOR con Dijkstra
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(new Color(245, 245, 245));
        JButton botonDijkstra = new JButton("Calcular caminos más cortos (Dijkstra)");
        bottom.add(botonDijkstra);
        add(bottom, BorderLayout.SOUTH);

        //MODELO Y LISTA PARA MOSTRAR RUTAS ALTERNATIVAS
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> listaRutas = new JList<>(listModel);
        JScrollPane listaScroll = new JScrollPane(listaRutas);
        listaScroll.setPreferredSize(new Dimension(600, 100));

        btnAlternativas.addActionListener(ev -> {
            if (listaScroll.getParent() == null) {
                bottom.add(listaScroll);
                bottom.revalidate();
                bottom.repaint();
            }
            panelGrafos.limpiarCamino();
            java.util.List<Nodo> nodos = panelGrafos.getTodosLosNodos();
            if (nodos.size() < 2) {
                return;
            }

            Nodo inicio = promptParaNodo("Inicio:", nodos);
            if (inicio == null) {
                return;
            }
            Nodo destino = promptParaNodo("Destino:", nodos);
            if (destino == null) {
                return;
            }

            Map<Nodo, List<Arista>> g = panelGrafos.getListaAdyacencia();

            List<List<Nodo>> rutas = CaminosAlt.CaminosAlt(g, inicio, destino, 5);
            listModel.clear();
            for (int i = 0; i < rutas.size(); i++) {
                String texto = "Ruta " + (i + 1) + ": " + formatearRuta(rutas.get(i));
                listModel.addElement(texto);
            }
            panelGrafos.setRutasAlternativas(rutas);
        });

        //CUANDO EL USUARIO SELECCIONA UNA RUTA EN LA LISTA (RUTAS ALT)
        listaRutas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int indice = listaRutas.getSelectedIndex();
                if (indice >= 0 && panelGrafos.getRutasAlternativas().size() > 0) {
                    panelGrafos.setRutaSeleccionada(panelGrafos.getRutasAlternativas().get(indice));
                }
            }
        });

        // ---------- Enlazar controles con la lógica ----------
        // Modo por defecto: Add Vertex
        addVertexBtn.setSelected(true);
        panelGrafos.setMode(PanelGrafos.Mode.ADD_VERTEX);

        addVertexBtn.addActionListener(e -> panelGrafos.setMode(PanelGrafos.Mode.ADD_VERTEX));
        addEdgeBtn.addActionListener(e -> panelGrafos.setMode(PanelGrafos.Mode.ADD_EDGE));
        moveVertexBtn.addActionListener(e -> panelGrafos.setMode(PanelGrafos.Mode.MOVE_VERTEX));
        shortestPathBtn.addActionListener(e -> panelGrafos.setMode(PanelGrafos.Mode.SHORTEST_PATH));
        changeWeightBtn.addActionListener(e -> {
            panelGrafos.setMode(PanelGrafos.Mode.CHANGE_WEIGHT);
            // intenta parsear peso
            String s = weightField.getText().trim();
            try {
                int w = Integer.parseInt(s);
                panelGrafos.setRequestedWeight(w);
            } catch (NumberFormatException ex) {
                // no hace nada: panel pedirá peso al crear arista si no hay
            }
        });

        btnAddAll.addActionListener(e -> {
            panelGrafos.addAllEdgesWithRandomWeights(1, 9);
            panelGrafos.repaint();
        });

        btnRandom.addActionListener(e -> {
            panelGrafos.randomizeEdgeWeights(1, 12);
            panelGrafos.repaint();
        });

        btnHelp.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Modo Add Vertex: clic en el área para crear vértices.\n"
                        + "Add Edges: clic en un vértice y luego en otro para crear arista (te pedirá peso).\n"
                        + "Move Vertex: arrastra vértices.\n"
                        + "Shortest Path: selecciona inicio y destino para resaltar camino (usa botón Dijkstra abajo).\n"
                        + "Change Weight: clic en una arista para cambiar su peso (o ingresa peso en la caja)."));

        btnMST.addActionListener(e -> {
            // opcional: resaltar MST (usamos Prim simple)
            panelGrafos.highlightMST();
        });

        botonDijkstra.addActionListener(this::ejecutarDijkstra);

        setVisible(true);
    }

    private void ejecutarDijkstra(ActionEvent event) {
        panelGrafos.limpiarCamino();
        java.util.List<Nodo> todosLosNodos = panelGrafos.getTodosLosNodos();

        if (todosLosNodos.size() < 2) {
            JOptionPane.showMessageDialog(this, "Debe haber al menos dos nodos para calcular un camino.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Nodo inicio = promptParaNodo("Ingrese ID nodo inicio:", todosLosNodos);
        if (inicio == null) return;
        Nodo destino = promptParaNodo("Ingrese ID nodo destino:", todosLosNodos);
        if (destino == null) return;
        if (inicio.equals(destino)) {
            JOptionPane.showMessageDialog(this, "Origen y destino no pueden ser iguales.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.util.Map<Nodo, java.util.List<Arista>> grafo = panelGrafos.getListaAdyacencia();
        java.util.List<Nodo> camino = Dijkstra.encontrarCaminoMasCorto(grafo, inicio, destino);
        if (camino.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontró ruta.", "Sin ruta", JOptionPane.WARNING_MESSAGE);
            return;
        }
        panelGrafos.setCaminoMasCorto(camino);
        // mostrar info
        StringBuilder sb = new StringBuilder("Ruta: ");
        int total = 0;
        for (int i = 0; i < camino.size(); i++) {
            sb.append(camino.get(i).getId());
            if (i < camino.size() - 1) {
                int p = panelGrafos.getPonderacionBetween(camino.get(i), camino.get(i + 1));
                total += p;
                sb.append(" ->(").append(p).append(")-> ");
            }
        }
        JOptionPane.showMessageDialog(this, sb + "\nCosto total: " + total, "Dijkstra", JOptionPane.INFORMATION_MESSAGE);
    }

    private Nodo promptParaNodo(String mensaje, java.util.List<Nodo> todos) {
        String disponibles = "Nodos disponibles: " + todos.stream().map(n -> "" + n.getId()).reduce((a, b) -> a + ", " + b).orElse("");
        String input = JOptionPane.showInputDialog(this, mensaje + "\n" + disponibles);
        if (input == null || input.trim().isEmpty()) return null;
        try {
            int id = Integer.parseInt(input.trim());
            Nodo n = panelGrafos.encontrarNodoPorID(id);
            if (n == null) {
                JOptionPane.showMessageDialog(this, "ID inválido", "Error", JOptionPane.ERROR_MESSAGE);
                return promptParaNodo(mensaje, todos);
            }
            return n;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese número válido", "Error", JOptionPane.ERROR_MESSAGE);
            return promptParaNodo(mensaje, todos);
        }
    }

    //FORMATO DE TEXTO PARA RUTAS
    private String formatearRuta(List<Nodo> ruta) {
        Map<Nodo, List<Arista>> grafo = panelGrafos.getListaAdyacencia();
        StringBuilder sb = new StringBuilder();
        int total = 0;
        for (int i = 0; i < ruta.size() - 1; i++) {
            Nodo actual = ruta.get(i);
            Nodo siguiente = ruta.get(i + 1);
            sb.append(actual.getId());
            List<Arista> lista = grafo.get(actual);
            if (lista != null) {
                for (Arista a : lista) {
                    if (a.getDestino().equals(siguiente)) {
                        total+= a.getPonderacion();
                        sb.append(" -> (").append(a.getPonderacion()).append(") -> ");
                        break;
                    }
                }
            }
        }
        sb.append(ruta.get(ruta.size() - 1).getId());
        sb.append("  | Costo total: ").append(total);
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
