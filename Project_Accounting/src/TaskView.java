import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class TaskView {

    String filename = " ";
	private final File dataFolder = new File("data");

    public StackPane getView(String username) {

        filename = "data/" + username + "Budget.txt";
        
        switch (fileCheck(filename)) {
            case 1:
            	writeInitial();            
        }

        StackPane root = new StackPane();
        VBox labelBox = new VBox(10);
        labelBox.setAlignment(Pos.CENTER);
        HBox button = new HBox(10);
        button.setAlignment(Pos.CENTER);
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20, 30, 20, 30));

        // Title and Budget input fields
        TextField titleInput = new TextField();
        titleInput.setPromptText("輸入記帳科目");

        TextField budgetInput = new TextField();
        budgetInput.setPromptText("輸入該科目的預算:");
        
        Label type = new Label("請輸入類別: ");
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("食物", "日常確幸", "服飾", "欠款", "通勤", "其他");
        
        HBox titleInputBox = new HBox(10);
        titleInputBox.getChildren().addAll(titleInput, type, categoryBox);
        
        // Output area
        TextArea displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setPrefRowCount(10);
        displayArea.setPrefColumnCount(30);
        updateDisplay(displayArea);

        // Label
        Label label = new Label("預算管理頁面");
        label.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        
        // Save button
        Button saveButton = new Button("新增項目");
        
        //Edit button
        Button editButton = new Button("編輯");

        Button deleteButton = new Button("清除檔案並關閉程式(按下前請三思)");
        deleteButton.setStyle("-fx-background-color: #8B0000; -fx-text-fill: #FFFFFF;");
        
        // button actions
        saveButton.setOnAction(e -> {
            String title = titleInput.getText().trim();
            String budget = budgetInput.getText().trim();

            if (title.isEmpty() || budget.isEmpty()) {
                showAlert(AlertType.WARNING, "兩邊都需要填");
                return;
            }
            if(Integer.valueOf(budget) < 0) {
            	showAlert(AlertType.WARNING, "預算金額必須大於0");
                return;
            }

            if (isDuplicateTitle(title)) {
                showAlert(AlertType.ERROR, "項目已經存在");
            } else {
                writeFile(title, budget, categoryBox.getValue().toString(), "0");
                updateDisplay(displayArea);
                showAlert(AlertType.INFORMATION, "已儲存項目");
                titleInput.clear();
                budgetInput.clear();
            }
        });
        
        editButton.setOnAction(e -> {
            String title = titleInput.getText().trim();
            String budgetText = budgetInput.getText().trim();
            String category = categoryBox.getValue();

            if (title.isEmpty() || budgetText.isEmpty()) {
                showAlert(AlertType.WARNING, "請輸入項目與預算金額。（刪除請將預算設為 0）");
                return;
            }

            double budget;
            try {
                budget = Double.parseDouble(budgetText);
                if (budget < 0) {
                    showAlert(AlertType.WARNING, "預算金額不能為負數。");
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "請輸入有效的預算金額（數字）。");
                return;
            }

            if (!isDuplicateTitle(title)) {
                showAlert(AlertType.ERROR, "找不到該項目，無法編輯。");
                return;
            }

            // Proceed to delete the old one first
            deleteTitle(title);

            if (budget == 0) {
                showAlert(AlertType.INFORMATION, "已刪除項目: " + title);
            } else {
                if (category == null || category.isEmpty()) {
                    showAlert(AlertType.WARNING, "請選擇類別。");
                    return;
                }

                // Write new item with updated budget
                writeFile(title, String.format("%.2f", budget), category, "0");
                showAlert(AlertType.INFORMATION, "已更新項目: " + title);
            }

            updateDisplay(displayArea);
            titleInput.clear();
            budgetInput.clear();
            categoryBox.setValue(null);
        });

        
        deleteButton.setOnAction(e -> handleClearFile(filename));
        
        
        button.getChildren().addAll(saveButton, editButton);
        labelBox.getChildren().addAll(label);
        content.getChildren().addAll(labelBox, titleInputBox, budgetInput, button, displayArea, deleteButton);
        root.getChildren().add(content);
        return root;
    }

    private int fileCheck(String fileName) {
    	File file = new File(fileName);
    	if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Save file not found. New file created.");
                    return 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println("Save file found.");
            return 0;
        }
        return 2;
    }

    private void writeFile(String title, String budget, String type, String budgetSpent) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(String.format("%-20s%-20s%-20s%-20s%n", title, type, budget, budgetSpent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateDisplay(TextArea area) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        area.setText(content.toString());
    }
    
    private void deleteTitle(String titleToDelete) {
    	File originalFile = new File(filename);
    	File tempFile = new File(originalFile.getParentFile(), "temp_" + originalFile.getName());
        try {
			tempFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        boolean found = false;
        try (
            BufferedReader reader = new BufferedReader(new FileReader(originalFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Skip header, always keep it
                if (line.trim().equalsIgnoreCase(String.format("%-20s%-20s%-20s%-20s%n", "項目", "類別", "預算", "已花費預算"))) {
                    writer.write(line);
                    writer.newLine();
                    continue;
                }

                // Check if line starts with the title
                String[] parts = line.trim().split("\\s+");
                if (parts.length > 0 && parts[0].equalsIgnoreCase(titleToDelete)) {
                    found = true; // Skip this line (do not write it)
                    continue;
                }

                // Keep the rest
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Replace original file with temp file
        if (!originalFile.delete() || !tempFile.renameTo(originalFile)) {
            showAlert(AlertType.ERROR, "讀檔失敗.");
        }

        if (!found) {
            showAlert(AlertType.WARNING, "找不到項目.");
        }
    }

    
    private void writeInitial() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(String.format("%-20s%-20s%-20s%-20s%n", "項目", "類別", "預算", "已花費預算"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean isDuplicateTitle(String title) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            // Skip header
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length > 0 && parts[0].equalsIgnoreCase(title)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public double getBudgetForTitle(String title) {
    	double b = 0;
    	try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            // Skip header
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length > 0 && parts[0].equalsIgnoreCase(title)) {
                	
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    	return b;
    }
    

    private void handleClearFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.delete()) {
            showSuccess("已清除檔案");
        } else {
            showError("清除檔案失敗，請至Data檔案夾手動清除");
        }
        System.exit(0);
    }
    
    private void showSuccess(String message) {
    	Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
	}
    
    private void showError(String message) {
    	Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

	private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
	
	private void style(StackPane root, Label label, TextField titleInput, TextField budgetInput,
            ComboBox<String> categoryBox, TextArea displayArea,
            Button saveButton, Button editButton, Button deleteButton) {

		// Background color
		root.setStyle("-fx-background-color: #87CEFA;"); // Light Blue
	
		// Label style
		label.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
	
		// TextFields and ComboBox
		String inputStyle = "-fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 5;";
		titleInput.setStyle(inputStyle);
		budgetInput.setStyle(inputStyle);
		categoryBox.setStyle("-fx-background-color: white; -fx-border-color: gray;");

		// Buttons
		saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");   // Green
		editButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");   // Orange
		deleteButton.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;"); // Red

		// Display Area
		displayArea.setStyle("-fx-control-inner-background: #F0F8FF; -fx-font-family: monospace;");
	}

}

