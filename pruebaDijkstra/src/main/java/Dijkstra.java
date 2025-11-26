import java.util.*;

public class Dijkstra {
    /**
     * Implementa el algoritmo de Dijkstra para encontrar la ruta más corta.
     * @param grafo La lista de adyacencia del grafo.
     * @param origen Nodo de inicio.
     * @param destino Nodo final.
     * @return Una lista de Nodos que representa el camino más corto, o una lista vacía si no hay camino.
     */

    public static List<PanelGrafos.Nodo> encontrarCaminoMasCorto(Map<PanelGrafos.Nodo, List<PanelGrafos.Arista>> grafo, PanelGrafos.Nodo origen, PanelGrafos.Nodo destino){
        Map<PanelGrafos.Nodo, Integer> dist = new HashMap<>();
        Map<PanelGrafos.Nodo, PanelGrafos.Nodo> prev = new HashMap<>();

        // Inicializar distancias a infinito
        for (PanelGrafos.Nodo n : grafo.keySet()) dist.put(n, Integer.MAX_VALUE);
        dist.put(origen, 0);

        // Cola de prioridad para seleccionar el nodo con la menor distancia conocida
        PriorityQueue<NodoDist> pq = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.dist));
        pq.add(new NodoDist(origen, 0));

        while (!pq.isEmpty()) {
            NodoDist nd = pq.poll();
            PanelGrafos.Nodo u = nd.node;
            int d = nd.dist;

            if (d > dist.get(u)) continue; // Ya encontramos un camino más corto

            if (u.equals(destino)) break; // Llegamos al destino

            // Explorar vecinos
            for (PanelGrafos.Arista ar : grafo.getOrDefault(u, Collections.emptyList())) {
                PanelGrafos.Nodo v = ar.getDestino();
                int alt = d + ar.getPonderacion();

                if (alt < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                    dist.put(v, alt);
                    prev.put(v, u); // Guarda el predecesor para reconstruir el camino
                    pq.add(new NodoDist(v, alt));
                }
            }
        }

        // Reconstruir el camino
        LinkedList<PanelGrafos.Nodo> path = new LinkedList<>();
        PanelGrafos.Nodo step = destino;

        // Si el destino no tiene predecesor y no es el origen, no hay camino
        if (!prev.containsKey(step) && !step.equals(origen)) {
            return Collections.emptyList();
        }

        while (step != null) {
            path.addFirst(step);
            step = prev.get(step);
        }
        return path;
    }

    // Clase auxiliar para la cola de prioridad de Dijkstra
    private static class NodoDist {
        final PanelGrafos.Nodo node;
        final int dist;
        NodoDist(PanelGrafos.Nodo n, int d) { node = n; dist = d; }
    }
}