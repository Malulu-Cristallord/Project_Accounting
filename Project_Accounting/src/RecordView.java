import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RecordView {
    private ListView<String> recordList = new ListView<>();
    private VBox summaryBox = new VBox(5); 

    public BorderPane getView(PieChart pieChart, String name) {
    	
    	String fileName = "data/" + name + "Record.txt";
    	fileCheck(fileName);
    	loadRecordsFromFile(fileName);
    	DatePicker datePicker = new DatePicker();
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("食物", "日常確幸", "服飾", "欠款", "通勤", "其他");

        TextField itemField = new TextField();
        itemField.setPromptText("請輸入項目名稱");

        TextField amountField = new TextField();
        amountField.setPromptText("請輸入金額");

        Button submitBtn = new Button("送出");
        
        Button deleteCategoryBtn = new Button("刪除類別紀錄");
        deleteCategoryBtn.setOnAction(e -> {
            String selectedCategory = categoryBox.getValue();
            if (selectedCategory != null) {
                Project_Accounting.records.removeIf(r -> r.category.equals(selectedCategory));
                recordList.getItems().removeIf(s -> s.contains("| " + selectedCategory + " |"));
                updatePieChartWithSummary(pieChart, Project_Accounting.records);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "請選擇要刪除的類別！");
                alert.showAndWait();
            }
        });

        HBox inputRow = new HBox(10,
                new Label("日期:"), datePicker,
                new Label("類別:"), categoryBox);
        
        VBox inputV = new VBox(10,
                new Label("項目:"), itemField,
                new Label("金額:"), amountField, submitBtn, deleteCategoryBtn);
        
        inputRow.setPadding(new Insets(10));

        submitBtn.setOnAction(e -> {
            try {
                String date = datePicker.getValue().toString();
                String category = categoryBox.getValue();
                String itemName = itemField.getText();
                double amount = Double.parseDouble(amountField.getText());

                Record newRecord = new Record(date, category, itemName, amount);
                Project_Accounting.records.add(newRecord);
                writeFile(fileName, date, category, itemName, amount);
                recordList.getItems().add(newRecord.toString());

                // 新增功能：支出預警（當該類別支出超過所有支出80%時）
                double totalAmount = Project_Accounting.records.stream()
                        .mapToDouble(r -> r.amount)
                        .sum();
                double categoryAmount = Project_Accounting.records.stream()
                        .filter(r -> r.category.equals(category))
                        .mapToDouble(r -> r.amount)
                        .sum();

                double warningThreshold = 0.8; // 50%
                if (totalAmount > 0 && (categoryAmount / totalAmount) > warningThreshold) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("支出預警");
                    alert.setHeaderText(null);
                    alert.setContentText("警告！類別「" + category + "」的支出已超過總支出的 80%！");
                    alert.showAndWait();
                }

                updatePieChartWithSummary(pieChart, Project_Accounting.records);
            } catch (Exception ex) {
                Parent parent = inputRow.getParent();
                if (parent instanceof Pane pane) {
                    pane.getChildren().remove(inputRow);
                }
            }
        });
        
        BorderPane layout = new BorderPane();
        layout.setTop(inputRow);
        layout.setCenter(recordList);

        recordList.setPrefWidth(400);  

        VBox rightPanel = new VBox(10, inputV, pieChart, summaryBox);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(300);  
        layout.setRight(rightPanel);
        updatePieChartWithSummary(pieChart, Project_Accounting.records);

        return layout;
    }

    private void updatePieChartWithSummary(PieChart chart, List<Record> records) {
        try {
            Map<String, Double> categoryTotals = new HashMap<>();
            for (Record r : records) {
                categoryTotals.put(r.category, categoryTotals.getOrDefault(r.category, 0.0) + r.amount);
            }

            chart.getData().clear();
            double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total == 0) total = 1; 

            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                double percentage = (entry.getValue() / total) * 100;
                chart.getData().add(new PieChart.Data(entry.getKey() + " " + String.format("%.1f%%", percentage), entry.getValue()));                
            }

            summaryBox.getChildren().clear();
            summaryBox.getChildren().add(new Label("各類別總額：" + total));
            for (String category : Arrays.asList("吃的", "日常確幸", "服飾", "欠款", "通勤", "其他")) {
                double sum = categoryTotals.getOrDefault(category, 0.0);
                summaryBox.getChildren().add(new Label(category + ": " + sum + " 元"));
            }

        } catch (Exception e) {
            chart.setTitle("資料錯誤：更新失敗但仍保留之前的資料");
        }
    }
    
    private void loadRecordsFromFile(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    recordList.getItems().add(line);
                    
                    // Optionally reconstruct Record objects for pie chart tracking
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 4) {
                        String date = parts[0];
                        String category = parts[1];
                        String itemName = parts[2];
                        double amount = Double.parseDouble(parts[3]);

                        Project_Accounting.records.add(new Record(date, category, itemName, amount));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    
    private void writeFile(String fileName, String date, String category, String itemName, double amount) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(String.format("%-20s%-20s%-20s%-20s%n", date, category, itemName, amount));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void fileCheck(String fileName) {
    	File file = new File(fileName);
    	if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Save file not found. New file created.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println("Save file found.");
        }
    }
}