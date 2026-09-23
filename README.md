# Analizador léxico, sintáctico y semántico MyJS (JS simplificado)

## Descripción

Este proyecto implementa un procesador de lenguaje completo desarrollado en **Java** para un lenguaje de programación imperativo simplificado (**MyJS**) inspirado en **JavaScript**. El proyecto implementa las tres fases clásicas del análisis de un compilador:

- **Análisis Léxico** — tokenización del código fuente.
- **Análisis Sintáctico** — validación mediante una gramática LL(1) y construcción del parse.
- **Análisis Semántico** — comprobación de tipos, gestión de ámbitos y funciones, y traducción dirigida por la sintaxis.

El compilador procesa un archivo `.txt` con código **MyJS** y genera como salida:

- `files/tokens.txt` — lista de tokens reconocidos.

- `files/parse.txt` — traza de las reglas de producción aplicadas.

- `files/tablaSimbolos.txt` — tablas de símbolos (global y locales).

Si encuentra algún error generará los respectivos `.txt` de las fases completadas y el archivo:

- `files/errores.txt` — errores léxicos, sintácticos y semánticos detectados.

## Tokens MyJS

Definición completa de los tokens reconocidos por el analizador léxico:

| Elemento | Código de Token | Atributo |
|----------|----------------|----------|
| `boolean` | `PalResBoolean` | - |
| `float` | `PalResFloat` | - |
| `function` | `PalResFunction` | - |
| `if` | `PalResIf` | - |
| `int` | `PalResInt` | - |
| `let` | `PalResLet` | - |
| `read` | `PalResRead` | - |
| `return` | `PalResReturn` | - |
| `string` | `PalResString` | - |
| `void` | `PalResVoid` | - |
| `while` | `PalResWhile` | - |
| `write` | `PalResWrite` | - |
| Constante real | `cteR` | Número |
| Constante entera | `cteE` | Número |
| Cadena (`"`) | `cad` | Cadena (`"c*"`) |
| Identificador | `id` | Número |
| `=` | `asignacion` | - |
| `\|=` | `olog` | - |
| `,` | `coma` | - |
| `;` | `puntoycoma` | - |
| `(` | `ParIzq` | - |
| `)` | `ParDcha` | - |
| `{` | `LlaveIzq` | - |
| `}` | `LlaveDcha` | - |
| Operadores Aritméticos (`+`) | `OPArSuma` | - |
| Operadores Lógicos (`\|\|`) | `OPLogO` | - |
| Operadores Relacionales (`==`) | `OPRIgual` | - |
| EOF | `eof` | - |

> Los atributos con `-` indican que el token no lleva valor asociado.

## Tabla de Símbolos

### Campos generales

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | `Integer` | Identificador único de la entrada |
| `lexema` | `String` | Nombre del identificador |
| `tipo` | `Object` | Tipo del símbolo (`int`, `float`, `boolean`, `string`, `function`, `-`) |
| `desp` | `Integer` | Desplazamiento en memoria |
| `tabla` | `Integer` | ID de la tabla a la que pertenece |

### Campos adicionales para funciones 

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `esFuncion` | `boolean` | Indica si el símbolo es una función |
| `numParam` | `Integer` | Número de parámetros |
| `tiposParam` | `List<String>` | Tipos de cada parámetro |
| `modosParam` | `List<Integer>` | Modos de paso de cada parámetro |
| `tipoRetorno` | `String` | Tipo de retorno de la función |
| `etiqFuncion` | `String` | Etiqueta interna para generación de código |

### Desplazamiento 

El desplazamiento (desp) es un valor numérico que indica la posición relativa en memoria que ocupa cada identificador dentro de su ámbito (global o local). Se calcula sumando el ancho del tipo de cada variable declarada previamente en el mismo ámbito.

| Tipo | Ancho (desplazamiento) |
|------|-----------------------------------|
| `int` | 1 |
| `float` | 2 |
| `boolean` | 1 |
| `string` | 64 |
| `function` | — (no ocupa desplazamiento) | 

### Formato de salida (`tablaSimbolos.txt`)

```plaintext

TSL nombreFuncion #1:
* lexema: 'parametro1'
+ tipo : 'tipo'
+ despl : 0

* lexema: 'variableLocal'
+ tipo : 'tipo'
+ despl : N


TSG #0:
* lexema: 'nombreFuncion'
+ tipo : 'function'
+ numParam : N
+ TipoParam1 : 'tipo'
+ ModoParam1 : 1
+ TipoParam2 : 'tipo'
+ ModoParam2 : 1
...
+ TipoParamN : 'tipo'
+ ModoParamN : 1
+ tipoRetorno : 'tipoRetorno'
+ EtiqFuncion : 'etq_nombreFuncionN'

* lexema: 'variableGlobal1'
+ tipo : 'tipo'
+ despl : 0

* lexema: 'variableGlobal2'
+ tipo : 'tipo'
+ despl : N

```

> La tabla global siempre tiene el identificador #0. Las tablas locales se numeran de forma incremental (#1, #2, ...).

## Arquitectura

El proyecto sigue una arquitectura modular basada en **análisis por fases**, donde cada componente es independiente y se comunica a través de un **gestor de errores** centralizado y un **gestor de tablas de símbolos** compartido.

