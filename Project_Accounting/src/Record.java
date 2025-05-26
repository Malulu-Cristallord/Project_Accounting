import java.util.*;
import java.io.*;
public class Record {
	
    String date, category;
    double amount;

    public Record(String date, String category, double amount) {
        this.date = date;
        this.category = category;
        this.amount = amount;
    }

    public String recordToString() {
        return date + " | " + category + " | $" + amount;
    }
}