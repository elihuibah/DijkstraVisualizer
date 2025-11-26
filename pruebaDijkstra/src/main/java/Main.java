import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends JFrame {

    private final PanelGrafos panelGrafos;
    private final Dimension BUTTON_SIZE = new Dimension(240, 42);
    private final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);

    // --- Colores Base ---
    private static final Color BASE_BUTTON_COLOR = new Color(70, 79, 94); // Gris Azulado Elegante
    private static final Color DARK_HOVER_COLOR = new Color(85, 95, 110);
    private static final Color LIGHT_HOVER_COLOR = new Color(55, 64, 79);

    // --- Colores para el Modo Oscuro ---
    private static final Color DARK_BACKGROUND_SOFT = new Color(40, 44, 52); // Fondo del panel de botones
    private static final Color DARKER_SURFACE_TRUE = new Color(50, 56, 68); // Fondo del área de dibujo redondeada
    private static final Color DARK_BUTTON_BG = BASE_BUTTON_COLOR;
    private static final Color TEXT_COLOR_DARK = Color.WHITE; // Texto en panel de botones (radio buttons)

    // --- Colores para el Modo Claro ---
    private static final Color LIGHT_BACKGROUND = new Color(240, 240, 240); // Fondo del panel de botones
    private static final Color LIGHT_SURFACE_TRUE = new Color(255, 255, 255); // Fondo del área de dibujo redondeada
    private static final Color LIGHT_BUTTON_BG = BASE_BUTTON_COLOR;
    private static final Color TEXT_COLOR_LIGHT = new Color(40, 44, 52); // Texto en panel de botones (gris oscuro)

    private Color activeBackground = DARK_BACKGROUND_SOFT;
    private Color activeSurface = DARKER_SURFACE_TRUE;
    private Color activeButtonBg = DARK_BUTTON_BG;
    private Color activeTextColor = TEXT_COLOR_DARK;
    private boolean isDarkMode = true;

    private JPanel leftPanel;
    private RoundedPanel panelDibujo;
    private JPanel bottomPanel;
    private JList<String> listaRutas;
    private JScrollPane listaScroll;
    private JRadioButton[] radioButtons;
    private JButton[] actionButtons;


    public Main() {
        // Configuramos la ventana
        setTitle("Graph GUI — Visualizador de Dijkstra (Modo Oscuro)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(activeBackground);

        // --- Panel Izquierdo: Controles y Botones ---
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(activeBackground);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Creación de Radio Buttons (Modos de interacción)
        JRadioButton addVertexBtn = createRadioStyleButton("Añadir Nodo");
        JRadioButton addEdgeBtn = createRadioStyleButton("Añadir Aristas");
        JRadioButton moveVertexBtn = createRadioStyleButton("Mover Nodo");

        ButtonGroup group = new ButtonGroup();
        group.add(addVertexBtn);
        group.add(addEdgeBtn);
        group.add(moveVertexBtn);

        radioButtons = new JRadioButton[]{addVertexBtn, addEdgeBtn, moveVertexBtn};

        for (JRadioButton btn : radioButtons) {
            btn.setPreferredSize(BUTTON_SIZE);
            btn.setMinimumSize(BUTTON_SIZE);
            btn.setMaximumSize(BUTTON_SIZE);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            leftPanel.add(btn);
            leftPanel.add(Box.createVerticalStrut(8));
        }

        leftPanel.add(Box.createVerticalStrut(12));

        // Creación de Action Buttons (Acciones y Algoritmos)
        JButton botonDijkstra = createButtonStyleButton("Calcular Caminos Mínimos");
        JButton btnAlternativas = createButtonStyleButton("Rutas Alternativas");
        JButton btnAddAll = createButtonStyleButton("Añadir Todas las Aristas");
        JButton btnRandom = createButtonStyleButton("Peso Aleatorio");
        JButton btnDeleteVertex = createButtonStyleButton("Eliminar Nodo");
        JButton btnDeleteEdge = createButtonStyleButton("Eliminar Arista");
        JButton btnCleanGraph = createButtonStyleButton("Limpiar Grafo");
        JButton btnHelp = createButtonStyleButton("Ayuda");
        JButton btnToggleMode = createButtonStyleButton("Modo Claro / Oscuro ■");
        btnToggleMode.addActionListener(this::toggleDisplayMode);

        actionButtons = new JButton[]{botonDijkstra, btnAlternativas, btnAddAll, btnRandom, btnDeleteVertex, btnDeleteEdge, btnCleanGraph, btnHelp, btnToggleMode};

        // Añadir botones de acción al panel
        for (JButton btn : actionButtons) {
            btn.setPreferredSize(BUTTON_SIZE);
            btn.setMinimumSize(BUTTON_SIZE);
            btn.setMaximumSize(BUTTON_SIZE);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        leftPanel.add(botonDijkstra);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(btnAlternativas);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnAddAll);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnRandom);
        leftPanel.add(Box.createVerticalStrut(20));

        leftPanel.add(btnDeleteVertex);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnDeleteEdge);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnCleanGraph);

        leftPanel.add(Box.createVerticalGlue());

        leftPanel.add(btnHelp);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(btnToggleMode);
        leftPanel.add(Box.createVerticalStrut(10));

        add(leftPanel, BorderLayout.WEST);

        // --- Panel Central: Área de Dibujo del Grafo (RoundedPanel) ---
        panelDibujo = new RoundedPanel(40);
        panelDibujo.setLayout(new BorderLayout());
        panelDibujo.setBackground(activeSurface);
        panelDibujo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelGrafos = new PanelGrafos();
        panelGrafos.setOpaque(false);
        panelDibujo.add(panelGrafos, BorderLayout.CENTER);

        add(panelDibujo, BorderLayout.CENTER);

        // --- Panel Inferior: Lista de Rutas ---
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(activeBackground);
        add(bottomPanel, BorderLayout.SOUTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        listaRutas = new JList<>(listModel);
        listaRutas.setBackground(activeSurface);
        listaRutas.setForeground(activeTextColor);

        listaScroll = new JScrollPane(listaRutas);
        listaScroll.setPreferredSize(new Dimension(600, 100));

        bottomPanel.add(listaScroll);
        add(bottomPanel, BorderLayout.SOUTH);
        panelGrafos.setLogListModel(listModel);

        // --- Conexión de Eventos ---
        addVertexBtn.setSelected(true);
        panelGrafos.setMode(PanelGrafos.Mode.ADD_VERTEX);
        panelGrafos.setDarkMode(isDarkMode);

        addVertexBtn.addActionListener(e -> panelGrafos.setMode(PanelGrafos.Mode.ADD_VERTEX));
        addEdgeBtn.addActionListener(e -> panelGrafos.setMode(PanelGrafos.Mode.ADD_EDGE));
        moveVertexBtn.addActionListener(e -> panelGrafos.setMode(PanelGrafos.Mode.MOVE_VERTEX));

        // Eventos de botones de acción...
        btnAlternativas.addActionListener(this::ejecutarRutasAlternativas);
        listaRutas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int indice = listaRutas.getSelectedIndex();
                if (indice >= 0 && panelGrafos.getRutasAlternativas() != null && panelGrafos.getRutasAlternativas().size() > 0) {
                    panelGrafos.setRutaSeleccionada(panelGrafos.getRutasAlternativas().get(indice));
                }
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
                "**FUNCIONES DEL VISUALIZADOR DE DIJKSTRA**\n\n"
                        + "### MODOS DE INTERACCIÓN:\n"
                        + "* **Añadir Nodo**: Haz clic en el panel central para crear nuevos nodos (círculos).\n"
                        + "* **Añadir Aristas**: Haz clic en un nodo y luego en otro. Se te pedirá ingresar el **peso** (ponderación) de la arista.\n"
                        + "* **Mover Nodo**: Haz clic y arrastra un nodo para reubicarlo en el panel.\n\n"
                        + "### ALGORITMOS Y HERRAMIENTAS:\n"
                        + "* **Caminos Mínimos**: Calcula el camino más corto entre dos nodos que tú elijas con el algoritmo de Dijkstra. Muestra la ruta y el costo total.\n"
                        + "* **Rutas Alternativas**: Calcula y muestra hasta 5 rutas diferentes entre dos nodos, listándolas en el panel inferior.\n"
                        + "* **Añadir Todas las Aristas**: Conecta cada nodo con todos los demás, asignando pesos aleatorios.\n"
                        + "* **Peso Aleatorio**: Asigna un nuevo peso aleatorio a todas las aristas existentes.\n"
                        + "* **Eliminar Nodo**: Abre un diálogo para seleccionar un nodo por su ID y eliminarlo (y sus aristas conectadas).\n"
                        + "* **Eliminar Arista**: Abre un diálogo para seleccionar una arista por sus nodos de conexión y eliminarla.\n"
                        + "* **Limpiar Grafo**: Elimina todos los nodos y aristas del panel.\n"
                        + "* **Modo Claro / Oscuro**: Cambia el esquema de colores de la aplicación.",
                "Ayuda y Descripción de Botones", JOptionPane.INFORMATION_MESSAGE));

        botonDijkstra.addActionListener(this::ejecutarDijkstra);
        btnDeleteVertex.addActionListener(this::ejecutarEliminarVertice);
        btnDeleteEdge.addActionListener(this::ejecutarEliminarArista);
        btnCleanGraph.addActionListener(this::ejecutarLimpiarGrafo);

        setVisible(true);
    }

    // --- Lógica de Manejo de Modos y Botones ---

    private void toggleDisplayMode(ActionEvent e) {
        isDarkMode = !isDarkMode;

        if (isDarkMode) {
            activeBackground = DARK_BACKGROUND_SOFT;
            activeSurface = DARKER_SURFACE_TRUE;
            activeButtonBg = DARK_BUTTON_BG;
            activeTextColor = TEXT_COLOR_DARK;
            setTitle("Graph GUI — Visualizador de Dijkstra (Modo Oscuro)");
        } else {
            activeBackground = LIGHT_BACKGROUND;
            activeSurface = LIGHT_SURFACE_TRUE;
            activeButtonBg = LIGHT_BUTTON_BG;
            activeTextColor = TEXT_COLOR_LIGHT;
            setTitle("Graph GUI — Visualizador de Dijkstra (Modo Claro)");
        }

        // Aplicar nuevos colores
        getContentPane().setBackground(activeBackground);
        leftPanel.setBackground(activeBackground);
        bottomPanel.setBackground(activeBackground);
        panelDibujo.setBackground(activeSurface);
        panelDibujo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (JRadioButton btn : radioButtons) {
            btn.setForeground(isDarkMode ? TEXT_COLOR_DARK : TEXT_COLOR_LIGHT);
            btn.setBackground(activeBackground);
        }

        for (JButton btn : actionButtons) {
            btn.setBackground(activeButtonBg);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activeButtonBg.darker(), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }

        listaRutas.setBackground(activeSurface);
        listaRutas.setForeground(activeTextColor);
        listaScroll.setBorder(BorderFactory.createLineBorder(activeButtonBg.darker(), 1));

        panelGrafos.setDarkMode(isDarkMode);

        revalidate();
        repaint();
    }

    private JButton createButtonStyleButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(activeButtonBg);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activeButtonBg.darker(), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // --- Implementación del efecto HOVER ---
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(isDarkMode ? DARK_HOVER_COLOR : LIGHT_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(activeButtonBg);
            }
        });

        return btn;
    }

    private JRadioButton createRadioStyleButton(String text) {
        JRadioButton btn = new JRadioButton(text);
        btn.setForeground(isDarkMode ? TEXT_COLOR_DARK : TEXT_COLOR_LIGHT);
        btn.setBackground(activeBackground);
        btn.setFont(BUTTON_FONT.deriveFont(Font.BOLD, 16f));
        btn.setFocusPainted(false);
        return btn;
    }

    // --- Lógica de Algoritmos ---

    private void ejecutarDijkstra(ActionEvent event) {
        panelGrafos.setMode(PanelGrafos.Mode.SHORTEST_PATH);
        List<PanelGrafos.Nodo> todosLosNodos = panelGrafos.getTodosLosNodos();

        if (todosLosNodos.size() < 2) {
            JOptionPane.showMessageDialog(this, "Debe haber al menos dos nodos para calcular un camino.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PanelGrafos.Nodo inicio = promptParaNodo("Ingrese ID del nodo de inicio:", todosLosNodos);
        if (inicio == null) return;
        PanelGrafos.Nodo destino = promptParaNodo("Ingrese ID del nodo de destino:", todosLosNodos);
        if (destino == null || inicio.equals(destino)) {
            if (destino != null) JOptionPane.showMessageDialog(this, "Origen y destino no pueden ser iguales.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<PanelGrafos.Nodo, List<PanelGrafos.Arista>> grafo = panelGrafos.getListaAdyacencia();
        List<PanelGrafos.Nodo> camino = Dijkstra.encontrarCaminoMasCorto(grafo, inicio, destino);

        if (camino.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontró ruta.", "Sin ruta", JOptionPane.WARNING_MESSAGE);
            return;
        }
        panelGrafos.setCaminoMasCorto(camino);

        StringBuilder sb = new StringBuilder("Ruta: ");
        int total = 0;
        for (int i = 0; i < camino.size(); i++) {
            sb.append(camino.get(i).getId());
            if (i < camino.size() - 1) {
                int p = panelGrafos.getPonderacionBetween(camino.get(i), camino.get(i + 1));
                total += p;
                sb.append(" -→(").append(p).append(")-→ ");
            }

        }
        JOptionPane.showMessageDialog(this, sb + "\nCosto total: " + total, "Dijkstra", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ejecutarRutasAlternativas(ActionEvent ev) {
        panelGrafos.limpiarCamino();
        List<PanelGrafos.Nodo> nodos = panelGrafos.getTodosLosNodos();

        PanelGrafos.Nodo inicio = promptParaNodo("Inicio:", nodos);
        if (inicio == null) { return; }
        PanelGrafos.Nodo destino = promptParaNodo("Destino:", nodos);
        if (destino == null || inicio.equals(destino)) {
            if (destino != null) JOptionPane.showMessageDialog(this, "Los nodos origen y destino no pueden ser iguales.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<PanelGrafos.Nodo, List<PanelGrafos.Arista>> g = panelGrafos.getListaAdyacencia();

        // Limpia y calcula rutas
        List<List<PanelGrafos.Nodo>> rutas = CaminosAlt.CaminosAlt(g, inicio, destino, 100);
        ((DefaultListModel<String>)listaRutas.getModel()).clear();

        if (rutas.isEmpty()){
            ((DefaultListModel<String>)listaRutas.getModel()).addElement("No se encontraron rutas alternativas.");
        } else {
            for (int i = 0; i < rutas.size(); i++) {
                String texto = "Ruta " + (i + 1) + ": " + formatearRuta(rutas.get(i));
                ((DefaultListModel<String>)listaRutas.getModel()).addElement(texto);
            }
        }
        panelGrafos.setRutasAlternativas(rutas);
    }

    // --- Lógica de Herramientas ---

    private void ejecutarEliminarVertice(ActionEvent e) {
        List<PanelGrafos.Nodo> nodos = panelGrafos.getTodosLosNodos();
        if (nodos.isEmpty()){
            JOptionPane.showMessageDialog(this, "No hay vértices para eliminar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        PanelGrafos.Nodo seleccionado = (PanelGrafos.Nodo) JOptionPane.showInputDialog(this, "Selecciona el vértice a eliminar",
                "Eliminar vértice", JOptionPane.PLAIN_MESSAGE, null, nodos.toArray(), nodos.get(0));
        if (seleccionado == null)return;

        // obtener una lista de aristas únicas (A->B donde A.id < B.id)
        List<PanelGrafos.Arista> aristasUnicas = panelGrafos.getTodasLasAristas().stream()
                .filter(ar -> ar.getOrigen().getId() < ar.getDestino().getId()).collect(Collectors.toList());

        // filtrar solo las aristas conectadas al nodo seleccionado
        List<PanelGrafos.Arista> aristasAfectadas = aristasUnicas.stream()
                .filter(ar -> ar.getOrigen().equals(seleccionado) || ar.getDestino().equals(seleccionado)).collect(Collectors.toList());

        // construir mensaje de confirmación
        StringBuilder message = new StringBuilder();
        message.append("¿Estás seguro de eliminar el vértice ").append(seleccionado.getId()).append("?\n\n");

        if (!aristasAfectadas.isEmpty()) {
            message.append("Las siguientes aristas también serán eliminadas:\n");
            for (PanelGrafos.Arista ar : aristasAfectadas) {
                message.append("  - Arista (Nodo ").append(ar.getOrigen().getId()).append(" - Nodo ").append(ar.getDestino().getId())
                        .append(") con peso: ").append(ar.getPonderacion()).append("\n");
            }
        } else {
            message.append("Este vértice no tiene aristas conectadas. Solo se eliminará el nodo.");
        }

        // mostrar confirmación y ejecutar
        int confirm = JOptionPane.showConfirmDialog(this, message.toString(), "Confirmar eliminación de vértice",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            panelGrafos.limpiarCamino();
            panelGrafos.eliminarNodo(seleccionado);
            panelGrafos.repaint();
        }
    }
    private void ejecutarEliminarArista(ActionEvent e) {
        List<PanelGrafos.Arista> aristas = panelGrafos.getTodasLasAristas().stream()
                .filter(ar -> ar.getOrigen().getId() < ar.getDestino().getId()).collect(Collectors.toList());

        if (aristas.isEmpty()){
            JOptionPane.showMessageDialog(this,"No hay aristas para eliminar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] options = aristas.stream().map(ar -> ar.getOrigen().getId() + " - " + ar.getDestino().getId() + " (" + ar.getPonderacion() + ")").toArray();

        Object seleccionString = JOptionPane.showInputDialog(this, "Selecciona la arista a eliminar", "Eliminar arista",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (seleccionString == null)return;

        int index = List.of(options).indexOf(seleccionString);
        PanelGrafos.Arista seleccionada = aristas.get(index);

        String mensaje = "¿Estás seguro de eliminar la arista seleccionada?\n\n"+
                "Detalles de la arista a eliminar:\n"+
                " - Origen: Nodo "+seleccionada.getOrigen().getId()+"\n"+
                " - Destino: Nodo "+seleccionada.getDestino().getId()+"\n"+
                " - Ponderación: "+seleccionada.getPonderacion();
        int confirmar = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar eliminación de arista",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmar == JOptionPane.YES_OPTION) {
            panelGrafos.limpiarCamino();
            panelGrafos.eliminarArista(seleccionada);
            panelGrafos.repaint();
        }
    }

    private void ejecutarLimpiarGrafo(ActionEvent e) {
        int numNodos = panelGrafos.getTodosLosNodos().size();
        if (numNodos == 0){
            JOptionPane.showMessageDialog(this, "El grafo ya está vacío.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String mensaje = "¿Estás seguro de que quieres eliminar TODOS los "+numNodos+" vértices y aristas del grafo?\n"+
                "ESTA ACCIÓN ES IRREVERSIBLE.";

        int confirmar = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar limpieza de grafo.", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmar == JOptionPane.YES_OPTION){
            panelGrafos.limpiarGrafo();
            panelGrafos.repaint();
        }
    }

    // --- Métodos Auxiliares ---

    private PanelGrafos.Nodo promptParaNodo(String mensaje, List<PanelGrafos.Nodo> todos) {
        String disponibles = "Nodos disponibles: " + todos.stream().map(n -> "" + n.getId()).collect(Collectors.joining(", "));

        // Ajuste temporal de JOptionPane para el modo de color
        Object originalBg = UIManager.get("OptionPane.background");
        Object originalPanelBg = UIManager.get("Panel.background");
        Object originalFg = UIManager.get("OptionPane.messageForeground");

        UIManager.put("OptionPane.background", activeBackground);
        UIManager.put("Panel.background", activeBackground);
        UIManager.put("OptionPane.messageForeground", activeTextColor);

        String input = JOptionPane.showInputDialog(this, mensaje + "\n" + disponibles);

        // Restaurar la apariencia
        UIManager.put("OptionPane.background", originalBg);
        UIManager.put("Panel.background", originalPanelBg);
        UIManager.put("OptionPane.messageForeground", originalFg);


        if (input == null || input.trim().isEmpty()) return null;
        try {
            int id = Integer.parseInt(input.trim());
            PanelGrafos.Nodo n = panelGrafos.encontrarNodoPorID(id);
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

    private String formatearRuta(List<PanelGrafos.Nodo> ruta) {
        StringBuilder sb = new StringBuilder();
        int total = 0;
        for (int i = 0; i < ruta.size() - 1; i++) {
            PanelGrafos.Nodo actual = ruta.get(i);
            PanelGrafos.Nodo siguiente = ruta.get(i + 1);
            sb.append(actual.getId());
            int p = panelGrafos.getPonderacionBetween(actual, siguiente);
            if (p != -1) {
                total+= p;
                sb.append(" -→ (").append(p).append(") -→ ");
            } else {
                sb.append(" -→ (?) -→ ");
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