package org.example.test_1;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {
    private double balance = 0;
    private List<Transaction> transactionHistory = new ArrayList<>();
    private List<LocalDate> transactionDates = new ArrayList<>();
    private List<Double> balanceVariation = new ArrayList<>();

    private Label feedbackLabel = new Label();
    private Label balanceStatusLabel = new Label();
    private String selectedCurrency = "USD"; // Default currency

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Budget Tracker");
        primaryStage.setResizable(false);

        // the main layout
        BorderPane borderPane = new BorderPane();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(50);

        // Labels for balance, amount, category, and date
        Label balanceLabel = new Label("Current Balance:");
        Label balanceValue = new Label("$" + balance);
        Label amountLabel = new Label("Amount:");
        TextField amountInput = new TextField();
        amountInput.setPromptText("Enter amount");
        amountInput.setStyle("-fx-prompt-text-fill:blue;");
        Label categoryLabel = new Label("Category:");
        TextField categoryInput = new TextField();
        categoryInput.setPromptText("Enter category");
        categoryInput.setStyle("-fx-prompt-text-fill:blue;");
        Label dateLabel = new Label("Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select date");

        // ComboBox for currency selection
        Label currencyLabel = new Label("Currency:");
        ComboBox<String> currencyComboBox = new ComboBox<>();
        currencyComboBox.getItems().addAll("USD", "EUR", "GBP");
        currencyComboBox.setValue(selectedCurrency);
        currencyComboBox.setStyle("-fx-pref-width: 120px;");
        currencyComboBox.setOnAction(e -> selectedCurrency = currencyComboBox.getValue());
        GridPane.setConstraints(currencyLabel, 0, 4);
        GridPane.setConstraints(currencyComboBox, 1, 4);

        // putting UI elements in the grid
        GridPane.setConstraints(balanceLabel, 0, 0);
        GridPane.setConstraints(balanceValue, 1, 0);
        GridPane.setConstraints(amountLabel, 0, 1);
        GridPane.setConstraints(amountInput, 1, 1);
        GridPane.setConstraints(categoryLabel, 0, 2);
        GridPane.setConstraints(categoryInput, 1, 2);
        GridPane.setConstraints(dateLabel, 0, 3);
        GridPane.setConstraints(datePicker, 1, 3);

        // Buttons for adding transactions, showing history, calendar, and graph
        Button addTransactionButton = new Button("Add Transaction");
        addTransactionButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        GridPane.setConstraints(addTransactionButton, 1, 5);
        Button showHistoryButton = new Button("Show History");
        showHistoryButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        GridPane.setConstraints(showHistoryButton, 1, 6);
        Button calendarButton = new Button("Calendar");
        calendarButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        GridPane.setConstraints(calendarButton, 0, 5);
        Button graphButton = new Button("Graph");
        graphButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        GridPane.setConstraints(graphButton, 1, 7);
        Button ratesButton = new Button("Rates");
        ratesButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        GridPane.setConstraints(ratesButton, 0, 7);

        // Feedback label for transaction status and balance status label
        feedbackLabel.setWrapText(true);
        feedbackLabel.setStyle("-fx-text-fill: blue;");
        GridPane.setConstraints(feedbackLabel, 0, 8, 2, 1);
        balanceStatusLabel.setStyle("-fx-font-weight: bold;");
        GridPane.setConstraints(balanceStatusLabel, 0, 9, 2, 1);

        // Event handlers for Enter key press to navigate between fields
        amountInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                categoryInput.requestFocus();
            }
        });
        categoryInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                datePicker.requestFocus();
            }
        });
        datePicker.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addTransactionButton.fire(); // Trigger addTransactionButton action
            }
        });

        // Event handler for calendar button
        calendarButton.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                showTransactionsForDate(selectedDate);
            } else {
                updateFeedback("Please select a date.");
            }
        });

        // Event handler for graph button
        graphButton.setOnAction(e -> {
            showBalanceGraph();
        });

        // Event handler for rates button
        ratesButton.setOnAction(e -> {
            fetchCurrencyRates();
        });

        // Event handlers for buttons
        addTransactionButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountInput.getText());
                String category = categoryInput.getText();
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate != null) {
                    String date = selectedDate.toString();
                    addTransaction(amount, category, date); // No need to pass currency here
                    amountInput.clear();
                    categoryInput.clear();
                    datePicker.setValue(null);
                    balanceValue.setText(getFormattedBalance()); // Update balance with currency
                    updateFeedback("Transaction added successfully.");
                    updateBalanceStatus();
                } else {
                    updateFeedback("Please select a date.");
                }
            } catch (NumberFormatException ex) {
                updateFeedback("Please enter a valid numeric amount.");
            }
        });
        showHistoryButton.setOnAction(e -> {
            showTransactionHistory();
        });

        grid.getChildren().addAll(balanceLabel, balanceValue, amountLabel, amountInput,
                categoryLabel, categoryInput, dateLabel, datePicker, currencyLabel, currencyComboBox,
                addTransactionButton, showHistoryButton, calendarButton, graphButton, feedbackLabel, balanceStatusLabel, ratesButton);

        // setting up the main layout
        borderPane.setTop(grid);
        borderPane.setCenter(new Label("Welcome to Budget Tracker"));
        borderPane.setStyle("-fx-background-color: white;");

        // creating the scene and setting it to the stage
        Scene scene = new Scene(borderPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to add a transaction
    private void addTransaction(double amount, String category, String date) {
        if (selectedCurrency.equals("EUR")) {
            amount *= 0.85; // Conversion rate from USD to EUR
        } else if (selectedCurrency.equals("GBP")) {
            amount *= 0.73; // Conversion rate from USD to GBP
        }
        balance += amount;
        transactionHistory.add(new Transaction(date, amount, category, selectedCurrency));
        transactionDates.add(LocalDate.parse(date));
        balanceVariation.add(balance);
    }

    // Method to show transaction history
    private void showTransactionHistory() {
        StringBuilder history = new StringBuilder();
        for (Transaction transaction : transactionHistory) {
            history.append(transaction.toString()).append("\n");
        }
        Alert historyAlert = new Alert(Alert.AlertType.INFORMATION);
        historyAlert.setTitle("Transaction History");
        historyAlert.setHeaderText(null);
        historyAlert.setContentText(history.toString());
        historyAlert.show();
    }

    // Method to show transactions for a specific date
    private void showTransactionsForDate(LocalDate date) {
        StringBuilder transactionsForDate = new StringBuilder();
        for (Transaction transaction : transactionHistory) {
            if (LocalDate.parse(transaction.getDate()).equals(date)) {
                transactionsForDate.append(transaction.toString()).append("\n");
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transactions for " + date);
        alert.setHeaderText(null);
        if (transactionsForDate.length() > 0) {
            alert.setContentText(transactionsForDate.toString());
        } else {
            alert.setContentText("No transactions found for " + date);
        }
        alert.show();
    }

    // Method to show balance variation graph
    private void showBalanceGraph() {
        Stage graphStage = new Stage();
        graphStage.setTitle("Balance Variation Graph");

        // Defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Transaction");
        yAxis.setLabel("Balance");

        // Creating the line chart
        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Balance Variation Over Time");

        // Defining a series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Balance");

        // Adding data to the series
        for (int i = 0; i < transactionDates.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, balanceVariation.get(i)));
        }

        // Adding the series to the line chart
        lineChart.getData().add(series);

        // Creating a stack pane to hold the chart
        StackPane root = new StackPane();
        root.getChildren().add(lineChart);

        // Creating the scene
        Scene scene = new Scene(root, 600, 400);
        graphStage.setScene(scene);

        // Displaying the stage
        graphStage.show();
    }

    // Method to update feedback label
    private void updateFeedback(String message) {
        feedbackLabel.setText(message);
    }

    // Method to update balance status label
    private void updateBalanceStatus() {
        if (balance < 0) {
            balanceStatusLabel.setText("Warning: You are spending too much, you are in debt!");
            balanceStatusLabel.setStyle("-fx-text-fill: red;");
        } else if (balance >= 0 && balance <= 100) {
            balanceStatusLabel.setText("Warning: If you spend more, you will be in debt.");
            balanceStatusLabel.setStyle("-fx-text-fill: orange;");
        } else {
            balanceStatusLabel.setText("You are spending a normal amount.");
            balanceStatusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    // Method to format balance with currency
    private String getFormattedBalance() {
        if (selectedCurrency.equals("EUR")) {
            return "€" + String.format("%.2f", balance * 0.85); // Conversion rate from USD to EUR
        } else if (selectedCurrency.equals("GBP")) {
            return "£" + String.format("%.2f", balance * 0.73); // Conversion rate from USD to GBP
        } else {
            return "$" + String.format("%.2f", balance);
        }
    }

    // Method to fetch currency conversion rates (simulated)
    private void fetchCurrencyRates() {
        if (selectedCurrency.equals("EUR")) {
            double conversionRate = CurrencyConverter.getConversionRate(selectedCurrency, "USD"); // Simulated conversion rate
            updateFeedback("1 " + selectedCurrency + " = " + conversionRate + " USD");
        } else if (selectedCurrency.equals("GBP")) {
            double conversionRate = CurrencyConverter.getConversionRate(selectedCurrency, "USD"); // Simulated conversion rate
            updateFeedback("1 " + selectedCurrency + " = " + conversionRate + " USD");
        } else {
            updateFeedback("No conversion rate available for selected currency.");
        }
    }

    // Main method to start the application
    public static void main(String[] args) {
        launch(args);
    }
}

class Transaction {
    private String date;
    private double amount;
    private String category;
    private String currency;

    public Transaction(String date, double amount, String category, String currency) {
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.currency = currency;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        String formattedAmount;
        if (currency.equals("EUR")) {
            formattedAmount = "€" + String.format("%.2f", amount * 0.85);
        } else if (currency.equals("GBP")) {
            formattedAmount = "£" + String.format("%.2f", amount * 0.73);
        } else {
            formattedAmount = "$" + String.format("%.2f", amount);
        }
        return String.format("[%s] %s - %s", date, formattedAmount, category);
    }
}

class CurrencyConverter {
    // Simulated method to fetch conversion rates from an external API
    public static double getConversionRate(String fromCurrency, String toCurrency) {
        // Simulated rates (replace with actual API call)
        if (fromCurrency.equals("USD") && toCurrency.equals("EUR")) {
            return 0.85;
        } else if (fromCurrency.equals("EUR") && toCurrency.equals("USD")) {
            return 1.18;
        } else if (fromCurrency.equals("USD") && toCurrency.equals("GBP")) {
            return 0.73;
        } else if (fromCurrency.equals("GBP") && toCurrency.equals("USD")) {
            return 1.37;
        } else {
            return 1.0; // Same currency, rate is 1
        }
    }
}
