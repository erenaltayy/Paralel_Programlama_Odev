import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        // Sistem bilgilerini yazdır: CPU çekirdek sayısı ve maksimum bellek
        System.out.println("Kullanılabilir İşlemci Sayısı: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Maksimum Bellek: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");

        // Örnek bir poligon (konveks veya konkav olabilir)
        Point[] polygonPoints = {
                new Point(0, 0),
                new Point(5, 0),
                new Point(5, 5),
                new Point(0, 5)
        };
        Polygon polygon = new Polygon(polygonPoints);

        // Test parametreleri: Nokta sayısı ve farklı THRESHOLD değerleri
        int pointCount = 10_000_000; // 10 milyon test noktası
        int[] thresholds = {100, 1000, 10_000, 100_000}; // Farklı eşik değerleri

        Random random = new Random();
        System.out.println("\n" + pointCount + " rastgele nokta oluşturuluyor...");
        Point[] testPoints = generateRandomPoints(pointCount, random);

        // Her THRESHOLD değeri için test yap
        for (int threshold : thresholds) {
            System.out.println("\nTHRESHOLD = " + threshold + ", Nokta Sayısı = " + pointCount + " için test yapılıyor");

            // 1. Paralel işlem süresini ölç
            long startTime = System.nanoTime();
            PointInPolygonTask task = new PointInPolygonTask(polygon, testPoints, 0, testPoints.length, threshold);
            ForkJoinPool pool = ForkJoinPool.commonPool();
            List<Boolean> results = pool.invoke(task);
            long parallelTime = System.nanoTime() - startTime;

            // 2. Seri işlem süresini ölç
            startTime = System.nanoTime();
            List<Boolean> serialResults = computeSerially(polygon, testPoints);
            long serialTime = System.nanoTime() - startTime;

            // Sonuçları doğrula: Paralel ve seri sonuçlar aynı mı?
            boolean resultsMatch = results.equals(serialResults);
            System.out.printf("Paralel İşlem Süresi: %.3f ms%n", parallelTime / 1_000_000.0);
            System.out.printf("Seri İşlem Süresi: %.3f ms%n", serialTime / 1_000_000.0);
            System.out.println("Sonuçlar Eşleşiyor: " + resultsMatch);

            // İlk 5 noktanın sonuçlarını yazdır
            System.out.println("Örnek Sonuçlar (ilk 5 nokta):");
            for (int i = 0; i < Math.min(5, results.size()); i++) {
                System.out.printf("Nokta (%.1f, %.1f): %s%n",
                        testPoints[i].getX(), testPoints[i].getY(),
                        results.get(i) ? "İçinde" : "Dışında");
            }

            // Bellek kullanımını raporla
            System.gc(); // Çöp toplayıcıyı çağır
            long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            System.out.println("Kullanılan Bellek: " + usedMemory + " MB");
        }
    }

    // Rastgele noktalar üret: -5 ile 5 arasında x ve y koordinatları
    private static Point[] generateRandomPoints(int count, Random random) {
        Point[] points = new Point[count];
        for (int i = 0; i < count; i++) {
            double x = random.nextDouble() * 10 - 5; // -5 ile 5 arasında x
            double y = random.nextDouble() * 10 - 5; // -5 ile 5 arasında y
            points[i] = new Point(x, y);
        }
        return points;
    }

    // Seri işlem: Her noktayı sırayla kontrol et
    private static List<Boolean> computeSerially(Polygon polygon, Point[] points) {
        List<Boolean> results = new ArrayList<>();
        Point[] polygonPoints = polygon.getPoints();
        for (Point p : points) {
            int intersectCount = 0; // Kesişim sayısını sıfırla
            for (int j = 0; j < polygonPoints.length; j++) {
                Point p1 = polygonPoints[j];
                Point p2 = polygonPoints[(j + 1) % polygonPoints.length]; // Sonraki noktaya geç
                if (polygon.intersects(p, p1, p2)) { // Işın kenarla kesişiyor mu?
                    intersectCount++;
                }
            }
            results.add(intersectCount % 2 == 1); // Kesişim tekse içeride, çiftse dışarıda
        }
        return results;
    }
}