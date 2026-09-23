package semantico;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import errores.GestorErrores;
import lexico.AnalizadorLexico;
import lexico.Token;
import lexico.TokenType;
import tablas.GestorTablas;
import tablas.TablaSimbolos;
import tablas.EntradaTS;

public class AnalizadorSemantico {

    private AnalizadorLexico lexer;
    private GestorTablas gestorTablas;
    public GestorErrores gestorErrores;
    private Token sigToken;

    private TablaSimbolos TSG;
    private TablaSimbolos TSL = null;
    private int desp_local = 0;
    private int desp_global = 0;
    private boolean zona_declaracion = false;
    private boolean zona_funcion = false;
    private int contadorEtiquetas = 0;
    private String tipoRetornoActual = null;

    public AnalizadorSemantico(AnalizadorLexico lexer, GestorTablas gestorTablas, GestorErrores gestorErrores) {
        this.lexer = lexer;
        this.gestorTablas = gestorTablas;
        this.gestorErrores = gestorErrores;
        this.TSG = gestorTablas.getTSGlobal();
    }

    class Nodo {
        String tipo;
        int ancho;
        List<String> tipoParametros;
        List<Integer> modoParametros;
        int numParam;
        String tipoReturn;
        boolean esAsignacionOlog = false;

        public Nodo() {
            this.tipo = "";
            this.ancho = 0;
            this.numParam = 0;
            this.tipoReturn = "";
            this.esAsignacionOlog = false;
            this.tipoParametros = new ArrayList<>();
            this.modoParametros = new ArrayList<>();
        }
    }

    private Integer insertarEnTabla(Integer idpos, TablaSimbolos tabla) {
        EntradaTS entrada = gestorTablas.getTSGlobal().getEntradaPos(idpos);
        if (entrada != null) {
            EntradaTS nuevaEntrada = tabla.insertarTSL(entrada.getLexema(), idpos, tabla.getID());
            return nuevaEntrada.getId();

        } else {
            System.err.println("No se ha podido insertar identificador con posicion " + idpos
                    + " en la tabla de simbolos correspondiente");
            System.exit(1);
            return null;
        }
    }

    private void setTipo(Integer idpos, String tipo, TablaSimbolos tabla) {
        EntradaTS entrada = tabla.getEntradaPos(idpos);
        if (entrada != null) {
            entrada.setTipo(tipo);
        } else {
            System.err.println(
                    "No se ha podido asignar tipo al identificador con posicion " + idpos + " en la tabla de simbolos");
            System.exit(1);
        }
    }

    private void setDesp(Integer idpos, int desp, TablaSimbolos tabla) {
        EntradaTS entrada = tabla.getEntradaPos(idpos);
        if (entrada != null) {
            entrada.setDesp(desp);
        } else {
            System.err.println("No se ha podido asignar desplazamiento al identificador con posicion " + idpos
                    + " en la tabla de simbolos");
            System.exit(1);
        }
    }

    private String nuevaEtiqueta(String lexema) {
        String etq = "etq_" + lexema + contadorEtiquetas;
        contadorEtiquetas++;
        return etq;
    }

    private void setEtiqueta(Integer idpos, String etiqueta, TablaSimbolos tabla) {

        EntradaTS entrada = tabla.getEntradaPos(idpos);
        if (entrada != null) {
            entrada.setEtiquetaFuncion(etiqueta);
        } else {
            System.err.println("No se ha podido asignar etiqueta al identificador de funcion en la tabla de simbolos");
            System.exit(1);
        }

    }

    private void setTipoRetorno(Integer idpos, String tipoRetorno, TablaSimbolos tabla) {

        EntradaTS entrada = tabla.getEntradaPos(idpos);
        if (entrada != null) {
            entrada.setTipoRetorno(tipoRetorno);
        } else {
            System.err.println(
                    "No se ha podido asignar tipo de retorno al identificador de funcion en la tabla de simbolos");
            System.exit(1);
        }

    }

    private void setNumParametros(Integer idpos, Integer num, TablaSimbolos tabla) {

        EntradaTS entrada = tabla.getEntradaPos(idpos);
        if (entrada != null) {
            entrada.setNumParam(num);
        } else {
            System.err.println(
                    "No se ha podido asignar el numero de parametros al identificador de funcion en la tabla de simbolos");
            System.exit(1);
        }

    }

    private void setTipoParametro(Integer idpos, String tipo, TablaSimbolos tabla) {

        EntradaTS entrada = tabla.getEntradaPos(idpos);
        if (entrada != null) {

            entrada.addTipoParam(tipo);
        } else {
            System.err.println(
                    "No se ha podido asignar el tipo de parametro al identificador de funcion en la tabla de simbolos");
            System.exit(1);
        }

    }

    private void setModoParametro(Integer idpos, Integer modo, TablaSimbolos tabla) {

        EntradaTS entrada = tabla.getEntradaPos(idpos);
        if (entrada != null) {

            entrada.addModoParam(modo);
        } else {
            System.err.println(
                    "No se ha podido asignar el modo de parametro al identificador de funcion en la tabla de simbolos");
            System.exit(1);
        }

    }

    private void setEsFuncion(Integer idpos, TablaSimbolos tabla) {

        EntradaTS entrada = tabla.getEntradaPos(idpos);
        if (entrada != null) {
            entrada.marcarComoFuncion();
        } else {
            System.err.println("No se ha podido marcar como funcion identificador de funcion en la tabla de simbolos");
            System.exit(1);
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
            System.err.println("El analisis sintactico no se realizo correctamente");
            System.exit(1);
        }

    }

