package tablas;

import java.util.ArrayList;
import java.util.List;

public class EntradaTS {

    protected static Integer contadorEntrada = 0;
    private Integer id;
    private String lexema;
    private Object tipo;
    private Integer desp;
    private Integer tabla;

    private boolean esFuncion = false;
    private Integer numParam;
    private List<String> tiposParam;
    private List<Integer> modosParam;
    private String tipoRetorno;
    private String etiqFuncion;

    public EntradaTS(String lexema) {

        this.setId(EntradaTS.contadorEntrada);
        EntradaTS.contadorEntrada++;

        this.lexema = lexema;
        this.tipo = "-";
        this.desp = -1;
        this.tiposParam = new ArrayList<>();
        this.modosParam = new ArrayList<>();

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public Object getTipo() {
        return tipo;
    }

    public void setTipo(Object tipo) {
        this.tipo = tipo;
    }

    public Integer getDesp() {
        Integer ret;
        if (this.desp == null) {
            ret = null;
        } else {
            ret = desp;
        }
        return ret;
    }

    public void setDesp(Integer desp) {
        this.desp = desp;
    }

    public Integer getTabla() {
        return tabla;
    }

    public void setTabla(Integer tabla) {
        this.tabla = tabla;
    }

    public void marcarComoFuncion() {
        this.esFuncion = true;
        this.setTipo("function");
    }

    public boolean esFuncion() {
        return esFuncion;
    }

    public void setNumParam(int n) {
        this.numParam = n;
    }

    public void addTipoParam(String tipo) {
        this.tiposParam.add(tipo);
    }

    public void resetTipoParam() {
        this.tiposParam = new ArrayList<>();
    }

    public void addModoParam(Integer tipo) {
        this.modosParam.add(tipo);
    }

    public void resetModoParam() {
        this.modosParam = new ArrayList<>();
    }

    public void setTipoRetorno(String tipoRetorno) {
        this.tipoRetorno = tipoRetorno;
    }

    public void setEtiquetaFuncion(String etiqueta) {
        this.etiqFuncion = etiqueta;
    }

    public Integer getNumParam() {
        return numParam;
    }

    public List<String> getTiposParam() {
        return tiposParam;
    }

    public List<Integer> getModosParam() {
        return modosParam;
    }

    public String getTipoRetorno() {
        return tipoRetorno;
    }

    public String getEtiquetaFuncion() {
        return etiqFuncion;
    }

}