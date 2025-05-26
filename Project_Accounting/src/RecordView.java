import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;


public class RecordView {
    private ListView<String> recordList = new ListView<>();

    public BorderPane getView(PieChart pieChart) {
        VBox inputBox = new VBox(10);
        inputBox.setPadding(new Insets(10));

        DatePicker datePicker = new DatePicker();
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("食物", "生活用品", "衣服", "通勤", "其他");
        TextField amountField = new TextField();
        amountField.setPromptText("請輸入金額");

        Button submitBtn = new Button("送出");
        submitBtn.setOnAction(e -> {
            try {
                String date = datePicker.getValue().toString();
                String category = categoryBox.getValue();
                double amount = Double.parseDouble(amountField.getText());

                Record newRecord = new Record(date, category, amount);
                Project_Accounting.records.add(newRecord);
                recordList.getItems().add(newRecord.toString());
                ChartUtil.updatePieChart(pieChart, Project_Accounting.records);
            } catch (Exception ignored) {}
        });

        HBox inputRow = new HBox(10, new Label("日期:"), datePicker,
                new Label("類別:"), categoryBox,
                new Label("金額:"), amountField, submitBtn);

        BorderPane layout = new BorderPane();
        layout.setTop(inputRow);
        layout.setCenter(recordList);
        layout.setRight(pieChart);
        return layout;
    }
}
