import java.util.*;
import java.util.stream.Collectors;

// Implementación básica del algoritmo de Yen para encontrar los K caminos más cortos (rutas alternativas).

public class CaminosAlt {

    // Usamos los tipos de PanelGrafos para consistencia
    private static class Nodo extends PanelGrafos.Nodo { public Nodo(int x, int y) { super(x, y); } }
    private static class Arista extends PanelGrafos.Arista { public Arista(PanelGrafos.Nodo o, PanelGrafos.Nodo d, int p) { super(o, d, p); } }


    /**
     * Encuentra K rutas alternativas usando una adaptación del algoritmo de Yen.
     * @param grafo El grafo.
     * @param origen Nodo de inicio.
     * @param destino Nodo final.
     * @param K Número máximo de rutas a encontrar.
     * @return Lista de rutas (cada ruta es una lista de Nodos).
     */
    public static List<List<PanelGrafos.Nodo>> CaminosAlt(Map<PanelGrafos.Nodo, List<PanelGrafos.Arista>> grafo, PanelGrafos.Nodo origen, PanelGrafos.Nodo destino, int K) {
        List<List<PanelGrafos.Nodo>> caminosAlt = new ArrayList<>();

        // 1. Encontrar el primer camino más corto (P1)
        List<PanelGrafos.Nodo> primerCamino = Dijkstra.encontrarCaminoMasCorto(grafo, origen, destino);
        if (primerCamino.isEmpty()) {
            return caminosAlt;
        }
        caminosAlt.add(primerCamino);

        // Cola de prioridad para almacenar rutas candidatas (ordenadas por costo total)
        PriorityQueue<List<PanelGrafos.Nodo>> candidatos = new PriorityQueue<>(
                Comparator.comparingInt(c -> costoRuta(grafo, c))
        );

        for (int k = 1; k < K; k++) {
            List<PanelGrafos.Nodo> caminoPrevio = caminosAlt.get(k - 1);

            // 2. Iterar sobre cada nodo del camino previo para encontrar caminos de desvío
            for (int i = 0; i < caminoPrevio.size() - 1; i++) {
                PanelGrafos.Nodo nodoDesv = caminoPrevio.get(i);
                // La raíz es el subcamino desde el origen hasta el nodo de desvío
                List<PanelGrafos.Nodo> caminoRaiz = new ArrayList<>(caminoPrevio.subList(0, i + 1));

                // 3. Crear un grafo temporal para calcular el camino de desvío
                Map<PanelGrafos.Nodo, List<PanelGrafos.Arista>> grafoTemp = copiarGrafo(grafo);

                // Eliminar las aristas de la raíz de los K-1 caminos ya encontrados
                for (List<PanelGrafos.Nodo> p : caminosAlt) {
                    // Si el camino p tiene la misma raíz que el camino actual
                    if (p.size() > i && caminoRaiz.equals(p.subList(0, i + 1))) {
                        PanelGrafos.Nodo a = p.get(i);
                        PanelGrafos.Nodo b = p.get(i + 1);
                        // Eliminar la arista (a -> b) del grafo temporal
                        grafoTemp.get(a).removeIf(ar -> ar.getDestino().equals(b));

                        // También eliminar la arista simétrica (B -> A) si el grafo es no dirigido
                        grafoTemp.get(b).removeIf(ar -> ar.getDestino().equals(a));
                    }
                }

                // Eliminar temporalmente los nodos en la raíz (excepto el nodo de desvío)
                for (PanelGrafos.Nodo n : caminoRaiz) {
                    if (!n.equals(nodoDesv)) {
                        grafoTemp.remove(n);
                        // Asegurar que ninguna arista apunte a 'n'
                        for (List<PanelGrafos.Arista> aristasList : grafoTemp.values()) {
                            aristasList.removeIf(ar -> ar.getDestino().equals(n));
                        }
                    }
                }

                // 4. Calcular el camino más corto del nodo de desvío al destino en el grafo temporal
                List<PanelGrafos.Nodo> caminoDesv = Dijkstra.encontrarCaminoMasCorto(grafoTemp, nodoDesv, destino);

                if (caminoDesv.isEmpty() || caminoDesv.size() < 2) {
                    continue; // No se encontró camino de desvío
                }

                // 5. Concatenar la raíz y el desvío para formar el camino total
                List<PanelGrafos.Nodo> caminoTotal = new ArrayList<>(caminoRaiz);
                caminoTotal.addAll(caminoDesv.subList(1, caminoDesv.size())); // Añadir desvío, excluyendo el nodoDesv

                // 6. Añadir el nuevo camino a los candidatos si no existe
                if (!contieneRuta(candidatos, caminoTotal) && !contieneRuta(caminosAlt, caminoTotal)) {
                    candidatos.add(caminoTotal);
                }
            }

            // 7. Si no hay candidatos, terminamos
            if (candidatos.isEmpty()) {
                break;
            }

            // 8. Seleccionar el mejor candidato (el de menor costo)
            caminosAlt.add(candidatos.poll());
        }
        return caminosAlt;
    }

    /**
     * Calcula el costo total de una ruta.
     */
    private static int costoRuta (Map<PanelGrafos.Nodo, List<PanelGrafos.Arista>> grafo, List<PanelGrafos.Nodo> ruta) {
        int total = 0;
        for (int i = 0; i < ruta.size() - 1; i++) {
            PanelGrafos.Nodo a = ruta.get(i);
            PanelGrafos.Nodo b = ruta.get(i + 1);

            // Buscar la ponderación de la arista (a -> b)
            Optional<PanelGrafos.Arista> aristaOpt = grafo.getOrDefault(a, Collections.emptyList()).stream()
                    .filter(ar -> ar.getDestino().equals(b))
                    .findFirst();

            if (aristaOpt.isPresent()) {
                total += aristaOpt.get().getPonderacion();
            } else {
                // Si la arista no existe, el camino es inválido o tiene un costo infinito
                return Integer.MAX_VALUE;
            }
        }
        return total;
    }

    /**
     * Crea una copia profunda del grafo para modificaciones temporales.
     */
    private static Map<PanelGrafos.Nodo, List <PanelGrafos.Arista>> copiarGrafo (Map<PanelGrafos.Nodo, List <PanelGrafos.Arista>> original){
        Map <PanelGrafos.Nodo, List <PanelGrafos.Arista>> copia = new HashMap<>();
        for (var e : original.entrySet()){
            // Copia la lista de aristas para que las modificaciones no afecten al original
            copia.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return copia;
    }

    /**
     * Verifica si una ruta ya existe en una colección de rutas.
     */
    private static boolean contieneRuta(Collection<List<PanelGrafos.Nodo>> rutas, List<PanelGrafos.Nodo> ruta) {
        // La comparación de rutas se hace comparando las listas de nodos
        return rutas.stream().anyMatch(r -> r.equals(ruta));
    }
}