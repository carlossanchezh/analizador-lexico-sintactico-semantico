package lexico;

public class Token {
  private TokenType type;
  private String lexema;
  private int linea;
  private int columna;

  public Token(TokenType type, String lexema, int linea, int columna) {
    this.type = type;
    this.lexema = lexema;
    this.linea = linea;
    this.columna = columna;
  }

  public TokenType getType() {
    return type;
  }

  public String getLexema() {
    return lexema;
  }

  public int getLinea() {
    return linea;
  }

  public int getColumna() {
    return columna;
  }

  @Override
  public String toString() {
    return "<" + type + ", " + lexema + ">";
  }
}
