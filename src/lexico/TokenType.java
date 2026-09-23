package lexico;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TokenType {

    private final String nombre;
    private final String descripcion;

    private static final Map<String, TokenType> registro = new HashMap<>();

    private TokenType(String nombre, String descripcion) {

        this.nombre = nombre;
        this.descripcion = descripcion;
        registro.put(nombre, this);

    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static final TokenType PalResBoolean = new TokenType("PalResBoolean", "el tipo 'boolean'");

    public static final TokenType PalResFloat = new TokenType("PalResFloat", "el tipo 'float'");

    public static final TokenType PalResFunction = new TokenType("PalResFunction", "'function'");

    public static final TokenType PalResIf = new TokenType("PalResIf", "'if'");

    public static final TokenType PalResInt = new TokenType("PalResInt", "el tipo 'int'");

    public static final TokenType PalResLet = new TokenType("PalResLet", "'let'");

    public static final TokenType PalResRead = new TokenType("PalResRead", "'read'");

    public static final TokenType PalResReturn = new TokenType("PalResReturn", "'return'");

    public static final TokenType PalResString = new TokenType("PalResString", "el tipo 'string'");

    public static final TokenType PalResVoid = new TokenType("PalResVoid", "'void'");

    public static final TokenType PalResWhile = new TokenType("PalResWhile", "'while'");

    public static final TokenType PalResWrite = new TokenType("PalResWrite", "'write'");

    public static final TokenType cteR = new TokenType("cteR", "una constante real");

    public static final TokenType cteE = new TokenType("cteE", "una constante entera");

    public static final TokenType cad = new TokenType("cad", "una cadena de caracteres");

    public static final TokenType id = new TokenType("id", "un identificador");

    public static final TokenType asignacion = new TokenType("asignacion", "'='");

    public static final TokenType olog = new TokenType("olog", "'|='");

    public static final TokenType coma = new TokenType("coma", ",");

    public static final TokenType puntoycoma = new TokenType("puntoycoma", "';'");

    public static final TokenType ParIzq = new TokenType("ParIzq", "'('");

    public static final TokenType ParDcha = new TokenType("ParDcha", "')'");

    public static final TokenType LlaveIzq = new TokenType("LlaveIzq", "'{'");

    public static final TokenType LlaveDcha = new TokenType("LlaveDcha", "'}'");

    public static final TokenType OPArSuma = new TokenType("OPArSuma", "'+'");

    public static final TokenType OPLogO = new TokenType("OPLogO", "'||'");

    public static final TokenType OPRIgual = new TokenType("OPRIgual", "'=='");

    public static final TokenType eof = new TokenType("eof", "fin de archivo");

    public static TokenType fromNombre(String nombre) {
        return registro.get(nombre);
    }

    public static Map<String, TokenType> values() {
        return Collections.unmodifiableMap(registro);
    }

    public String toString() {
        return nombre;
    }

}
