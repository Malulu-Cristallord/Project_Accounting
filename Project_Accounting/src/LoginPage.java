import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

public class LoginPage {

	private final File dataFolder = new File("data");
	private final File userDataFile = new File(dataFolder, "UserNames.txt");

	private final String FIELD_DET = "XXXX";
	private final int currentWeek = getCurrentWeek();
	private final int currentDay = getCurrentDay();
    private TextField tfUserName;
    private PasswordField tfPassword;
    private Button btnLogin, btnDeleteUser, btnEnroll, btnClearFile;
    private String fileName = "UserNames.txt";
    private final String server  = "jdbc:mysql://140.119.19.73:3315/";
    private final String database = "TG12";
	private final String url = server + database + "?useSSL=false";
	private final String username = "TG12";
	private final String password = "nkH3Iq"; 
    public void createLoginPage(Stage stage) {
    	
    	fileCheck(userDataFile);
    	connectDB(server, database, url, username, password);
        VBox layout = new VBox(10);
        VBox button = new VBox(10);
        tfUserName = new TextField();
        tfUserName.setPromptText("使用者名稱");

        tfPassword = new PasswordField();
        tfPassword.setPromptText("輸入密碼");

        btnEnroll = new Button("註冊");
        btnLogin = new Button("登入");
        btnClearFile = new Button("清除檔案並關閉程式(按下前請三思)");
        btnClearFile.setStyle("-fx-background-color: #8B0000; -fx-text-fill: #FFFFFF;");
        btnDeleteUser = new Button("刪除已輸入的使用者檔案");
        btnDeleteUser.setStyle("-fx-background-color: #8B0000; -fx-text-fill: #FFFFFF;");

        btnLogin.setOnAction(e -> handleLogin(stage));
        btnEnroll.setOnAction(e -> handleEnroll());
        btnDeleteUser.setOnAction(e -> handleDeleteUser());
        btnClearFile.setOnAction(e -> handleClearFile());
        
        button.getChildren().addAll(btnLogin, btnEnroll, btnDeleteUser, btnClearFile);
        layout.getChildren().addAll(tfUserName, tfPassword, button);

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("記帳軟體登入");
        stage.setScene(scene);
        stage.show();
    }
    
