import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import errores.GestorErrores;
import lexico.AnalizadorLexico;
import lexico.TokensToFile;
import semantico.AnalizadorSemantico;
import sintactico.AnalizadorSintactico;
import tablas.GestorTablas;

public class Main {
    public static void main(String[] args) throws IOException {

        String carpeta = "files";

        Path carpetaPath = Paths.get(carpeta);

        if (Files.notExists(carpetaPath)) {
            try {
                Files.createDirectories(carpetaPath);
                System.out.println("Directorio creado: " + carpeta);
            } catch (IOException e) {
                System.err.println("No se pudo crear el directorio " + carpeta + ": " + e.getMessage());
                System.exit(1);
            }
        }

        String rutaEntrada = seleccionarArchivo();
        if (rutaEntrada == null) {

            System.out.println("No se seleccionó ningún archivo.");
            return;
        }

        System.out.println("Analizando: " + rutaEntrada);

        String[] archivos = {
                "files/errores.txt",
                "files/parse.txt",
                "files/tablaSimbolos.txt",
                "files/tokens.txt"
        };

        for (String archivo : archivos) {
            try {
                Files.deleteIfExists(Paths.get(archivo));
            } catch (IOException e) {
                System.err.println("No se pudo borrar " + archivo + ": " + e.getMessage());
            }
        }

        String codigo = "";

        try {

            codigo = new String(
                    Files.readAllBytes(Paths.get(rutaEntrada)));

        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
            System.exit(1);
        }

        GestorErrores gestorErrores = new GestorErrores("files/errores.txt");

        GestorTablas gestorTablas = new GestorTablas("files/tablaSimbolos.txt");

        AnalizadorLexico lexerTokens = new AnalizadorLexico(codigo, gestorTablas, gestorErrores);

        TokensToFile tokenizador = new TokensToFile(lexerTokens, "files/tokens.txt");

        tokenizador.tokenizar();

        AnalizadorLexico lexerSint = new AnalizadorLexico(codigo, gestorTablas, gestorErrores);

        AnalizadorSintactico sint = new AnalizadorSintactico(lexerSint, gestorTablas, gestorErrores, "files/parse.txt");

        sint.A_Sint();

        AnalizadorLexico lexerSem = new AnalizadorLexico(codigo, gestorTablas, gestorErrores);

        AnalizadorSemantico sem = new AnalizadorSemantico(lexerSem, gestorTablas, gestorErrores);

        sem.A_Sm();

    }

    private static String seleccionarArchivo() {
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception ignored) {
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona el archivo .txt a analizar");
        chooser.setCurrentDirectory(new File("."));

        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));

        int resultado = chooser.showOpenDialog(null);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
}
