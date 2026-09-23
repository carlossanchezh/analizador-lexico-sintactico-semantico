package lexico;

import java.util.*;

import errores.GestorErrores;
import tablas.GestorTablas;
import tablas.TablaSimbolos;
import tablas.EntradaTS;

public class AnalizadorLexico {

    // mapa de Palabras reservadas
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("boolean", TokenType.PalResBoolean);
        keywords.put("float", TokenType.PalResFloat);
        keywords.put("function", TokenType.PalResFunction);
        keywords.put("if", TokenType.PalResIf);
        keywords.put("int", TokenType.PalResInt);
        keywords.put("let", TokenType.PalResLet);
        keywords.put("read", TokenType.PalResRead);
        keywords.put("return", TokenType.PalResReturn);
        keywords.put("string", TokenType.PalResString);
        keywords.put("void", TokenType.PalResVoid);
        keywords.put("while", TokenType.PalResWhile);
        keywords.put("write", TokenType.PalResWrite);
    }

    public GestorErrores gestorErrores; // controlador de errores
    public GestorTablas gestorTablas; // Mapa que contiene todas las tablas
    private TablaSimbolos tablaGlobal; // tabla de simbolos global

    private String codigo; // codigo a analizar
    private int caracter; // posicion del codigo
    private int linea; // línea actual, empezando en 1
    private int columna; // columna actual, empezando en 1

    // Constructor del Analizador Lexico
    public AnalizadorLexico(String codigo, GestorTablas gestorTablas, GestorErrores gestorErrores) {
        this.codigo = codigo;
        this.caracter = 0;
        this.linea = 1;
        this.columna = 1;
        this.gestorErrores = gestorErrores;
        this.gestorTablas = gestorTablas;
        this.tablaGlobal = gestorTablas.getTSGlobal();
    }

    // Funcion para avanzar el contador de lineas y columnas
    private void advance() {
        if (caracter >= codigo.length())
            return;
        if (codigo.charAt(caracter) == '\n') {
            linea++;
            columna = 1;
        } else {
            columna++;
        }
    }

    // Funcion para leer el caracter en la posicion actual del codigo
    private char leer() {

        if (caracter >= codigo.length()) {
            System.err.println("Error Fatal en la lectura del carcter: " + caracter + " en el codigo");
            System.exit(1);
        }

        return codigo.charAt(caracter);
    }

    // Funcion para leer el caracter siguiente a la posicion actual del codigo
    private char leerSiguiente() {

        if (caracter >= codigo.length()) {
            System.err.println("Error Fatal en la lectura del carcter: " + caracter + " en el codigo");
            System.exit(1);
        }

        return codigo.charAt(caracter + 1);
    }

    // Funcion que devuelve un token del codigo
    public Token getToken() {

        if (caracter >= codigo.length()) {
            return new Token(TokenType.eof, "", linea, columna); // si llega al fin del codigo token de eof
        }

        // Ignorar espacios y saltos de línea (del)
        while (caracter < codigo.length() && Character.isWhitespace(codigo.charAt(caracter))) {
            advance();
            caracter++;
        }
        if (caracter >= codigo.length())
            return new Token(TokenType.eof, "", linea, columna); // si llega al fin del codigo token de eof

        // Comentarios de bloque /* */
        if (leer() == '/' && caracter + 1 < codigo.length() && leerSiguiente() == '*') {
            int sl = linea;
            int sc = columna;

            for (int i = 0; i < 2; i++) { // saltar /*
                advance();
                caracter++;
            }

            while (caracter + 1 < codigo.length()
                    && !(leer() == '*' && leerSiguiente() == '/')) {

                advance();
                caracter++;

            }
            if (caracter + 1 >= codigo.length()) {
                gestorErrores.errorLexico("comentario sin cerrar", sl, sc);
            }

            for (int i = 0; i < 2; i++) { // saltar */
                advance();
                caracter++;
            }
            return getToken();
        }

        // Identificadores y palabras reservadas
        if (Character.isLetter(leer()) || leer() == '_') {
            int start = caracter; // posicion inicio de la palabra
            while (caracter < codigo.length()
                    && (Character.isLetterOrDigit(leer()) || leer() == '_')) {
                advance();
                caracter++;

            }
            String word = codigo.substring(start, caracter);
            TokenType type = keywords.getOrDefault(word, TokenType.id);

            if (type == TokenType.id) {

                // Verificar si ya está en la tabla
                EntradaTS entrada = tablaGlobal.getEntradaId(word);

                if (entrada == null) {
                    // Si no existe, insertar
                    entrada = tablaGlobal.insertarTSG(word, tablaGlobal.getID());

                }

                // Crear token usando el ID asignado
                return new Token(type, String.valueOf(entrada.getId()), linea, columna);

            } else {

                return new Token(type, "", linea, columna); // palabra reservada
            }
        }

        // Números numero entero maximo 32767 y numero real maximo 117549436.0
        if (Character.isDigit(leer())) {

            int start = caracter; // pos inicio
            boolean isFloat = false;
            int puntos = 0;

            while (caracter < codigo.length()
                    && (Character.isDigit(leer()) || leer() == '.')) {
                if (leer() == '.') {
                    puntos++;
                    isFloat = true; // si contiene decimales es un float
                    if (puntos > 1) {
                        gestorErrores.errorLexico("número mal formado", linea, columna);
                    }
                }
                advance();
                caracter++;

            }

            String num = codigo.substring(start, caracter); // numero entre pos inicial y final
            if (num.contains(".")) {
                if (Float.parseFloat(num) > 117549436.0) {
                    gestorErrores.errorLexico(
                            "numero fuera de rango (numero maximo 117549436.0)",
                            linea,
                            columna);
                } else {

                    return new Token(isFloat ? TokenType.cteR : TokenType.cteE, num, linea, columna);// devuelve token
                                                                                                     // si es int de
                                                                                                     // tipo
                    // entero
                    // y si es float de tipo real

                }
            } else {

                if (Integer.parseInt(num) > 32767) {
                    gestorErrores.errorLexico("numero fuera de rango (numero maximo 32767)",
                            linea,
                            columna);
                } else {

                    return new Token(isFloat ? TokenType.cteR : TokenType.cteE, num, linea, columna);// devuelve token
                                                                                                     // si es int de
                                                                                                     // tipo
                    // entero
                    // y si es float de tipo real

                }
            }

        }

        // Cadenas caracteres maximos 64
        if (leer() == '"') {
            int start = caracter;
            int sl = linea; // linea donde empezo la cadena
            int sc = columna; // columna de la linea donde empezo la cadena

            advance();
            caracter++; // saltar la comilla inicial

            while (caracter < codigo.length() && leer() != '"') {

                advance();
                caracter++;
            }

            if (caracter >= codigo.length()) {
                gestorErrores.errorLexico("cadena sin cerrar", sl, sc);
            }

            advance();
            caracter++; // saltar la comilla final

            String str = codigo.substring(start, caracter);
            if (str.length() > 64) {
                gestorErrores.errorLexico(
                        "superados los caracteres maximos de una cadena (64)", sl,
                        sc);
            } else {
                return new Token(TokenType.cad, str, linea, columna);
            }

        }

        // Operadores y símbolos
        switch (leer()) {
            case '=':
                if (caracter + 1 < codigo.length() && leerSiguiente() == '=') {
                    for (int i = 0; i < 2; i++) {
                        advance();
                        caracter++;
                    }
                    return new Token(TokenType.OPRIgual, "", linea, columna);
                } else {
                    advance();
                    caracter++;
                    return new Token(TokenType.asignacion, "", linea, columna);
                }
            case '+':
                advance();
                caracter++;
                return new Token(TokenType.OPArSuma, "", linea, columna);
            case '|':
                if (caracter + 1 < codigo.length() && leerSiguiente() == '|') {
                    // Avanza dos posiciones porque es "||"
                    for (int i = 0; i < 2; i++) {
                        advance();
                        caracter++;
                    }
                    return new Token(TokenType.OPLogO, "", linea, columna);
                } else if (caracter + 1 < codigo.length() && leerSiguiente() == '=') {
                    // operador |=
                    for (int i = 0; i < 2; i++) {
                        advance();
                        caracter++;
                    }
                    return new Token(TokenType.olog, "", linea, columna); // tu token para |=
                } else {
                    gestorErrores.errorLexico("'|' solitario ", linea, columna);
                }
            case ',':
                advance();
                caracter++;
                return new Token(TokenType.coma, "", linea, columna);
            case ';':
                advance();
                caracter++;
                return new Token(TokenType.puntoycoma, "", linea, columna);
            case '(':
                advance();
                caracter++;
                return new Token(TokenType.ParIzq, "", linea, columna);
            case ')':
                advance();
                caracter++;
                return new Token(TokenType.ParDcha, "", linea, columna);
            case '{':
                advance();
                caracter++;
                return new Token(TokenType.LlaveIzq, "", linea, columna);
            case '}':
                advance();
                caracter++;
                return new Token(TokenType.LlaveDcha, "", linea, columna);
        }

        // Si no se reconoce el caracter
        char desconocido = leer();
        gestorErrores.errorLexico("carácter no reconocido '" + desconocido + "'", linea,
                columna);

        return null;

    }
}