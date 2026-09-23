package tablas;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorTablas {

    private String rutaArchivo;
    private Map<Integer, TablaSimbolos> listaTablaSimbolos;
    private TablaSimbolos TSG;

    public GestorTablas(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        this.listaTablaSimbolos = new HashMap<>();
        this.TSG = new TablaSimbolos();

        listaTablaSimbolos.put(TSG.getID(), TSG);
    }

    public TablaSimbolos getTSGlobal() {
        return listaTablaSimbolos.get(0);
    }

    public TablaSimbolos crearTSLocal() {
        TablaSimbolos TSL = new TablaSimbolos();
        listaTablaSimbolos.put(TSL.getID(), TSL);
        return TSL;
    }

    public TablaSimbolos destroyTSG() {
        return listaTablaSimbolos.remove(0);
    }

    public TablaSimbolos destroyTSL(int id) {
        return listaTablaSimbolos.remove(id);
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String nuevaRutaArchivo) {
        rutaArchivo = nuevaRutaArchivo;
    }

    public Map<Integer, TablaSimbolos> getListaTablaSimbolos() {
        return listaTablaSimbolos;
    }

    public void tablaSimbolosToFile(int id, String rutaTS) {
        TablaSimbolos tabla = listaTablaSimbolos.get(id);
        if (tabla == null) {
            System.err.println("No existe la tabla con id " + id);
            return;
        }

        if (tabla.getidEntradas() == null || tabla.getidEntradas().isEmpty()) {
            return;
        }

        try (FileWriter writer = new FileWriter(rutaTS, true)) {

            if (id < 1) {
                writer.write("TSG #" + tabla.getID() + ":\n");
            } else {
                writer.write("TSL " + tabla.getNombre() + " #" + tabla.getID() + ":\n");
            }

            List<EntradaTS> entradas = new ArrayList<>(tabla.getidEntradas().values());
            entradas.sort(Comparator.comparingInt(EntradaTS::getDesp));

            for (EntradaTS eentrada : entradas) {

                if (!eentrada.getTipo().equals("-")) {

                    writer.write("* lexema: '" + eentrada.getLexema() + "'\n");
                    if (!(eentrada.getTipo().equals("-"))) {

                        writer.write("+ tipo : '" + eentrada.getTipo() + "'\n");

                        if (eentrada.getTipo().equals("function")) {
                            writer.write("+ numParam : " + eentrada.getNumParam() + "\n");

                            for (int i = 0; i < eentrada.getTiposParam().size(); i++) {
                                String tipoParam = eentrada.getTiposParam().get(i);
                                Integer modoParam = (eentrada.getModosParam().size() > i)
                                        ? eentrada.getModosParam().get(i)
                                        : null;
                                writer.write(String.format("+ TipoParam" + (i + 1) + " : '%s'\n", tipoParam));
                                writer.write(String.format("+ ModoParam" + (i + 1) + " : %s\n", modoParam));
                            }

                            writer.write("+ tipoRetorno : '" + eentrada.getTipoRetorno() + "'\n");
                            writer.write("+ EtiqFuncion : '" + eentrada.getEtiquetaFuncion() + "'\n");

                        } else {

                            writer.write("+ despl : " + eentrada.getDesp() + "\n");

                        }
                    }

                    writer.write("\n");
                }
            }

            writer.write("\n");

            if (id < 1) {
                System.out.println("Archivo tablaSimbolos.txt actualizado correctamente, Tabla de Simbolos : TSG #"
                        + tabla.getID() + " añadida correctamente al archivo tablaSimbolos.txt en " + rutaTS);
            } else {
                System.out.println("Archivo tablaSimbolos.txt actualizado correctamente, Tabla de Simbolos : TSL "
                        + tabla.getNombre() + " #" + tabla.getID()
                        + " añadida correctamente al archivo tablaSimbolos.txt en " + rutaTS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}