    public void A_Sm() throws IOException {

        sigToken = lexer.getToken();

        S();

        gestorTablas.tablaSimbolosToFile(TSG.getID(), gestorTablas.getRutaArchivo());
        gestorTablas.destroyTSG();

        gestorErrores.guardarErrores();

    }

    private void S() throws IOException {

        // 1. S -> A S
        if (sigToken.getType() == TokenType.PalResLet || sigToken.getType() == TokenType.PalResIf
                || sigToken.getType() == TokenType.PalResWhile || sigToken.getType() == TokenType.id
                || sigToken.getType() == TokenType.PalResRead || sigToken.getType() == TokenType.PalResWrite
                || sigToken.getType() == TokenType.PalResReturn) {

            A();
            S();
        }

        // 2. S -> T S
        else if (sigToken.getType() == TokenType.PalResFunction) {

            T();
            S();
        }

        // 3. S -> eof
        else if (sigToken.getType() == TokenType.eof) {

            return;
        }

    }

    private Nodo A() throws IOException { // FIRST(A) = { let, if, while, id, read, write, return }
                                          // FOLLOW(A) = { let, if, while, id, read, write, return, function, eof }

        Nodo A = new Nodo();

        // 4. A -> let B id ;
        if (sigToken.getType() == TokenType.PalResLet) {

            zona_declaracion = true;

            equipara(TokenType.PalResLet);

            Nodo B = B();
            String idpos = sigToken.getLexema();
            String id = gestorTablas.getTSGlobal().getEntradaPos(Integer.parseInt(idpos)).getLexema();

            equipara(TokenType.id);
            Token lineaID = sigToken;
            equipara(TokenType.puntoycoma);

            TablaSimbolos tablaActual;

            if (TSL != null) {
                tablaActual = TSL;
            } else {
                tablaActual = TSG;
            }

            if (zona_declaracion) {

                EntradaTS entradaExistente = tablaActual.getEntradaId(id);

                if (entradaExistente != null && !entradaExistente.getTipo().equals("-")) {
                    gestorErrores.errorSemantico("Identificador '" + id + "' ya declarado en este ambito", lineaID);
                    A.tipo = "error";
                } else {

                    A.tipo = "ok";

                    if (tablaActual == TSL) {
                        insertarEnTabla(Integer.parseInt(idpos), tablaActual);
                        setTipo(Integer.parseInt(idpos), B.tipo, tablaActual);
                        setDesp(Integer.parseInt(idpos), desp_local, tablaActual);
                        desp_local += B.ancho;
                    } else {
                        setTipo(Integer.parseInt(idpos), B.tipo, tablaActual);
                        setDesp(Integer.parseInt(idpos), desp_global, tablaActual);
                        desp_global += B.ancho;
                    }
                }
            } else {
                gestorErrores.errorSemantico("No se puede declarar variable fuera de una sentencia 'let'", sigToken);
                A.tipo = "error";
            }

            zona_declaracion = false;

        }

        // 5. A -> if ( C ) M
        else if (sigToken.getType() == TokenType.PalResIf) {
            equipara(TokenType.PalResIf);
            equipara(TokenType.ParIzq);

            Token lineaCondicion = sigToken;

            Nodo C = C();
            equipara(TokenType.ParDcha);

            Nodo M = M();

            if (!C.tipo.equals("boolean")) {
                gestorErrores.errorSemantico(
                        "La condicion dento de la sentencia 'if(condicion)' debe ser una expresion logica valida",
                        lineaCondicion);
                A.tipo = "error";
            } else {
                A.tipo = M.tipo;
                A.tipoReturn = M.tipoReturn;
            }
        }

        // 6. A -> while ( C ) { L }
        else if (sigToken.getType() == TokenType.PalResWhile) {
            equipara(TokenType.PalResWhile);
            equipara(TokenType.ParIzq);

            Token lineaCond = sigToken;

            Nodo C = C();

            equipara(TokenType.ParDcha);
            equipara(TokenType.LlaveIzq);

            Nodo L = L();

            equipara(TokenType.LlaveDcha);

            if (!C.tipo.equals("boolean")) {
                gestorErrores.errorSemantico(
                        "La condicion dento de la sentencia 'while(condicion){...}' debe ser una expresion logica valida",
                        lineaCond);
                A.tipo = "error";
            } else {
                A.tipo = L.tipo;
                A.tipoReturn = L.tipoReturn;
            }
        }

        // 7. A -> M
        else if (sigToken.getType() == TokenType.id ||
                sigToken.getType() == TokenType.PalResRead ||
                sigToken.getType() == TokenType.PalResWrite ||
                sigToken.getType() == TokenType.PalResReturn) {

            Nodo M = M();
            A.tipo = M.tipo;
            A.tipoReturn = M.tipoReturn;
        }

        return A;

    }

