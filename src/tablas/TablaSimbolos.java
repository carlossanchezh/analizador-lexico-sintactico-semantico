package tablas;

import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {

    protected static Integer contadorTabla = 0;
    private Integer id;
    private Map<String, Integer> idEntradas;
    private Map<Integer, EntradaTS> idposEntradas;
    private String nombre;

    public TablaSimbolos() {

        this.setID(TablaSimbolos.contadorTabla);
        TablaSimbolos.contadorTabla++;

        this.idEntradas = new HashMap<String, Integer>();
        this.idposEntradas = new HashMap<Integer, EntradaTS>();

    }

    public Integer getID() {
        return id;
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public EntradaTS insertarTSG(String id, int idTS) {

        EntradaTS entrada = new EntradaTS(id);
        entrada.setTabla(idTS);

        this.idEntradas.put(id, entrada.getId());
        this.idposEntradas.put(entrada.getId(), entrada);

        return entrada;
    }

    public EntradaTS insertarTSL(String id, Integer idpos, int idTS) {

        EntradaTS entrada = new EntradaTS(id);
        entrada.setTabla(idTS);

        this.idEntradas.put(id, idpos);
        this.idposEntradas.put(idpos, entrada);

        return entrada;
    }

    public EntradaTS getEntradaPos(Integer idpos) {
        return this.idposEntradas.get(idpos);
    }

    public EntradaTS getEntradaId(String id) {

        Integer idpos = this.idEntradas.get(id);

        if (idpos != null) {

            return this.idposEntradas.get(idpos);

        } else {

            return null;
        }

    }

    public Map<Integer, EntradaTS> getidEntradas() {
        return idposEntradas;
    }

    public Map<String, Integer> getlexemasEntradas() {
        return idEntradas;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}