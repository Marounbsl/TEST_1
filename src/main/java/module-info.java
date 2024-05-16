module org.example.test_1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.test_1 to javafx.fxml;
    exports org.example.test_1;
}