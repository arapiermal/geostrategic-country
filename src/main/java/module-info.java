module com.erimali.cntrygame {

    requires javafx.controls;
    requires javafx.media;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires eu.hansolo.tilesfx;

    opens com.erimali.cntrygame to javafx.fxml;
    exports com.erimali.cntrygame;
    exports com.erimali.minigames;

}