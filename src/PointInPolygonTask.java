import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class PointInPolygonTask extends RecursiveTask<List<Boolean>> {
    private final int threshold; // Her görev için özel THRESHOLD
    private final Polygon polygon;
    private final Point[] points;
    private final int start;
    private final int end;

    public PointInPolygonTask(Polygon polygon, Point[] points, int start, int end, int threshold) {
        this.polygon = polygon;
        this.points = points;
        this.start = start;
        this.end = end;
        this.threshold = threshold;
    }

    @Override
    protected List<Boolean> compute() {
        if (end - start <= threshold) {
            // Küçük görev: doğrudan hesapla
            List<Boolean> result = new ArrayList<>();
            Point[] polygonPoints = polygon.getPoints();
            for (int i = start; i < end; i++) {
                Point p = points[i];
                int intersectCount = 0;
                for (int j = 0; j < polygonPoints.length; j++) {
                    Point p1 = polygonPoints[j];
                    Point p2Next = polygonPoints[(j + 1) % polygonPoints.length];
                    if (polygon.intersects(p, p1, p2Next)) {
                        intersectCount++;
                    }
                }
                result.add(intersectCount % 2 == 1);
            }
            return result;
        } else {
            // Görevi böl
            int mid = (start + end) / 2;
            PointInPolygonTask leftTask = new PointInPolygonTask(polygon, points, start, mid, threshold);
            PointInPolygonTask rightTask = new PointInPolygonTask(polygon, points, mid, end, threshold);

            invokeAll(leftTask, rightTask);

            List<Boolean> result = new ArrayList<>();
            result.addAll(leftTask.join());
            result.addAll(rightTask.join());
            return result;
        }
    }
}