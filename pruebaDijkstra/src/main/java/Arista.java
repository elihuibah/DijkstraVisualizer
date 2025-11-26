public class Arista {
    private final Nodo origen;
    private final Nodo destino;
    private int ponderacion;

    public Arista(Nodo origen, Nodo destino, int ponderacion) {
        this.origen = origen;
        this.destino = destino;
        this.ponderacion = ponderacion;
    }

    public Nodo getOrigen() { return origen; }
    public Nodo getDestino() { return destino; }
    public int getPonderacion() { return ponderacion; }
    public void setPonderacion(int ponderacion){
        this.ponderacion = ponderacion;
    }
    @Override
    public String toString(){
        return "Nodo "+origen.getId()+" -> ("+ponderacion+") -> Nodo "+destino.getId();
    }

}
