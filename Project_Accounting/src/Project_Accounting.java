import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Project_Accounting extends Application {
	
    private BorderPane mainLayout;
    private VBox mainMenu;
    private PieChart mainPieChart;
    public static ArrayList<Record> records = new ArrayList<>();

    public static PieChart getSharedPieChart() {
        return new PieChart();
    }

    @Override
    public void start(Stage primaryStage) {
        LoginPage login = new LoginPage();
        login.createLoginPage(primaryStage);  // Pass this app instance to allow callback after login
    }

    public void setupMainMenu(String name, Stage stage, Integer[] loginDaysArray, int totalLoggedInDays) {
        mainLayout = new BorderPane();
        setupMainChart();

        // Menu
        mainMenu = new VBox(10);
        Set<Integer> LDA = new HashSet<>(Arrays.asList(loginDaysArray));
        Button btnRecord = new Button("記帳");
        Button btnCalendar = new Button("記帳日曆");
        Button btnTask = new Button("預算管理");
        

        btnRecord.setOnAction(e -> mainLayout.setCenter(new RecordView().getView(mainPieChart, name)));
        btnCalendar.setOnAction(e -> mainLayout.setCenter(new CalendarView().getView(LDA, totalLoggedInDays)));
        btnTask.setOnAction(e -> mainLayout.setCenter(new TaskView().getView(name)));

        mainMenu.getChildren().addAll(btnRecord, btnCalendar, btnTask);

        // Assemble UI
        mainLayout.setLeft(mainMenu);
        mainLayout.setCenter(mainPieChart);

        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void setupMainChart() {
        mainPieChart = new PieChart();
        mainPieChart.setTitle("支出分類圖表");
        ChartUtil.updatePieChart(mainPieChart, records);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