    private Nodo B() throws IOException { // FIRST(B) = { int, float, boolean, string }
                                          // FOLLOW(B) = { id, ( }

        Nodo B = new Nodo();

        // 8. B -> int
        if (sigToken.getType() == TokenType.PalResInt) {
            equipara(TokenType.PalResInt);
            B.tipo = "int";
            B.ancho = 1;
        }

        // 9. B -> float
        else if (sigToken.getType() == TokenType.PalResFloat) {
            equipara(TokenType.PalResFloat);
            B.tipo = "float";
            B.ancho = 2;
        }

        // 10. B -> boolean
        else if (sigToken.getType() == TokenType.PalResBoolean) {
            equipara(TokenType.PalResBoolean);
            B.tipo = "boolean";
            B.ancho = 1;
        }

        // 11. B -> string
        else if (sigToken.getType() == TokenType.PalResString) {
            equipara(TokenType.PalResString);
            B.tipo = "string";
            B.ancho = 64;
        }

        return B;

    }

    private Nodo C() throws IOException { // FIRST(C) = { id, (, entero, real, cadena }
                                          // FOLLOW(C) = { ), ,, ; }

        Nodo C = new Nodo();

        // 12. C -> D P
        Nodo D = D();
        Nodo P = P(D.tipo);

        if (P.tipo.equals("ok")) {
            C.tipo = D.tipo;
        } else if (P.tipo.equals("boolean")) {
            C.tipo = "boolean";
        } else {

            C.tipo = "error";
        }

        return C;

    }

    private Nodo P(String DAnteriorTipo) throws IOException { // FIRST(P) = { ||, λ }
                                                              // FOLLOW(P) = { ), ,, ; }

        Nodo P = new Nodo();

        // 13. P -> || D P
        if (sigToken.getType() == TokenType.OPLogO) {
            equipara(TokenType.OPLogO);

            if (!DAnteriorTipo.equals("boolean") && !DAnteriorTipo.equals("error")) {
                gestorErrores.errorSemantico(
                        "Tipo no valido para el operador logico || se esperaba [ boolean || ... ] pero se encontro [ "
                                + DAnteriorTipo + " || ... ]",
                        sigToken);
                P.tipo = "error";

            }

            Nodo D = D();

            if (!D.tipo.equals("boolean") || !DAnteriorTipo.equals("boolean")) {
                gestorErrores.errorSemantico(
                        "Tipo no valido para el operador logico || " + "se esperaba [ boolean || boolean ] "
                                + "pero se encontro [ " + DAnteriorTipo + " || " + D.tipo + " ]",
                        sigToken);
                P.tipo = "error";
                DAnteriorTipo = "error";
            } else {

                P.tipo = "boolean";
            }

            Nodo P2 = P(DAnteriorTipo);

            if (P.tipo.equals("error") || P2.tipo.equals("error")) {
                P.tipo = "error";
            } else {
                P.tipo = "boolean";
            }
        }

        // 14. P -> λ
        else if (sigToken.getType() == TokenType.ParDcha ||
                sigToken.getType() == TokenType.coma ||
                sigToken.getType() == TokenType.puntoycoma) {

            P.tipo = "ok";
        }

        return P;

    }

    private Nodo D() throws IOException { // FIRST(D) = { id, (, entero, real, cadena }
                                          // FOLLOW(D) = { ), ,, ;, || }

        Nodo D = new Nodo();

        // 15. D -> F G
        Nodo F = F();
        Nodo G = G(F.tipo);

        if (G.tipo.equals("ok")) {
            D.tipo = F.tipo;
        } else if (G.tipo.equals("boolean")) {
            D.tipo = "boolean";
        } else {

            D.tipo = "error";
        }

        return D;

    }

    private Nodo G(String FAnteriorTipo) throws IOException { // FIRST(G) = { ==, λ }
                                                              // FOLLOW(G) = { ||, ), ,, ; }

        Nodo G = new Nodo();

        // 16. G -> == F G
        if (sigToken.getType() == TokenType.OPRIgual) {
            equipara(TokenType.OPRIgual);

            if (!(FAnteriorTipo.equals("boolean") || FAnteriorTipo.equals("int") || FAnteriorTipo.equals("float")
                    || FAnteriorTipo.equals("string") || FAnteriorTipo.equals("error"))) {
                gestorErrores.errorSemantico(
                        "Tipo no valido para el operador == se esperaba [ (boolean/int/float/string) == ... ] pero se encontro [ "
                                + FAnteriorTipo + " == ... ]",
                        sigToken);
                G.tipo = "error";
                FAnteriorTipo = "error";

            }

            Nodo F = F();

            if (!F.tipo.equals(FAnteriorTipo)) {

                gestorErrores.errorSemantico(
                        "Los tipos a comparar deben ser iguales en == se esperaba [tipo1 == tipo2 ] tipo1 y tipo2 iguales pero se encontro [ "
                                + FAnteriorTipo + " == " + F.tipo + " ]",
                        sigToken);
                G.tipo = "error";
                FAnteriorTipo = "error";
            } else {

                G.tipo = "boolean";
                FAnteriorTipo = "boolean";
            }

            Nodo G2 = G(FAnteriorTipo);

            if (G.tipo.equals("error") || G2.tipo.equals("error")) {

                G.tipo = "error";

            } else {

                G.tipo = "boolean";
            }

        }

        // 17. G -> λ
        else if (sigToken.getType() == TokenType.OPLogO ||
                sigToken.getType() == TokenType.ParDcha ||
                sigToken.getType() == TokenType.coma ||
                sigToken.getType() == TokenType.puntoycoma) {

            G.tipo = "ok";
        }

        return G;

    }

