import java.util.*;
public class CaminosAlt {
    public static List<List<Nodo>> CaminosAlt(Map<Nodo, List <Arista>> grafo, Nodo origen, Nodo destino, int K) {
        List<List<Nodo>> caminosAlt = new ArrayList<>();
        List <Nodo> primerCamino = Dijkstra.encontrarCaminoMasCorto(grafo, origen, destino);
        if (primerCamino.isEmpty()) {
            return caminosAlt;
        }
        caminosAlt.add(primerCamino);

        PriorityQueue<List<Nodo>> candidatos = new PriorityQueue<>(Comparator.comparingInt(c -> costoRuta(grafo, c)));

        for (int k = 1; k < K; k++) {
            List<Nodo> caminoPrevio = caminosAlt.get(k - 1);

            for (int i = 0; i < caminoPrevio.size() - 1; i++) {
                Nodo nodoDesv = caminoPrevio.get(i);
                List<Nodo> caminoRaiz = new ArrayList<>(caminoPrevio.subList(0, i + 1));

                Map<Nodo, List<Arista>> grafoTemp = copiarGrafo(grafo);

                for (Nodo n: caminoRaiz) {
                    if (!n.equals(nodoDesv)) {
                        grafoTemp.remove(n);
                    }
                }
                for (List<Nodo> p : caminosAlt) {
                    if (p.size() > i && caminoRaiz.equals(p.subList(0, i + 1))) {
                        Nodo a = p.get(i);
                        Nodo b = p.get(i + 1);
                        grafoTemp.get(a).removeIf(ar -> ar.getDestino().equals(b));
                    }
                }

                List<Nodo> caminoDesv = Dijkstra.encontrarCaminoMasCorto(grafoTemp, nodoDesv, destino);
                if (caminoDesv.isEmpty()) {
                    continue;
                }

                List<Nodo> caminoTotal = new ArrayList<>(caminoRaiz);
                caminoTotal.addAll(caminoDesv.subList(1, caminoDesv.size()));

                if (!candidatos.contains(caminoTotal)) {
                    candidatos.add(caminoTotal);
                }
            }

            if (candidatos.isEmpty()) {
                break;
            }
            caminosAlt.add(candidatos.poll());
        }
        return caminosAlt;
    }
    private static int costoRuta (Map<Nodo, List<Arista>> grafo, List<Nodo> ruta) {
        int total = 0;

        for (int i = 0; i < ruta.size() - 1; i++) {
            Nodo a = ruta.get(i);
            Nodo b = ruta.get(i + 1);

            for (Arista ar : grafo.get(a)) {
                if (ar.getDestino().equals(b)) {
                    total += ar.getPonderacion();
                    break;
                }
            }
        }
        return total;
    }
    private static Map<Nodo, List <Arista>> copiarGrafo (Map<Nodo, List <Arista>> original){
        Map <Nodo, List <Arista>> copia = new HashMap<>();
        for (var e : original.entrySet()){
            copia.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return copia;
    }
}