### Componentes principales

#### Analizador Léxico (`AnalizadorLexico`)

- Recorre el código carácter a carácter.

- Reconoce identificadores, palabras reservadas, constantes enteras/reales, cadenas, operadores y símbolos.

- Valida rangos: enteros máx. 32767, reales máx. 117549436.0, cadenas máx. 64 caracteres.

- Inserta identificadores en la tabla de símbolos global.

- Maneja comentarios de bloque /* */ y errores léxicos (comentario/cadena sin cerrar, número mal formado, carácter no reconocido).

- En caso de error léxico, devuelve el error en `files/errores.txt` y detiene el análisis.

- Devuelve tokens con su posición (id en la tabla) en `files/tokens.txt`.

#### Analizador Sintáctico (`AnalizadorSintactico`)

- Implementa un parser descendente recursivo LL(1) basado en la gramática del lenguaje.

- Utiliza la tabla sintáctica generada con la herramienta SDGLL1.

- Escribe en `parse.txt` la secuencia de reglas de producción aplicadas (Desc 1 4 8 ...).

- En caso de error sintáctico, devuelve el error en `files/errores.txt` y detiene el análisis.

#### Analizador Semántico (`AnalizadorSemantico`)

- Implementa la traducción dirigida por la sintaxis con atributos heredados y sintetizados.

- Gestiona ámbitos mediante tablas de símbolos locales (TSL) y global (TSG).

- Realiza comprobaciones de:
    -   Declaración duplicada de identificadores.
    -   Asignaciones compatibles de tipos.
    -   Operadores aritméticos (+), relacionales (==) y lógicos ( || , |= ).
    -   Llamadas a funciones: número y tipo de parámetros, tipo de retorno.
    -   Uso correcto de return dentro de funciones.
    -   Restricciones de read y write.

- Almacena errores semánticos encontrados en una lista y los vuelca a `files/errores.txt` al finalizar.    

#### Gestor de Tablas (`GestorTablas` / `TablaSimbolos` / `EntradaTS`)

- TSG: tabla global creada al inicio.

- TSL: tabla local creada al entrar en una función.

- Cada entrada en la Tabla de Simbolos (EntradaTS) almacena: `id`, `lexema`, `tipo`, `desplazamiento`, `tabla`, y para funciones: `numParam`, `tiposParam`, `modosParam`, `tipoRetorno`, `etiqFuncion`.

- El gestor permite crear, destruir, consultar y volcar tablas a `tablaSimbolos.txt`.

#### Gestor de Errores (`GestorErrores`)

- Centraliza la notificación de errores léxicos, sintácticos y semánticos.

- Los errores léxicos y sintácticos se escriben inmediatamente en `files/errores.txt` y detienen la ejecución.

- Los errores semánticos se acumulan y se escriben al final en `files/errores.txt`.

### Tecnologías

- Lenguaje: **Java**
- Interfaz Gráfica: **Java Swing (JFileChooser)**

## Estructura del Proyecto

```plaintext
.
├── docs/
│   ├── casosDePrueba/
│   │   ├── correctos/                 # Casos de prueba que compilan sin errores
│   │   │   ├── prueba1/
│   │   │   ├── prueba2/
│   │   │   ├── ...
│   │   │   └── prueba5/
│   │   │
│   │   └── incorrectos/               # Casos de prueba que generan errores
│   │       ├── prueba6/
│   │       ├── prueba7/
│   │       ├── ...
│   │       └── prueba10/
│   │ 
│   └── gramaticas/
│       ├── lexico/
│       │   └── gramatica.txt          # Gramática regular del analizador léxico (AFD)
│       │
│       ├── semantico/
│       │   └── gramatica.txt          # Gramática de traducción dirigida por la sintaxis
│       │
│       └── sintactico/
│           ├── gramatica.txt          # Gramática LL(1) del analizador sintáctico
│           ├── gramaticaAnalisisLL1(1).txt # Análisis LL(1) generado con SDGLL1
│           └── tablaSintactica.html   # Tabla sintáctica LL(1) generada con SDGLL1
│
├── src/
│   ├── errores/
│   │   └── GestorErrores.java         # Gestión centralizada de errores
│   │
│   ├── lexico/
│   │   ├── AnalizadorLexico.java      # Analizador léxico
│   │   ├── Token.java                 # Representación de un token
│   │   ├── TokenType.java             # Tipos de token (palabras reservadas, operadores, etc.)
│   │   └── TokensToFile.java          # Volcado de tokens a archivo
│   │
│   ├── sintactico/
│   │   └── AnalizadorSintactico.java  # Analizador sintáctico
│   │
│   ├── semantico/
│   │   └── AnalizadorSemantico.java   # Analizador semántico
│   │
│   └── tablas/
│   │   ├── EntradaTS.java             # Entrada individual de la tabla de símbolos
│   │   ├── GestorTablas.java          # Gestor de todas las tablas de símbolos
│   │   └── TablaSimbolos.java         # Tabla de símbolos (global o local)
│   │
│   └── Main.java                      # Punto de entrada, selección de archivo y orquestación
│
└── README.md                          # Descripción del proyecto 
```