    private Nodo F() throws IOException { // FIRST(F) = { id, (, entero, real, cadena }
                                          // FOLLOW(F) = { ==, ||, ), ,, ; }

        Nodo F = new Nodo();

        // 18. F -> H Z
        Nodo H = H();
        Nodo Z = Z(H.tipo);

        if (Z.tipo.equals("ok")) {
            F.tipo = H.tipo;
        } else {

            F.tipo = "error";
        }

        return F;

    }

    private Nodo Z(String HAnteriorTipo) throws IOException { // FIRST(Z) = { + , λ }
                                                              // FOLLOW(Z) = { ==, ||, ), ,, ; }

        Nodo Z = new Nodo();

        // 19. Z -> + H Z
        if (sigToken.getType() == TokenType.OPArSuma) {
            equipara(TokenType.OPArSuma);

            if (!(HAnteriorTipo.equals("int") || HAnteriorTipo.equals("float") || HAnteriorTipo.equals("error"))) {
                gestorErrores.errorSemantico(
                        "Tipo no valido para el operador + se esperaba [ (int/float) + ... ] pero se encontro [ "
                                + HAnteriorTipo + " + ... ]",
                        sigToken);
                Z.tipo = "error";
                HAnteriorTipo = "error";

            }

            Nodo H = H();

            if (!((HAnteriorTipo.equals("int") && H.tipo.equals("int"))
                    || (HAnteriorTipo.equals("float") && H.tipo.equals("float")))) {
                gestorErrores.errorSemantico(
                        "Tipo no valido para la suma los operandos deben ser del mismo tipo siendo ambos enteros o reales se esperaba [(int/float) + (int/float)] pero se encontro [ "
                                + HAnteriorTipo + " + " + H.tipo + " ]",
                        sigToken);
                Z.tipo = "error";
                HAnteriorTipo = "error";
            } else {

                Z.tipo = "ok";
            }

            Nodo Z2 = Z(HAnteriorTipo);

            if (Z.tipo.equals("error") || Z2.tipo.equals("error")) {
                Z.tipo = "error";
            } else {

                Z.tipo = "ok";
            }
        }

        // 20. Z -> λ
        else if (sigToken.getType() == TokenType.OPRIgual ||
                sigToken.getType() == TokenType.OPLogO ||
                sigToken.getType() == TokenType.ParDcha ||
                sigToken.getType() == TokenType.coma ||
                sigToken.getType() == TokenType.puntoycoma) {

            Z.tipo = "ok";
        }

        return Z;

    }

    private Nodo H() throws IOException { // FIRST(H) = { id, (, entero, real, cadena }
                                          // FOLLOW(H) = { +, ==, ||, ), ,, ; }

        Nodo H = new Nodo();

        // 21. H -> id J
        if (sigToken.getType() == TokenType.id) {

            Integer idpos = Integer.parseInt(sigToken.getLexema());
            String id = gestorTablas.getTSGlobal().getEntradaPos(idpos).getLexema();

            equipara(TokenType.id);
            Token lineaID = sigToken;

            Nodo J = J();

            TablaSimbolos tablaActual;

            if (TSL != null) {
                tablaActual = TSL;
            } else {
                tablaActual = TSG;
            }

            EntradaTS entrada = tablaActual.getEntradaId(id);

            if (entrada == null) {
                entrada = TSG.getEntradaId(id);

            }

            if (entrada.getTipo().equals("-") && J.tipo.equals("id")) {

                setTipo(idpos, "int", TSG);
                setDesp(idpos, desp_global, TSG);
                desp_global += 1;

                entrada = TSG.getEntradaId(id);
            }

            if (J.tipo.equals("id")) {

                if (!entrada.esFuncion()) {

                    H.tipo = (String) entrada.getTipo();

                } else {

                    gestorErrores.errorSemantico("Uso indebido de funcion, '" + entrada.getLexema()
                            + "' es funcion y no puede usarse como variable", lineaID);
                    H.tipo = "error";
                }

            }

            else {

                if (!entrada.esFuncion()) {
                    gestorErrores.errorSemantico("'" + entrada.getLexema() + "' no esta declarado como funcion",
                            lineaID);
                    H.tipo = "error";

                } else {

                    if (J.numParam != entrada.getNumParam()) {
                        gestorErrores.errorSemantico("Numero de parametros incorrecto en llamada a '"
                                + entrada.getLexema() + "' se esperaban '" + entrada.getNumParam()
                                + "' parametros de tipo '" + entrada.getTiposParam().toString()
                                + "' pero se encontraron '" + J.numParam + "' parametros ", lineaID);
                        H.tipo = "error";
                    } else {
                        if (J.numParam == 0) {
                            H.tipo = entrada.getTipoRetorno();
                        } else {

                            for (int i = 0; i < J.numParam; i++) {

                                if (!J.tipoParametros.get(i).equals(entrada.getTiposParam().get(i))) {
                                    gestorErrores.errorSemantico("Tipo del parámetro '" + (i + 1)
                                            + "' incorrecto en la llamada a '" + entrada.getLexema()
                                            + "' el parametro deberia ser de tipo '" + entrada.getTiposParam().get(i)
                                            + "' pero es de tipo '" + J.tipoParametros.get(i) + "'", lineaID);
                                    H.tipo = "error";
                                } else {
                                    H.tipo = entrada.getTipoRetorno();
                                }
                            }
                        }
                        if (!H.tipo.equals("error")) {
                            H.tipo = entrada.getTipoRetorno();
                        }
                    }
                }
            }

        }

        // 22. H -> ( C )
        else if (sigToken.getType() == TokenType.ParIzq) {
            equipara(TokenType.ParIzq);

            Nodo C = C();

            equipara(TokenType.ParDcha);

            H.tipo = C.tipo;

        }

        // 23. H -> entero
        else if (sigToken.getType() == TokenType.cteE) {
            equipara(TokenType.cteE);

            H.tipo = "int";
            H.ancho = 1;
        }

        // 24. H -> real
        else if (sigToken.getType() == TokenType.cteR) {
            equipara(TokenType.cteR);

            H.tipo = "float";
            H.ancho = 2;
        }

        // 25. H -> cadena
        else if (sigToken.getType() == TokenType.cad) {
            equipara(TokenType.cad);

            H.tipo = "string";
            H.ancho = 64;
        }

        return H;

    }

