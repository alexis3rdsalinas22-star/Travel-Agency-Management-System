package main;

import common.DBConnection;
import common.DatabaseSeeder;
import ui.Homepage;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            DatabaseSeeder.ensureSchemaCompatibility(con);
        } catch (Exception ignored) {}

        Homepage home = new Homepage();
        home.showScreen();
    }
}
