import java.util.Objects;

public class Point {
    int x, y, z;

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x &&
                y == point.y &&
                z == point.z;
    }

    @Override
    public int hashCode() {

        return Objects.hash(x, y, z);
    }

    Point add(int dir, World w) {
        Point res = new Point(x + Utils.dx[dir], y + Utils.dy[dir], z + Utils.dz[dir]);
        res.makeOk(w);
        return res;
    }

    void makeOk(World w) {
        x = (x + w.xmax) % w.xmax;
        y = (y + w.ymax) % w.ymax;
        z = (z + w.zmax) % w.zmax;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ") ";
    }
}