    private Nodo J() throws IOException { // FIRST(J) = { (, λ }
                                          // FOLLOW(J) = { +, ==, ||, ), ,, ; }

        Nodo J = new Nodo();

        // 26. J -> ( K )
        if (sigToken.getType() == TokenType.ParIzq) {
            equipara(TokenType.ParIzq);

            Nodo K = K();

            equipara(TokenType.ParDcha);

            J.tipo = "funcion";
            J.numParam = K.numParam;
            J.tipoParametros = K.tipoParametros;
        }

        // 27. J -> λ
        else if (sigToken.getType() == TokenType.OPArSuma ||
                sigToken.getType() == TokenType.OPRIgual ||
                sigToken.getType() == TokenType.OPLogO ||
                sigToken.getType() == TokenType.ParDcha ||
                sigToken.getType() == TokenType.coma ||
                sigToken.getType() == TokenType.puntoycoma) {

            J.tipo = "id";
            J.numParam = 0;
            J.tipoParametros = new ArrayList<>();
        }

        return J;

    }

    private Nodo K() throws IOException { // FIRST(K) = { id, (, entero, real, cadena, void, λ }
                                          // FOLLOW(K) = { ) }

        Nodo K = new Nodo();

        // 28. K -> C Q
        if (sigToken.getType() == TokenType.id ||
                sigToken.getType() == TokenType.ParIzq ||
                sigToken.getType() == TokenType.cteE ||
                sigToken.getType() == TokenType.cteR ||
                sigToken.getType() == TokenType.cad) {

            Nodo C = C();
            Nodo Q = Q();

            K.tipoParametros.add(C.tipo);

            for (int i = 0; i < Q.numParam; i++) {
                K.tipoParametros.add(Q.tipoParametros.get(i));
            }

            K.numParam = 1 + Q.numParam;
            K.tipo = "ok";
        }

        // 29. K -> λ
        else if (sigToken.getType() == TokenType.ParDcha) {

            K.tipo = "ok";
            K.numParam = 0;
            K.tipoParametros = new ArrayList<>();
        }

        return K;

    }

    private Nodo L() throws IOException { // FIRST(L) = FIRST(A) = { let, if, while, id, read, write, return }
                                          // FOLLOW(L) = { } }

        Nodo L = new Nodo();

        // 30. L -> A L
        if (sigToken.getType() == TokenType.PalResLet ||
                sigToken.getType() == TokenType.PalResIf ||
                sigToken.getType() == TokenType.PalResWhile ||
                sigToken.getType() == TokenType.id ||
                sigToken.getType() == TokenType.PalResRead ||
                sigToken.getType() == TokenType.PalResWrite ||
                sigToken.getType() == TokenType.PalResReturn) {
            Nodo A = A();
            Nodo L2 = L();

            if (A.tipo.equals("error") || L2.tipo.equals("error")) {
                L.tipo = "error";
            } else {
                L.tipo = "ok";
            }

            if (A.tipoReturn != null && L2.tipoReturn != null && !A.tipoReturn.equals(L2.tipoReturn)) {
                L.tipoReturn = "error";
            } else if (A.tipoReturn != null && !A.tipoReturn.equals("void")) {
                L.tipoReturn = A.tipoReturn;
            } else if (L2.tipoReturn != null && !L2.tipoReturn.equals("void")) {
                L.tipoReturn = L2.tipoReturn;
            } else {
                L.tipoReturn = "void";
            }

        }

        // 31. L -> λ
        else if (sigToken.getType() == TokenType.LlaveDcha) {

            L.tipo = "ok";
            L.tipoReturn = "void";
        }

        return L;

    }

