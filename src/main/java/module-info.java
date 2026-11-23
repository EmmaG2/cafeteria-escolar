module org.fernandogranados.demo {
    requires java.desktop;
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires mysql.connector.j;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires org.jfree.jfreechart;
    requires kernel;
    requires layout;
    requires io;
    requires static lombok;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens org.fernandogranados.demo to javafx.fxml;
    opens org.fernandogranados.demo.controlador to javafx.fxml;
    exports org.fernandogranados.demo;
}