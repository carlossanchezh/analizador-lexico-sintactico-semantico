package errores;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lexico.Token;
import lexico.TokenType;
import tablas.TablaSimbolos;

public class GestorErrores {

    private String rutaArchivo;
    private List<String> erroresSemanticos;

    public GestorErrores(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        this.erroresSemanticos = new ArrayList<>();
    }

    public void errorLexico(String mensaje, int linea, int columna) {
        System.out.println("Se ha encontrado un error Lexico");
        escribirError("Error Lexico: " + mensaje + " | En la linea: " + linea);
    }

    public void errorSintactico(TokenType esperado, Token recibido, TablaSimbolos tabla) {
        if (recibido.getType() == TokenType.id) {

            int idpos = Integer.parseInt(recibido.getLexema());

            System.out.println("Se ha encontrado un error Sintactico");
            escribirError("Error Sintactico: se esperaba " + esperado.getDescripcion() + " | Pero se encontro "
                    + recibido.getType().getDescripcion() + " (" + tabla.getEntradaPos(idpos).getLexema() + ")"
                    + " | En la linea: " + recibido.getLinea());

        } else {

            System.out.println("Se ha encontrado un error Sintactico");
            escribirError("Error Sintactico: se esperaba " + esperado.getDescripcion() + " | Pero se encontro "
                    + recibido.getType().getDescripcion() + " | En la linea: " + recibido.getLinea());
        }

    }

    public void errorSintacticoParse(String produccion, Token recibido, TablaSimbolos tabla) {
        if (recibido.getType() == TokenType.id) {

            int idpos = Integer.parseInt(recibido.getLexema());
            System.out.println("Se ha encontrado un error Sintactico");
            escribirError("Error Sintactico: se esperaba " + produccion + " | Pero se encontro: "
                    + recibido.getType().getDescripcion() + " (" + tabla.getEntradaPos(idpos).getLexema() + ")"
                    + " | En la linea: " + recibido.getLinea());

        } else {

            System.out.println("Se ha encontrado un error Sintactico");
            escribirError("Error Sintactico: se esperaba " + produccion + " | Pero se encontro: "
                    + recibido.getType().getDescripcion() + " | En la linea: " + recibido.getLinea());
        }

    }

    public void errorSemantico(String mensaje, Token recibido) {
        String error = "Error semantico: " + mensaje + " | En la linea: " + recibido.getLinea();
        erroresSemanticos.add(error);
    }

    public void guardarErrores() {
        if (erroresSemanticos.isEmpty()) {
            System.out.println("No se han encontrado errores semanticos.");
            return;
        }

        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            for (String err : erroresSemanticos) {
                writer.write(err + "\n");
            }
            System.out.println("Se han encontrado errores semanticos." + "\n"
                    + "Archivo de errores semanticos creado correctamente en " + rutaArchivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hayErrores() {
        return !erroresSemanticos.isEmpty();
    }

    private void escribirError(String mensaje) {
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            writer.write(mensaje + "\n");
            System.out.println("Archivo de errores creado correctamente en " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error escribiendo en archivo de errores: " + e.getMessage());
        }
        System.exit(1);
    }

}
