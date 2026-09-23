package lexico;

import java.io.FileWriter;
import java.io.IOException;

public class TokensToFile {

    private AnalizadorLexico lexer;
    private String rutaArchivo;

    public TokensToFile(AnalizadorLexico lexer, String rutaArchivo) {
        this.lexer = lexer;
        this.rutaArchivo = rutaArchivo;
    }

    public void tokenizar() {

        try (FileWriter writer = new FileWriter(rutaArchivo)) {

            Token token;
            do {
                token = lexer.getToken();
                writer.write(token.toString());
                writer.write("\n");
            } while (token.getType() != TokenType.eof);

            System.out.println("Archivo tokens.txt creado correctamente en " + rutaArchivo);

        } catch (IOException e) {
            System.err.println("Error escribiendo tokens.txt: " + e.getMessage());
        }

    }

}