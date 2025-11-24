import java.awt.*;
public class Nodo {
    private static int nextId = 0;
    private final int id;
    private final Point posicion;

    public Nodo(int x, int y) {
        this.id = nextId++;
        this.posicion = new Point(x, y);
    }

    public int getId() { return id; }
    public Point getPosicion() { return posicion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nodo)) return false;
        Nodo nodo = (Nodo) o;
        return id == nodo.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
