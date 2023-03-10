module zadanie {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens zadanie to javafx.fxml;
    exports zadanie;
    exports zadanie.client;
    opens zadanie.client to javafx.fxml;
    exports zadanie.server;
    opens zadanie.server to javafx.fxml;
}