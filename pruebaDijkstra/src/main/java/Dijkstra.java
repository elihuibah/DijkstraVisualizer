import java.util.*;

public class Dijkstra {
    public static List<Nodo> encontrarCaminoMasCorto(Map<Nodo, List<Arista>> grafo, Nodo origen, Nodo destino){
        Map<Nodo, Integer> dist = new HashMap<>();
        Map<Nodo, Nodo> prev = new HashMap<>();
        for (Nodo n : grafo.keySet()) dist.put(n, Integer.MAX_VALUE);
        dist.put(origen, 0);
        PriorityQueue<NodoDist> pq = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.dist));
        pq.add(new NodoDist(origen, 0));

        while (!pq.isEmpty()) {
            NodoDist nd = pq.poll();
            Nodo u = nd.node;
            int d = nd.dist;
            if (d > dist.get(u)) continue;
            if (u.equals(destino)) break;
            for (Arista ar : grafo.getOrDefault(u, Collections.emptyList())) {
                Nodo v = ar.getDestino();
                int alt = d + ar.getPonderacion();
                if (alt < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(new NodoDist(v, alt));
                }
            }
        }

        // reconstruir
        LinkedList<Nodo> path = new LinkedList<>();
        Nodo step = destino;
        if (!prev.containsKey(step) && !step.equals(origen)) {
            return Collections.emptyList();
        }
        while (step != null) {
            path.addFirst(step);
            step = prev.get(step);
        }
        return path;
    }

    private static class NodoDist {
        final Nodo node;
        final int dist;
        NodoDist(Nodo n, int d) { node = n; dist = d; }
    }
}