    private Nodo M() throws IOException { // FIRST(M) = { id, read, write, return }
                                          // FOLLOW(M) = FIRST(A) ∪ { } → { let, if, while, id, read, write, return, } }

        Nodo M = new Nodo();

        // 32. M -> id N
        if (sigToken.getType() == TokenType.id) {

            Integer idpos = Integer.parseInt(sigToken.getLexema());
            String id = gestorTablas.getTSGlobal().getEntradaPos(idpos).getLexema();

            equipara(TokenType.id);
            Token lineaAsignacion = sigToken;

            Nodo N = N();

            TablaSimbolos tablaActual;

            if (TSL != null) {
                tablaActual = TSL;
            } else {
                tablaActual = TSG;
            }

            EntradaTS entrada = tablaActual.getEntradaId(id);

            if (entrada == null) {
                entrada = TSG.getEntradaId(id);
            }

            if (entrada.getTipo().equals("-") && !N.tipo.equals("function")) {

                setTipo(idpos, "int", TSG);
                setDesp(idpos, desp_global, TSG);
                desp_global += 1;

                entrada = TSG.getEntradaId(id);
            }

            if (!entrada.esFuncion()) {

                if (N.tipo.equals("function")) {
                    gestorErrores.errorSemantico("'" + entrada.getLexema() + "' no esta declarado como funcion",
                            lineaAsignacion);
                    M.tipo = "error";
                } else {

                    String tipoVar = (String) entrada.getTipo();

                    if (N.tipo.equals("error") && !N.esAsignacionOlog) {
                        gestorErrores.errorSemantico("Asignacion incompatible se esperaba una asignacion de tipo [ "
                                + tipoVar + " = " + tipoVar + " ] pero se encontro una asignacion de tipo [ " + tipoVar
                                + " = expresion que da lugar a un resultado erroneo ]", lineaAsignacion);
                        M.tipo = "error";

                    } else if (!tipoVar.equals(N.tipo) && !N.esAsignacionOlog) {
                        gestorErrores.errorSemantico("Asignacion incompatible se esperaba una asignacion de tipo [ "
                                + tipoVar + " = " + tipoVar + " ] pero se encontro una asignacion de tipo [ " + tipoVar
                                + " = " + N.tipo + " ]", lineaAsignacion);
                        M.tipo = "error";

                    } else if (N.esAsignacionOlog && !tipoVar.equals("boolean")) {
                        gestorErrores.errorSemantico(
                                "Tipo no valido para operador '|=' se esperaba [ boolean |= boolean ] pero se encontro [ "
                                        + tipoVar + " |= " + N.tipo + " ]",
                                lineaAsignacion);
                        M.tipo = "error";

                    } else {
                        M.tipo = "ok";
                    }
                }

            } else {

                if (!N.tipo.equals("function")) {

                    gestorErrores.errorSemantico(
                            "La asignacion a función no esta permitida '" + entrada.getLexema() + "' es una funcion ",
                            lineaAsignacion);
                    M.tipo = "error";

                } else if (N.numParam != entrada.getNumParam()) {
                    gestorErrores.errorSemantico("Numero de parametros incorrecto en llamada a '" + entrada.getLexema()
                            + "' se esperaban '" + entrada.getNumParam() + "' parametros de tipo '"
                            + entrada.getTiposParam().toString() + "' pero se encontraron '" + N.numParam
                            + "' parametros ", lineaAsignacion);
                    M.tipo = "error";
                } else {
                    if (N.numParam == 0) {
                        M.tipo = M.tipo = "ok";
                    } else {

                        for (int i = 0; i < N.numParam; i++) {

                            if (!N.tipoParametros.get(i).equals(entrada.getTiposParam().get(i))) {
                                gestorErrores.errorSemantico("Tipo del parámetro '" + (i + 1)
                                        + "' incorrecto en la llamada a '" + entrada.getLexema()
                                        + "' el parametro deberia ser de tipo '" + entrada.getTiposParam().get(i)
                                        + "' pero es de tipo '" + N.tipoParametros.get(i) + "'", lineaAsignacion);
                                M.tipo = "error";
                            } else {
                                M.tipo = "ok";
                            }
                        }
                    }
                    if (!M.tipo.equals("error")) {
                        M.tipo = "ok";
                    }
                }

            }

        }

        // 33. M -> read id ;
        else if (sigToken.getType() == TokenType.PalResRead) {
            equipara(TokenType.PalResRead);

            Integer idpos = Integer.parseInt(sigToken.getLexema());
            String id = gestorTablas.getTSGlobal().getEntradaPos(idpos).getLexema();

            equipara(TokenType.id);
            Token lineaAsignacion = sigToken;
            equipara(TokenType.puntoycoma);

            TablaSimbolos tablaActual;

            if (TSL != null) {
                tablaActual = TSL;
            } else {
                tablaActual = TSG;
            }

            EntradaTS entrada = tablaActual.getEntradaId(id);

            if (entrada == null) {
                entrada = TSG.getEntradaId(id);
            }

            if (entrada.getTipo().equals("-")) {

                setTipo(idpos, "int", TSG);
                setDesp(idpos, desp_global, TSG);
                desp_global += 1;

                entrada = TSG.getEntradaId(id);

                M.tipo = "ok";
            } else {

                String tipo = (String) entrada.getTipo();

                if (!tipo.equals("int") && !tipo.equals("float") && !tipo.equals("string")) {
                    gestorErrores.errorSemantico(
                            "Al usar read solo se pueden leer valores de tipo int, float o string, pero se encontro '"
                                    + tipo + "'",
                            lineaAsignacion);
                    M.tipo = "error";
                } else {
                    M.tipo = "ok";
                }
            }
        }

        // 34. M -> write C ;
        else if (sigToken.getType() == TokenType.PalResWrite) {
            equipara(TokenType.PalResWrite);

            Token lineaWrite = sigToken;

            Nodo C = C();

            equipara(TokenType.puntoycoma);

            if (!C.tipo.equals("int") && !C.tipo.equals("float") && !C.tipo.equals("string")) {

                gestorErrores.errorSemantico(
                        "Al usar write solo se pueden imprimir valores o expresiones de tipo int, float o string, pero se encontro '"
                                + C.tipo + "'",
                        lineaWrite);
                M.tipo = "error";
            } else {
                M.tipo = "ok";
            }

        }

        // 35. M -> return R ;
        else if (sigToken.getType() == TokenType.PalResReturn) {

            equipara(TokenType.PalResReturn);
            Token lineaReturn = sigToken;

            if (!zona_funcion) {
                gestorErrores.errorSemantico("El uso de return fuera de un ambito local (function) no esta permitido",
                        lineaReturn);
                M.tipoReturn = "error";
            }

            Nodo R = R();

            if (tipoRetornoActual != null && !tipoRetornoActual.equals(R.tipo)) {
                if (R.tipo.equals("error")) {
                    gestorErrores.errorSemantico("El tipo que devuelve la funcion es incorrecto se esperaba '"
                            + tipoRetornoActual + "' y se devuelve una expresion con resultado erroneo ", lineaReturn);
                } else {
                    gestorErrores.errorSemantico("El tipo que devuelve la funcion es incorrecto se esperaba '"
                            + tipoRetornoActual + "' y se devuelve '" + R.tipo + "'", lineaReturn);
                }

                M.tipoReturn = "error";
            }

            equipara(TokenType.puntoycoma);

            if (R.tipo.equals("error")) {

                M.tipo = "error";
                M.tipoReturn = "error";
            } else {
                M.tipo = "ok";
                M.tipoReturn = R.tipo;

            }

        }

        return M;

    }

