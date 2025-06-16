public class Polygon {
    private final Point[] points;

    public Polygon(Point[] points) {
        this.points = points;
    }

    public Point[] getPoints() {
        return points;
    }

    // Bir kenarın (p1, p2) yatay ışın ile kesişimini kontrol eder
    public boolean intersects(Point p, Point p1, Point p2) {
        if ((p1.getY() > p.getY()) != (p2.getY() > p.getY())) {
            double xinters = (p.getY() - p1.getY()) * (p2.getX() - p1.getX()) / (p2.getY() - p1.getY()) + p1.getX();
            if (p.getX() < xinters) {
                return true;
            }
        }
        return false;
    }
}