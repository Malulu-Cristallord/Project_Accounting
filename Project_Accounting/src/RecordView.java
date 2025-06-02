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
    	String fileNameBudget = "data/" + name + "Budget.txt";
    	fileCheck(fileName);
    	Project_Accounting.records.clear();
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
                updatePieChartWithSummary(pieChart, Project_Accounting.records, fileName);
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
                writeFile(fileName, fileNameBudget, date, category, itemName, amount);
                getBudget(fileNameBudget, category, itemName, amount);
                loadRecordListFromFile(fileName);
                updatePieChartWithSummary(pieChart, Project_Accounting.records, fileName);
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
        updatePieChartWithSummary(pieChart, Project_Accounting.records, fileName);

        return layout;
    }

    private void updatePieChartWithSummary(PieChart chart, List<Record> records, String fileName) {
        try {
            Map<String, Double> categoryTotals = new HashMap<>();
            for (Record r : records) {
                categoryTotals.put(r.category, categoryTotals.getOrDefault(r.category, 0.0) + r.amount);
            }

            chart.getData().clear();
            double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total == 0) total = 1;  // avoid division by zero

            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                double value = entry.getValue();
                double percentage = (value / total) * 100;
                String label = String.format("%s %.1f%%", entry.getKey(), percentage);
                PieChart.Data slice = new PieChart.Data(label, value);
                chart.getData().add(slice);
            }

            summaryBox.getChildren().clear();
            summaryBox.getChildren().add(new Label("各類別總額：" + String.format("%.2f 元", total)));

            for (String category : Arrays.asList("食物", "日常確幸", "服飾", "欠款", "通勤", "其他")) {
                double sum = categoryTotals.getOrDefault(category, 0.0);
                summaryBox.getChildren().add(new Label(category + ": " + sum + " 元"));
            }

        } catch (Exception e) {
            chart.setTitle("資料錯誤：更新失敗但仍保留之前的資料");
            e.printStackTrace();
        }
    }


    
    private void loadRecordsFromFile(String fileName) {
    	recordList.getItems().clear();
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
    
    private void getBudget(String fileNameBudget, String category, String itemName, double amount) {
    	boolean titleFound = false;
    	double budget = 0;
    	double budgetSpent = 0;
    	double newSpent = 0;
        try (Scanner scanner = new Scanner(new File(fileNameBudget))) {
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // skip header
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 4) {
                        String itemNameT = parts[0];
                        budget = Double.parseDouble(parts[2]);
                        budgetSpent = Double.parseDouble(parts[3]);

                        if (itemName.trim().equals(itemNameT.trim())) {
                            titleFound = true;
                        }
                    }
                }
            }
            if(titleFound == true) {
            	newSpent = budgetSpent + amount;
                if (budget > 0  && newSpent >= 0.8 * budget) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("支出預警");
                    alert.setHeaderText(null);
                    alert.setContentText("警告！科目「" + itemName + "」的支出已超過其預算的 80%！");
                    alert.showAndWait();
                }
                System.out.println("Budget: " + budget + ", Spent: " + budgetSpent + ", New: " + newSpent);

            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        
    }

    // title-category-budget-budgetSpent
    // write a record into a file and check the budget file, if found, update the 已花費預算. If not, don't do anything to the budget file
    private void writeFile(String fileName, String fileNameBudget, String date, String category, String itemName, double amount) {
    	
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(String.format("%-20s%-20s%-20s%-20s%n", date, category, itemName, amount));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        boolean budgetTitleFound = false;
        List<String> updatedLines = new ArrayList<>();
        // fix this so that the reader ignores the first line
        try(Scanner scanner = new Scanner(new File(fileNameBudget))){
            if (scanner.hasNextLine()) {
                scanner.nextLine(); 
            }
    		while(scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
    			if (!line.isEmpty()) {                    
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 4) {
                        String itemNameT = parts[0];
                        double budget = Double.parseDouble(parts[2]);
                        double budgetSpent = Double.parseDouble(parts[3]);
                        
                        if(itemNameT.equals(itemName)) {
                        	budgetSpent += amount;
                        	budgetTitleFound = true;
                        	updatedLines.add(String.format("%-20s%-20s%-20.2f%-20.2f", itemName, category, budget, budgetSpent));
                        } else {
                        	updatedLines.add(line);
                        }
                    }
                }
    		}
    		
    	    if (budgetTitleFound) {
    	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileNameBudget))) {
    	        	writer.write(String.format("%-20s%-20s%-20s%-20s%n", "項目", "類別", "預算", "已花費預算"));
    	            for (String updatedLine : updatedLines) {
    	                writer.write(updatedLine);
    	                writer.newLine();
    	            }
    	        } catch (IOException e) {
    	            e.printStackTrace();
    	        }
    	    }
    	}catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        
    }
    
    private void loadRecordListFromFile(String fileName) {
        recordList.getItems().clear();
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    recordList.getItems().add(line);
                }
            }
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