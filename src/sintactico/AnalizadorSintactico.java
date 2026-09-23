package sintactico;

import java.io.FileWriter;
import java.io.IOException;

import errores.GestorErrores;
import lexico.AnalizadorLexico;
import lexico.Token;
import lexico.TokenType;
import tablas.GestorTablas;

public class AnalizadorSintactico {

    private AnalizadorLexico lexer;
    private GestorTablas gestorTablas;
    public GestorErrores gestorErrores;
    private Token sigToken;
    private FileWriter writer;
    private String rutaArchivo;

    public AnalizadorSintactico(AnalizadorLexico lexer, GestorTablas gestorTablas, GestorErrores gestorErrores,
            String rutaArchivo) {
        this.lexer = lexer;
        this.gestorTablas = gestorTablas;
        this.gestorErrores = gestorErrores;
        this.rutaArchivo = rutaArchivo;
        try {
            writer = new FileWriter(rutaArchivo);
            writer.write("Desc ");
        } catch (IOException e) {
            System.err.println("Error al escribir en archivo: " + e.getMessage());
        }
    }

    private void equipara(TokenType token) {

        if (sigToken == null) {
            System.err.println("Error fatal al leer el token, el token es null");
            System.exit(1);
        }

        if (sigToken.getType() == token) {
            sigToken = lexer.getToken();
        } else {
            gestorErrores.errorSintactico(token, sigToken, gestorTablas.getTSGlobal());
        }

    }

    public void A_Sint() throws IOException {

        try {

            sigToken = lexer.getToken();

            S();

            if (sigToken.getType() != TokenType.eof) {
                gestorErrores.errorSintactico(TokenType.eof, sigToken, gestorTablas.getTSGlobal());
            }

            System.out.println("Archivo parse.txt creado correctamente en " + rutaArchivo);

        } finally {
            if (writer != null) {
                try {

                    writer.close();

                } catch (IOException e) {
                }
            }
        }

    }