    private Nodo N() throws IOException { // FIRST(N) = { = , |= , ( }
                                          // FOLLOW(N) = { ; }

        Nodo N = new Nodo();

        // 36. N -> = C ;
        if (sigToken.getType() == TokenType.asignacion) {
            equipara(TokenType.asignacion);

            Nodo C = C();

            equipara(TokenType.puntoycoma);

            N.tipo = C.tipo;
        }

        // 37. N -> |= C ;
        else if (sigToken.getType() == TokenType.olog) {
            equipara(TokenType.olog);
            Token lineaAsigOlog = sigToken;
            Nodo C = C();

            equipara(TokenType.puntoycoma);

            N.esAsignacionOlog = true;

            if (C.tipo.equals("boolean")) {
                N.tipo = "boolean";
            } else {
                gestorErrores.errorSemantico(
                        "Tipo no valido para operador '|=' se esperaba [ boolean |= boolean ] pero se encontro [ boolean "
                                + "|= " + C.tipo + " ]",
                        lineaAsigOlog);
                N.tipo = "error";
            }
        }

        // 38. N -> ( K ) ;
        else if (sigToken.getType() == TokenType.ParIzq) {
            equipara(TokenType.ParIzq);

            Nodo K = K();

            equipara(TokenType.ParDcha);
            equipara(TokenType.puntoycoma);

            N.tipo = "function";
            N.numParam = K.numParam;
            N.tipoParametros = K.tipoParametros;

        }

        return N;

    }

    private Nodo Q() throws IOException { // FIRST(Q) = { ',', λ }
                                          // FOLLOW(Q) = { ) }

        Nodo Q = new Nodo();

        // 39. Q -> , C Q
        if (sigToken.getType() == TokenType.coma) {
            equipara(TokenType.coma);

            Nodo C = C();
            Nodo Q2 = Q();

            Q.tipoParametros.add(C.tipo);

            int offset = 1;
            for (int i = 0; i < Q2.numParam; i++) {
                Q.tipoParametros.add(i + offset, Q2.tipoParametros.get(i));
            }

            Q.numParam = 1 + Q2.numParam;
            Q.tipo = "ok";
        }

        // 40. Q -> λ
        else if (sigToken.getType() == TokenType.ParDcha) {

            Q.tipo = "ok";
            Q.numParam = 0;
            Q.tipoParametros = new ArrayList<>();
        }

        return Q;

    }

    private Nodo R() throws IOException { // FIRST(R) = { (, id, entero, real, cadena , λ }
                                          // FOLLOW(R) = { ; }

        Nodo R = new Nodo();

        // 41. R -> C
        if (sigToken.getType() == TokenType.ParIzq ||
                sigToken.getType() == TokenType.id ||
                sigToken.getType() == TokenType.cteE ||
                sigToken.getType() == TokenType.cteR ||
                sigToken.getType() == TokenType.cad) {

            Nodo C = C();
            R.tipo = C.tipo;
        }

        // 42. R -> λ
        else if (sigToken.getType() == TokenType.puntoycoma) {
            R.tipo = "void";
        }

        return R;

    }

