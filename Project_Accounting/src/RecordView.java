import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;



public class RecordView {
    private ListView<String> recordList = new ListView<>();
    private String fileName = " ";
    public GridPane getView(PieChart pieChart, String name) {

        fileName = name + "Record.txt";
        fileCheck(fileName);

        DatePicker datePicker = new DatePicker();
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("食物", "生活用品", "衣服", "通勤", "其他");

        TextField amountField = new TextField();
        amountField.setPromptText("請輸入金額");

        Button submitBtn = new Button("送出");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefWidth(300);
        outputArea.setPrefHeight(400);

        // Record list (placed under input row)
        recordList.setPrefHeight(200);

        submitBtn.setOnAction(e -> {
            try {
                String date = datePicker.getValue().toString();
                String category = categoryBox.getValue();
                double amount = Double.parseDouble(amountField.getText());

                Record newRecord = new Record(date, category, amount);
                Project_Accounting.records.add(newRecord);
                recordList.getItems().add(newRecord.toString());

                outputArea.appendText(newRecord.toString() + "\n"); // Optional
                ChartUtil.updatePieChart(pieChart, Project_Accounting.records);
            } catch (Exception ex) {
            	showAlert(AlertType.INFORMATION,"輸入錯誤，請檢查資料是否完整與格式正確。\n");
            }
        });

        HBox inputRow = new HBox(10, new Label("日期:"), datePicker,
                new Label("類別:"), categoryBox,
                new Label("金額:"), amountField, submitBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        VBox rightSide = new VBox(15, inputRow, recordList);
        rightSide.setPadding(new Insets(10));

        HBox root = new HBox(10, outputArea, rightSide);
        root.setPadding(new Insets(10));

        // Wrapping HBox in GridPane for compatibility
        GridPane layout = new GridPane();
        layout.add(root, 0, 0);

        return layout;
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
    
    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}
