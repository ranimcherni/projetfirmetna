package test;
import utils.DatabaseInitializer;

public class DBFixer {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        System.out.println("Partenariats tables should be ready.");
    }
}
