import javafx.scene.chart.PieChart;
import java.util.*;

public class ChartUtil {
    public static void updatePieChart(PieChart chart, List<Record> records) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Record r : records) {
            categoryTotals.put(r.category, categoryTotals.getOrDefault(r.category, 0.0) + r.amount);
        }
        chart.getData().clear();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            chart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }
}
