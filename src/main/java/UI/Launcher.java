package UI;

public class Launcher {
    public static void main(String[] args) {
        // This bypasses the JavaFX module check because Launcher
        // does NOT extend Application!
        Main.main(args);
    }
}