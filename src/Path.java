public class Path {
    Point prev;
    int direction;
    int dist;

    public Path(Point prev, int direction, int dist) {
        this.prev = prev;
        this.direction = direction;
        this.dist = dist;
    }
}