    private void T() throws IOException { // FIRST(T) = { function }
                                          // FOLLOW(T) = { function, id, if, let, read, return, while, write, eof }

        // 43. T -> function V id ( X ) { L }
        if (sigToken.getType() == TokenType.PalResFunction) {

            equipara(TokenType.PalResFunction);

            zona_declaracion = true;
            zona_funcion = true;
            boolean funcionRepetida = false;

            Nodo V = V();
            String tipoRetorno = V.tipo;
            tipoRetornoActual = V.tipo;

            Integer idpos = Integer.parseInt(sigToken.getLexema());
            String id = gestorTablas.getTSGlobal().getEntradaPos(idpos).getLexema();

            equipara(TokenType.id);

            if (!TSG.getEntradaId(id).getTipo().equals("-")) {
                gestorErrores.errorSemantico("La funcion '" + id + "' ya esta declarada", sigToken);

                TSL = gestorTablas.crearTSLocal();
                TSL.setNombre(id + " (REPETIDA)");
                desp_local = 0;

                funcionRepetida = true;
            } else {

                setTipo(idpos, "function", TSG);
                setEsFuncion(idpos, TSG);
                setEtiqueta(idpos, nuevaEtiqueta(id), TSG);

                setTipoRetorno(idpos, tipoRetorno, TSG);
                TSL = gestorTablas.crearTSLocal();
                TSL.setNombre(id);
                desp_local = 0;
                zona_declaracion = true;
            }

            equipara(TokenType.ParIzq);

            Nodo X = X();

            if (!funcionRepetida) {

                if (X.numParam == 0) {
                    setNumParametros(idpos, 0, TSG);
                } else {

                    setNumParametros(idpos, X.numParam, TSG);

                    for (String param : X.tipoParametros) {

                        setTipoParametro(idpos, param, TSG);
                    }
                    for (Integer param : X.modoParametros) {

                        setModoParametro(idpos, param, TSG);
                    }

                }
            }

            equipara(TokenType.ParDcha);

            zona_declaracion = false;

            zona_funcion = true;

            equipara(TokenType.LlaveIzq);

            Nodo L = L();

            if (L.tipo.equals("error")) {
                gestorErrores.errorSemantico(
                        "El bloque de argumentos de la funcion '" + id + "' contene algun error/es semantico/s",
                        sigToken);
            }

            equipara(TokenType.LlaveDcha);

            zona_funcion = false;

            if (TSL != null) {
                gestorTablas.tablaSimbolosToFile(TSL.getID(), gestorTablas.getRutaArchivo());
                gestorTablas.destroyTSL(TSL.getID());
                TSL = null;
                tipoRetornoActual = null;
            }

        }

    }

    private Nodo V() throws IOException { // FIRST(V) = { int, float, boolean, string, void, λ }
                                          // FOLLOW(V) = { ( }

        Nodo V = new Nodo();

        // 44. V -> B
        if (sigToken.getType() == TokenType.PalResInt ||
                sigToken.getType() == TokenType.PalResFloat ||
                sigToken.getType() == TokenType.PalResBoolean ||
                sigToken.getType() == TokenType.PalResString) {

            Nodo B = B();
            V.tipo = B.tipo;
        }

        // 45. V -> void
        else if (sigToken.getType() == TokenType.PalResVoid) {
            equipara(TokenType.PalResVoid);

            V.tipo = "void";
        }

        return V;

    }

    private Nodo X() throws IOException { // FIRST(X) = { int, float, boolean, string, void, λ }
                                          // FOLLOW(X) = { ) }

        Nodo X = new Nodo();

        // 46. X -> B id Y
        if (sigToken.getType() == TokenType.PalResInt ||
                sigToken.getType() == TokenType.PalResFloat ||
                sigToken.getType() == TokenType.PalResBoolean ||
                sigToken.getType() == TokenType.PalResString) {

            Nodo B = B();
            String tipoParam = B.tipo;

            Integer idEntrada = Integer.parseInt(sigToken.getLexema());
            String lexema = gestorTablas.getTSGlobal().getEntradaPos(idEntrada).getLexema();

            equipara(TokenType.id);

            zona_declaracion = true;
            if (TSL.getEntradaId(lexema) != null) {
                gestorErrores.errorSemantico("Parametro '" + lexema + "' ya declarado", sigToken);
            } else {

                insertarEnTabla(idEntrada, TSL);
                setTipo(idEntrada, tipoParam, TSL);
                setDesp(idEntrada, desp_local, TSL);
                desp_local += B.ancho;
            }

            X.tipoParametros.add(tipoParam);
            X.modoParametros.add(1);
            X.numParam = 1;

            Nodo Y = Y();

            if (Y.numParam != 0) {
                X.tipoParametros.addAll(Y.tipoParametros);
                X.modoParametros.addAll(Y.modoParametros);
                X.numParam = X.tipoParametros.size();
            }

        }

        // 47. X -> void
        else if (sigToken.getType() == TokenType.PalResVoid) {
            equipara(TokenType.PalResVoid);

            X.numParam = 0;
        }

        return X;

    }

    private Nodo Y() throws IOException { // FIRST(Y) = { ',' , λ }
                                          // FOLLOW(Y) = { ')' }

        Nodo Y = new Nodo();

        // 48. Y -> , B id Y
        if (sigToken.getType() == TokenType.coma) {
            equipara(TokenType.coma);

            Nodo B = B();
            String tipoParam = B.tipo;

            Integer idEntrada = Integer.parseInt(sigToken.getLexema());
            String lexema = gestorTablas.getTSGlobal().getEntradaPos(idEntrada).getLexema();

            equipara(TokenType.id);

            zona_declaracion = true;

            if (TSL.getEntradaId(lexema) != null) {
                gestorErrores.errorSemantico("Parametro '" + lexema + "' ya declarado", sigToken);
            } else {

                insertarEnTabla(idEntrada, TSL);
                setTipo(idEntrada, tipoParam, TSL);
                setDesp(idEntrada, desp_local, TSL);
                desp_local += B.ancho;
            }

            Y.tipoParametros.add(tipoParam);
            Y.modoParametros.add(1);
            Y.numParam++;

            Nodo Y2 = Y();

            if (Y2.numParam != 0) {
                Y.tipoParametros.addAll(Y2.tipoParametros);
                Y.modoParametros.addAll(Y2.modoParametros);
                Y.numParam += Y2.numParam;
            }

        }

        // 49. Y -> λ
        else if (sigToken.getType() == TokenType.ParDcha) {

            Y.numParam = 0;
        }

        return Y;

    }

}
