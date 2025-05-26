import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.*;

public class Project_Accounting extends Application {
	
	private Integer[] loggedInDays;
	private int totalLoggedInDays;
	private String name = " ";
    private BorderPane mainLayout;
    private VBox mainMenu;
    private PieChart mainPieChart;

    public static ArrayList<Record> records = new ArrayList<>();
    public static PieChart getSharedPieChart() {
        return new PieChart();
    }

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
    	LoginPage loginPage = new LoginPage();
        loginPage.createLoginPage(primaryStage);
    }
    
    public void startMainApplication(Stage primaryStage, String name, Integer[] loggedInDays, int totalLoggedInDays) {
        primaryStage.setTitle("記帳軟體");
        mainLayout = new BorderPane();
        setInitial(name, loggedInDays, totalLoggedInDays);
        setupMainMenu();
        setupMainChart();
        mainLayout.setLeft(mainMenu);
        mainLayout.setCenter(mainPieChart);

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    //set initial variables
    public void setInitial(String name, Integer[] loggedInDays, int totalLoggedInDays) {
    	this.name = name;
    	this.loggedInDays = loggedInDays;
    	this.totalLoggedInDays = totalLoggedInDays;
    }
    
    private void setupMainMenu() {
        mainMenu = new VBox(10);
        Set<Integer> s = new HashSet<>(Arrays.asList(loggedInDays));
        Button btnRecord = new Button("記帳");
        Button btnCalendar = new Button("記帳日曆");
        Button btnTask = new Button("預算與任務");

        btnRecord.setOnAction(e -> mainLayout.setCenter(new RecordView().getView(mainPieChart, name)));
        btnCalendar.setOnAction(e -> mainLayout.setCenter(new CalendarView().getView(s, totalLoggedInDays)));
        btnTask.setOnAction(e -> mainLayout.setCenter(new TaskView().getView(name)));

        mainMenu.getChildren().addAll(btnRecord, btnCalendar, btnTask);
    }

    private void setupMainChart() {
        mainPieChart = new PieChart();
        mainPieChart.setTitle("支出分類圖表");
        ChartUtil.updatePieChart(mainPieChart, records);
    }

    public String getN() {
    	return name;
    }
    public Integer[] getLd() {
    	return loggedInDays;
    }
    public int getTLd() {
    	return totalLoggedInDays;
    }
}