    private void S() throws IOException { // FIRST(S) = { eof, function, id, if, let, read, return, while, write }
                                          // FOLLOW(S) = {eof}

        // 1. S -> A S
        if (sigToken.getType() == TokenType.PalResLet || sigToken.getType() == TokenType.PalResIf
                || sigToken.getType() == TokenType.PalResWhile || sigToken.getType() == TokenType.id
                || sigToken.getType() == TokenType.PalResRead || sigToken.getType() == TokenType.PalResWrite
                || sigToken.getType() == TokenType.PalResReturn) {

            writer.write("1 ");
            A();
            S();
        }

        // 2. S -> T S
        else if (sigToken.getType() == TokenType.PalResFunction) {

            writer.write("2 ");
            T();
            S();
        }

        // 3. S -> eof
        else if (sigToken.getType() == TokenType.eof) {

            writer.write("3 ");
            return;
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "una declaracion/sentencia (let, if, while, id, read, write, return), una funcion (function) o fin de archivo.",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void A() throws IOException { // FIRST(A) = { let, if, while, id, read, write, return }
                                          // FOLLOW(A) = { let, if, while, id, read, write, return, function, eof }

        // 4. A -> let B id ;
        if (sigToken.getType() == TokenType.PalResLet) {
            writer.write("4 ");
            equipara(TokenType.PalResLet);
            B();
            equipara(TokenType.id);
            equipara(TokenType.puntoycoma);
        }

        // 5. A -> if ( C ) M
        else if (sigToken.getType() == TokenType.PalResIf) {
            writer.write("5 ");
            equipara(TokenType.PalResIf);
            equipara(TokenType.ParIzq);
            C();
            equipara(TokenType.ParDcha);
            M();
        }

        // 6. A -> while ( C ) { L }
        else if (sigToken.getType() == TokenType.PalResWhile) {
            writer.write("6 ");
            equipara(TokenType.PalResWhile);
            equipara(TokenType.ParIzq);
            C();
            equipara(TokenType.ParDcha);
            equipara(TokenType.LlaveIzq);
            L();
            equipara(TokenType.LlaveDcha);
        }

        // 7. A -> M
        else if (sigToken.getType() == TokenType.id ||
                sigToken.getType() == TokenType.PalResRead ||
                sigToken.getType() == TokenType.PalResWrite ||
                sigToken.getType() == TokenType.PalResReturn) {
            writer.write("7 ");
            M();
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "una declaracion (let, if, while) o una sentencia (id, read, write, return).", sigToken,
                    gestorTablas.getTSGlobal());
        }

    }

    private void B() throws IOException { // FIRST(B) = { int, float, boolean, string }
                                          // FOLLOW(B) = { id, ( }

        // 8. B -> int
        if (sigToken.getType() == TokenType.PalResInt) {
            writer.write("8 ");
            equipara(TokenType.PalResInt);
        }

        // 9. B -> float
        else if (sigToken.getType() == TokenType.PalResFloat) {
            writer.write("9 ");
            equipara(TokenType.PalResFloat);
        }

        // 10. B -> boolean
        else if (sigToken.getType() == TokenType.PalResBoolean) {
            writer.write("10 ");
            equipara(TokenType.PalResBoolean);
        }

        // 11. B -> string
        else if (sigToken.getType() == TokenType.PalResString) {
            writer.write("11 ");
            equipara(TokenType.PalResString);
        }

        else {
            gestorErrores.errorSintacticoParse("un tipo valido: int, float, boolean o string.", sigToken,
                    gestorTablas.getTSGlobal());
        }

    }

    private void C() throws IOException { // FIRST(C) = { id, (, entero, real, cadena }
                                          // FOLLOW(C) = { ), ,, ; }

        // 12. C -> D P
        writer.write("12 ");
        D();
        P();

    }

    private void P() throws IOException { // FIRST(P) = { ||, λ }
                                          // FOLLOW(P) = { ), ,, ; }

        // 13. P -> || D P
        if (sigToken.getType() == TokenType.OPLogO) {
            writer.write("13 ");
            equipara(TokenType.OPLogO);
            D();
            P();
        }

        // 14. P -> λ
        else if (sigToken.getType() == TokenType.ParDcha ||
                sigToken.getType() == TokenType.coma ||
                sigToken.getType() == TokenType.puntoycoma) {
            writer.write("14 ");
            return; // lambda
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "'+' , '==' o '||' para continuar la expresion, fin de expresion anterior ';' o cirre de parentesis ')'.",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void D() throws IOException { // FIRST(D) = { id, (, entero, real, cadena }
                                          // FOLLOW(D) = { ), ,, ;, || }

        // 15. D -> F G
        writer.write("15 ");
        F();
        G();

    }

    private void G() throws IOException { // FIRST(G) = { ==, λ }
                                          // FOLLOW(G) = { ||, ), ,, ; }

        // 16. G -> == F G
        if (sigToken.getType() == TokenType.OPRIgual) {
            writer.write("16 ");
            equipara(TokenType.OPRIgual);
            F();
            G();
        }

        // 17. G -> λ
        else if (sigToken.getType() == TokenType.OPLogO ||
                sigToken.getType() == TokenType.ParDcha ||
                sigToken.getType() == TokenType.coma ||
                sigToken.getType() == TokenType.puntoycoma) {
            writer.write("17 ");
            return;
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "'+' , '==' o '||' para continuar la expresion, fin de expresion anterior ';' o cirre de parentesis ')'.",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void F() throws IOException { // FIRST(F) = { id, (, entero, real, cadena }
                                          // FOLLOW(F) = { ==, ||, ), ,, ; }

        // 18. F -> H Z
        writer.write("18 ");
        H();
        Z();

    }

    private void Z() throws IOException { // FIRST(Z) = { + , λ }
                                          // FOLLOW(Z) = { ==, ||, ), ,, ; }

        // 19. Z -> + H Z
        if (sigToken.getType() == TokenType.OPArSuma) {
            writer.write("19 ");
            equipara(TokenType.OPArSuma);
            H();
            Z();
        }

        // 20. Z -> λ
        else if (sigToken.getType() == TokenType.OPRIgual ||
                sigToken.getType() == TokenType.OPLogO ||
                sigToken.getType() == TokenType.ParDcha ||
                sigToken.getType() == TokenType.coma ||
                sigToken.getType() == TokenType.puntoycoma) {
            writer.write("20 ");
            return;
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "'+' , '==' o '||' para continuar la expresion, fin de expresion anterior ';' o cirre de parentesis ')'.",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void H() throws IOException { // FIRST(H) = { id, (, entero, real, cadena }
                                          // FOLLOW(H) = { +, ==, ||, ), ,, ; }

        // 21. H -> id J
        if (sigToken.getType() == TokenType.id) {
            writer.write("21 ");
            equipara(TokenType.id);
            J();
        }

        // 22. H -> ( C )
        else if (sigToken.getType() == TokenType.ParIzq) {
            writer.write("22 ");
            equipara(TokenType.ParIzq);
            C();
            equipara(TokenType.ParDcha);
        }

        // 23. H -> entero
        else if (sigToken.getType() == TokenType.cteE) {
            writer.write("23 ");
            equipara(TokenType.cteE);
        }

        // 24. H -> real
        else if (sigToken.getType() == TokenType.cteR) {
            writer.write("24 ");
            equipara(TokenType.cteR);
        }

        // 25. H -> cadena
        else if (sigToken.getType() == TokenType.cad) {
            writer.write("25 ");
            equipara(TokenType.cad);
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "una expresión valida: identificador, llamada a funcion, parentesis, entero, real o cadena.",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void J() throws IOException { // FIRST(J) = { (, λ }
                                          // FOLLOW(J) = { +, ==, ||, ), ,, ; }

        // 26. J -> ( K )
        if (sigToken.getType() == TokenType.ParIzq) {
            writer.write("26 ");
            equipara(TokenType.ParIzq);
            K();
            equipara(TokenType.ParDcha);
        }

        // 27. J -> λ
        else if (sigToken.getType() == TokenType.OPArSuma ||
                sigToken.getType() == TokenType.OPRIgual ||
                sigToken.getType() == TokenType.OPLogO ||
                sigToken.getType() == TokenType.ParDcha ||
                sigToken.getType() == TokenType.coma ||
                sigToken.getType() == TokenType.puntoycoma) {
            writer.write("27 ");
            return;
        }

        else {
            gestorErrores.errorSintacticoParse("una lista de parametros o expresion entre parentesis o fin de llamada",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void K() throws IOException { // FIRST(K) = { id, (, entero, real, cadena, void, λ }
                                          // FOLLOW(K) = { ) }

        // 28. K -> C Q
        if (sigToken.getType() == TokenType.id ||
                sigToken.getType() == TokenType.ParIzq ||
                sigToken.getType() == TokenType.cteE ||
                sigToken.getType() == TokenType.cteR ||
                sigToken.getType() == TokenType.cad) {
            writer.write("28 ");
            C();
            Q();
        }

        // 29. K -> λ
        else if (sigToken.getType() == TokenType.ParDcha) {
            writer.write("29 ");
            return; // lambda
        }

        else {
            gestorErrores.errorSintacticoParse("Se esperaba un argumento valido o fin de lista de argumentos", sigToken,
                    gestorTablas.getTSGlobal());
        }

    }

    private void L() throws IOException { // FIRST(L) = FIRST(A) = { let, if, while, id, read, write, return }
                                          // FOLLOW(L) = { } }

        // 30. L -> A L
        if (sigToken.getType() == TokenType.PalResLet ||
                sigToken.getType() == TokenType.PalResIf ||
                sigToken.getType() == TokenType.PalResWhile ||
                sigToken.getType() == TokenType.id ||
                sigToken.getType() == TokenType.PalResRead ||
                sigToken.getType() == TokenType.PalResWrite ||
                sigToken.getType() == TokenType.PalResReturn) {
            writer.write("30 ");
            A();
            L();
        }

        // 31. L -> λ
        else if (sigToken.getType() == TokenType.LlaveDcha) {
            writer.write("31 ");
            return;
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "una secuencia de sentencias o fin de bloque '}'.",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void M() throws IOException { // FIRST(M) = { id, read, write, return }
                                          // FOLLOW(M) = FIRST(A) ∪ { } → { let, if, while, id, read, write, return, } }

        // 32. M -> id N
        if (sigToken.getType() == TokenType.id) {
            writer.write("32 ");
            equipara(TokenType.id);
            N();
        }

        // 33. M -> read id ;
        else if (sigToken.getType() == TokenType.PalResRead) {
            writer.write("33 ");
            equipara(TokenType.PalResRead);
            equipara(TokenType.id);
            equipara(TokenType.puntoycoma);
        }

        // 34. M -> write C ;
        else if (sigToken.getType() == TokenType.PalResWrite) {
            writer.write("34 ");
            equipara(TokenType.PalResWrite);
            C();
            equipara(TokenType.puntoycoma);
        }

        // 35. M -> return R ;
        else if (sigToken.getType() == TokenType.PalResReturn) {
            writer.write("35 ");
            equipara(TokenType.PalResReturn);
            R();
            equipara(TokenType.puntoycoma);
        }

        else {
            gestorErrores.errorSintacticoParse("una sentencia valida: asignacion, lectura, escritura o retorno.",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void N() throws IOException { // FIRST(N) = { = , |= , ( }
                                          // FOLLOW(N) = { ; }

        // 36. N -> = C ;
        if (sigToken.getType() == TokenType.asignacion) {
            writer.write("36 ");
            equipara(TokenType.asignacion);
            C();
            equipara(TokenType.puntoycoma);
        }

        // 37. N -> |= C ;
        else if (sigToken.getType() == TokenType.olog) {
            writer.write("37 ");
            equipara(TokenType.olog);
            C();
            equipara(TokenType.puntoycoma);
        }

        // 38. N -> ( K ) ;
        else if (sigToken.getType() == TokenType.ParIzq) {
            writer.write("38 ");
            equipara(TokenType.ParIzq);
            K();
            equipara(TokenType.ParDcha);
            equipara(TokenType.puntoycoma);
        }

        else {
            gestorErrores.errorSintacticoParse("una asignacion (=, |=) o llamada a funcion ((...)).",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void Q() throws IOException { // FIRST(Q) = { ',', λ }
                                          // FOLLOW(Q) = { ) }

        // 39. Q -> , C Q
        if (sigToken.getType() == TokenType.coma) {
            writer.write("39 ");
            equipara(TokenType.coma);
            C();
            Q();
        }

        // 40. Q -> λ
        else if (sigToken.getType() == TokenType.ParDcha) {
            writer.write("40 ");
            return;
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "',' para continuar la lista de argumentos o cierre de parentesis ')'.", sigToken,
                    gestorTablas.getTSGlobal());
        }

    }

    private void R() throws IOException { // FIRST(R) = { (, id, entero, real, cadena , λ }
                                          // FOLLOW(R) = { ; }

        // 41. R -> C
        if (sigToken.getType() == TokenType.ParIzq ||
                sigToken.getType() == TokenType.id ||
                sigToken.getType() == TokenType.cteE ||
                sigToken.getType() == TokenType.cteR ||
                sigToken.getType() == TokenType.cad) {

            writer.write("41 ");
            C();
        }

        // 42. R -> λ
        else if (sigToken.getType() == TokenType.puntoycoma) {
            writer.write("42 ");
            return;
        }

        else {
            gestorErrores.errorSintacticoParse("una expresion de retorno o fin de sentencia.", sigToken,
                    gestorTablas.getTSGlobal());
        }

    }

    private void T() throws IOException { // FIRST(T) = { function }
                                          // FOLLOW(T) = { function, id, if, let, read, return, while, write, eof }

        // 43. T -> function V id ( X ) { L }
        if (sigToken.getType() == TokenType.PalResFunction) {
            writer.write("43 "); // regla 40 según tu gramática
            equipara(TokenType.PalResFunction);
            V();
            equipara(TokenType.id);
            equipara(TokenType.ParIzq);
            X();
            equipara(TokenType.ParDcha);
            equipara(TokenType.LlaveIzq);
            L();
            equipara(TokenType.LlaveDcha);
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "una definicion de funcion: function <tipo> <id>(<parámetros>) { <sentencias> }", sigToken,
                    gestorTablas.getTSGlobal());
        }

    }

    private void V() throws IOException { // FIRST(V) = { int, float, boolean, string, void, λ }
                                          // FOLLOW(V) = { ( }

        // 44. V -> B
        if (sigToken.getType() == TokenType.PalResInt ||
                sigToken.getType() == TokenType.PalResFloat ||
                sigToken.getType() == TokenType.PalResBoolean ||
                sigToken.getType() == TokenType.PalResString) {

            writer.write("44 ");
            B();
        }

        // 45. V -> void
        else if (sigToken.getType() == TokenType.PalResVoid) {
            writer.write("45 ");
            equipara(TokenType.PalResVoid);
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "un tipo de retorno para la funcion valido (int, float, boolean, string) o void.", sigToken,
                    gestorTablas.getTSGlobal());
        }

    }

    private void X() throws IOException { // FIRST(X) = { int, float, boolean, string, void, λ }
                                          // FOLLOW(X) = { ) }

        // 46. X -> B id Y
        if (sigToken.getType() == TokenType.PalResInt ||
                sigToken.getType() == TokenType.PalResFloat ||
                sigToken.getType() == TokenType.PalResBoolean ||
                sigToken.getType() == TokenType.PalResString) {

            writer.write("46 ");
            B();
            equipara(TokenType.id);
            Y();
        }

        // 47. X -> void
        else if (sigToken.getType() == TokenType.PalResVoid) {
            writer.write("47 ");
            equipara(TokenType.PalResVoid);
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "un parametro/s valido/s de tipo identificador (con su respectivo/s tipado/s) o void en los argumentos de la funcion.",
                    sigToken, gestorTablas.getTSGlobal());
        }

    }

    private void Y() throws IOException { // FIRST(Y) = { ',' , λ }
                                          // FOLLOW(Y) = { ')' }

        // 48. Y -> , B id Y
        if (sigToken.getType() == TokenType.coma) {
            writer.write("48 ");
            equipara(TokenType.coma);
            B();
            equipara(TokenType.id);
            Y();
        }

        // 49. Y -> λ
        else if (sigToken.getType() == TokenType.ParDcha) {
            writer.write("49 ");
            return;
        }

        else {
            gestorErrores.errorSintacticoParse(
                    "',' para continuar la lista de parametros o cierre de parentesis ')'.", sigToken,
                    gestorTablas.getTSGlobal());
        }

    }

}