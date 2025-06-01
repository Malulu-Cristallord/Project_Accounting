public class Record {
    String date, category, itemName;
    double amount;

    public Record(String date, String category, String itemName,double amount) {
        this.date = date;
        this.category = category;
        this.itemName = itemName;
        this.amount = amount;
    }

    public String toString() {
        return date + " | " + category + " | " + itemName + " | $" + amount;
    }
}