    //Checks if file exists, if not, create a new file.
    private void fileCheck(File userFile) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();  // Create the folder if missing
            System.out.println("Created data directory.");
        }

        if (!userFile.exists()) {
            try {
                if (userFile.createNewFile()) {
                	showSuccess("已成功建立檔案!");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println("Save file found.");
        }
    }

    
    //The login process after the user presses the login button
    private void handleLoginAlt(Stage stage) {
        String inputU = tfUserName.getText().trim();
        String inputP = tfPassword.getText().trim();
        int totalLoggedInDays = 0;

        if (inputU.isEmpty() || inputP.isEmpty()) {
            showError("使用者名稱或密碼不得為空!");
            return;
        }

        boolean userFound = false;
        String storedP = null; // stored password
        String tempWeekLogin = " "; // temp
        int recordedWeek = 0; // record week
        int recordedDay = 0; // //day
        int hasLoggedIn = 0; // same day login indicator
        String matchedLine = null;
        List<String> allLines = new ArrayList<>();

        try (Scanner sc = new Scanner(new File(userDataFile.getPath()))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                allLines.add(line);
                String[] parts = line.split(FIELD_DET);

                if (parts.length >= 7) {
                    String storedU = parts[0];
                    if (storedU.equals(inputU)) {
                        userFound = true;
                        storedP = parts[1];
                        tempWeekLogin = parts[2];
                        hasLoggedIn = Integer.parseInt(parts[3]);
                        totalLoggedInDays = Integer.parseInt(parts[4]);
                        recordedWeek = Integer.parseInt(parts[5]);
                        recordedDay = Integer.parseInt(parts[6]);
                        matchedLine = line;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("讀檔時錯誤!");
            return;
        }

        if (!userFound) {
            showError("找不到使用者名稱!");
            return;
        }

        if (!inputP.equals(storedP)) {
            showError("密碼錯誤!");
            return;
        }

      
        LocalDate today = LocalDate.now();
        int currentDayOfWeek = today.getDayOfWeek().getValue(); // 1 = Monday
        int currentWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());
        int tempHasLoggedIn = 1;
    
        if (recordedDay != currentDayOfWeek) {
            hasLoggedIn = 0;
        }

    
        if (recordedDay != currentDayOfWeek) {
            hasLoggedIn = 0;
        }

        if (recordedWeek != currentWeek || hasLoggedIn == 0) {
            totalLoggedInDays++; //needs to return some values
            hasLoggedIn = 1;
            tempHasLoggedIn = 0;
        }

        String weekLoginStr = buildNormalizedWeekLogin(tempWeekLogin, recordedWeek, recordedDay,
                                                        currentWeek, currentDayOfWeek, tempHasLoggedIn);

        String updatedLine = inputU + FIELD_DET + storedP + FIELD_DET + weekLoginStr + FIELD_DET +
                hasLoggedIn + FIELD_DET + totalLoggedInDays + FIELD_DET +
                currentWeek + FIELD_DET + currentDayOfWeek + "\n";
     
        List<String> updatedLines = new ArrayList<>();
        for (String line : allLines) {
            if (line.equals(matchedLine)) {
                updatedLines.add(updatedLine);
            } else {
                updatedLines.add(line);
            }  
        }

        try (FileWriter writer = new FileWriter(userDataFile, false)) {
            for (String updated : updatedLines) {
                writer.write(updated + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("更新登入資料時錯誤!");
            return;
        }
        
        String[] updatedWeekLoginS = weekLoginStr.split("\\.");
        Integer[] updatedWeekLoginI = new Integer[updatedWeekLoginS.length];

        for (int i = 0; i < 7; i++) {
            if(updatedWeekLoginS[i].equals("1")) {
            	updatedWeekLoginI[i] = i + 1;
            }
        }

        loginSuccess(inputU, stage, updatedWeekLoginI, totalLoggedInDays);
    }
    
    private void handleDeleteUser() {
    	
    	String inputU = tfUserName.getText().trim();
        String inputP = tfPassword.getText().trim();
        if (inputU.isEmpty() || inputP.isEmpty()) {
            showError("使用者名稱或密碼不得為空!");
            return;
        }

        String encodedInputP = Base64.getEncoder().encodeToString(inputP.getBytes());
        String storedHash = null;
        int userID = -1;
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT ID, password_hash, loginInfo FROM UserInfo WHERE userName = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, inputU);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                userID = rs.getInt("ID");
                storedHash = rs.getString("password_hash");
            } else {
                showError("找不到使用者名稱!");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("資料庫錯誤!");
            return;
        }

        if (!encodedInputP.equals(storedHash)) {
            showError("密碼錯誤!");
            return;
        }
        try (Connection conn = DriverManager.getConnection(url, username, password)){
            String sql = "DELETE FROM UserInfo WHERE userName = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, inputU);
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                showSuccess("使用者已成功刪除!");
            } else {
                showError("刪除失敗，請再試一次。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("資料庫錯誤!");
            return;
        }
    }
    private void handleLogin(Stage stage) {
        String inputU = tfUserName.getText().trim();
        String inputP = tfPassword.getText().trim();
        
        if (inputU.isEmpty() || inputP.isEmpty()) {
            showError("使用者名稱或密碼不得為空!");
            return;
        }

        String encodedInputP = Base64.getEncoder().encodeToString(inputP.getBytes());
        String storedHash = null;
        String loginInfo = null;
        int userID = -1;

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT ID, password_hash, loginInfo FROM UserInfo WHERE userName = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, inputU);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                userID = rs.getInt("ID");
                storedHash = rs.getString("password_hash");
                loginInfo = rs.getString("loginInfo");
            } else {
                showError("找不到使用者名稱!");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("資料庫錯誤!");
            return;
        }

        if (!encodedInputP.equals(storedHash)) {
            showError("密碼錯誤!");
            return;
        }

        // --- Parse loginInfo ---
        String[] parts = loginInfo.split(FIELD_DET);
        if (parts.length < 5) {
            showError("資料格式錯誤!");
            return;
        }

        String weekLoginStr = parts[0];  // e.g. "0.1.0.1.0.0.0"
        int hasLoggedIn = Integer.parseInt(parts[1]);
        int totalLoggedInDays = Integer.parseInt(parts[2]);
        int recordedWeek = Integer.parseInt(parts[3]);
        int recordedDay = Integer.parseInt(parts[4]);

        LocalDate today = LocalDate.now();
        int currentDayOfWeek = today.getDayOfWeek().getValue(); // 1 = Monday
        int currentWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());

        int tempHasLoggedIn = 1;
        if (recordedDay != currentDayOfWeek) {
            hasLoggedIn = 0;
        }

        if (recordedWeek != currentWeek || hasLoggedIn == 0) {
            totalLoggedInDays++;
            hasLoggedIn = 1;
            tempHasLoggedIn = 0;
        }

        String updatedWeekLogin = buildNormalizedWeekLogin(weekLoginStr, recordedWeek, recordedDay,
                currentWeek, currentDayOfWeek, tempHasLoggedIn);

        // --- Rebuild loginInfo string ---
        String updatedLoginInfo = updatedWeekLogin + FIELD_DET +
                                  hasLoggedIn + FIELD_DET +
                                  totalLoggedInDays + FIELD_DET +
                                  currentWeek + FIELD_DET +
                                  currentDayOfWeek;

        // --- Save updated loginInfo ---
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String updateSql = "UPDATE UserInfo SET loginInfo = ? WHERE ID = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, updatedLoginInfo);
            updateStmt.setInt(2, userID);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("更新登入資料時錯誤!");
            return;
        }

        // --- Parse week login string into int array ---
        String[] updatedWeekLoginS = updatedWeekLogin.split("\\.");
        Integer[] updatedWeekLoginI = new Integer[7];
        for (int i = 0; i < 7; i++) {
            updatedWeekLoginI[i] = updatedWeekLoginS[i].equals("1") ? i + 1 : null;
        }

        loginSuccess(inputU, stage, updatedWeekLoginI, totalLoggedInDays);
    }



    //Passes the data after success login
    private void loginSuccess(String name, Stage stage, Integer[] loginDaysArray, int totalLoggedInDays) {
        Project_Accounting app = new Project_Accounting();
        stage.setTitle("記帳軟體");
        app.start(stage);
        app.setupMainMenu(name, stage, loginDaysArray, totalLoggedInDays);

    }

    //Identical to normal login only with the name and password set to mine
    
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //enrolls user
    /*
     * FORMAT: name password weeklogin hasloggedin totalloggedin currentweek currentday
     */
    private void handleEnroll() {
    	
        String inputU = tfUserName.getText().trim();
        String inputP = tfPassword.getText().trim();
        
        if (inputU.isEmpty() || inputP.isEmpty()) {
            showError("使用者名稱與密碼不得為空");
            return;
        }

        // Check for duplicate user names
        try (Scanner sc = new Scanner(new File(userDataFile.getPath()))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(FIELD_DET);
                if (parts.length >= 1 && parts[0].equals(inputU)) {
                    showError("該名稱已被使用!");
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error checking existing users");
            return;
        }

        int totalLoggedInDays = 0;
        int weekLogin = 0;
        int hasLoggedIn = 0;

     // Base64 encode password (not secure! Better to use BCrypt or Argon2 instead)
        String encodedPassword = Base64.getEncoder().encodeToString(inputP.getBytes());

        String loginInfo = "0.0.0.0.0.0.0" + FIELD_DET + "0" + FIELD_DET + "0" + FIELD_DET + currentWeek + FIELD_DET + currentDay;

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "INSERT INTO UserInfo (userName, password_hash, loginInfo) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, inputU);
            ps.setString(2, encodedPassword);
            ps.setString(3, loginInfo);
            ps.executeUpdate();
            showSuccess("註冊成功!");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("無法註冊使用者。");
        }

    }
    
    //Don't touch
    private String buildNormalizedWeekLogin(String previousLogin, int recordedWeek, int recordedDay,
            int currentWeek, int currentDayOfWeek, int hasLoggedIn) {
    			int[] days = new int[7]; // default to 0s

    			// Fill previous login data if it's same week
    			if (recordedWeek == currentWeek) {
    				String[] split = previousLogin.split("\\.");
    				for (int i = 0; i < Math.min(split.length, 7); i++) {
    					try {
    						days[i] = Integer.parseInt(split[i]);
    					} catch (NumberFormatException e) {
    						days[i] = 0;
    					}
    				}

    				// If not logged in yet today, mark it
    				if (hasLoggedIn == 0) {
    					days[currentDayOfWeek - 1] = 1;
    				}
    			} else {
    				// New week, only mark today
    				days[currentDayOfWeek - 1] = 1;
    			}

    			// Build string
    			StringBuilder sb = new StringBuilder();
    			for (int i = 0; i < 7; i++) {
    				sb.append(days[i]).append(".");
    			}
    			
    			return sb.toString();
    }

    private int getCurrentWeek() {
        LocalDate date = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfWeekBasedYear());
    }
    
    private int getCurrentDay() {
    	Calendar calendar = Calendar.getInstance();
    	int day = calendar.get(Calendar.DAY_OF_WEEK); 
		return day;
    }
    
    private void handleClearFile() {
        if (dataFolder.exists() && dataFolder.isDirectory()) {
            File[] files = dataFolder.listFiles();
            boolean allDeleted = true;

            if (files != null) {
                for (File f : files) {
                    if (!f.delete()) {
                        allDeleted = false;
                    }
                }
            }

            if (allDeleted) {
                showSuccess("全部資料都已刪除.");
            } else {
                showError("Some files could not be deleted.");
            }
        } else {
            showError("Data folder does not exist.");
        }
        System.exit(0);  // Close application
    }
    
    private void connectDB(String server, String database, String url, String username, String password) {
		try (Connection conn = DriverManager.getConnection(url, username, password)) {
			System.out.println("DB Connected");
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

}
