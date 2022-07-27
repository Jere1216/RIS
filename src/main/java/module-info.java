module aasim.ris {
    //FileUtils
    requires org.apache.commons.io;
    //JavaFX
    requires javafx.controls;
    requires javafx.graphics;

    //SQL
    requires java.sql;
    //Cockroach DB
    requires java.naming;
    requires org.postgresql.jdbc;

    opens datastorage;
    exports aasim.ris;
}
