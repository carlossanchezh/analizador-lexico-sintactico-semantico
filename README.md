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

## Gramáticas del Proyecto

Todas las gramáticas están documentadas en `docs/gramaticas/`:

| Fase | Archivo | Descripción |
|------|---------|-------------|
| **Léxica** | [`docs/gramaticas/lexico/gramatica.txt`](docs/gramaticas/lexico/gramatica.txt) | Gramática regular del AFD del analizador léxico |
| **Sintáctica** | [`docs/gramaticas/sintactico/gramatica.txt`](docs/gramaticas/sintactico/gramatica.txt) | Gramática LL(1) del analizador sintáctico |
| **Sintáctica (análisis)** | [`docs/gramaticas/sintactico/gramatica-analisisLL1(1).txt`](docs/gramaticas/sintactico/gramatica-analisisLL1(1).txt) | Análisis LL(1) generado con SDGLL1 |
| **Sintáctica (tabla)** | [`docs/gramaticas/sintactico/tablaSintactica.html`](docs/gramaticas/sintactico/tablaSintactica.html) | Tabla sintáctica LL(1) generada con SDGLL1 |
| **Semántica** | [`docs/gramaticas/semantico/gramatica.txt`](docs/gramaticas/semantico/gramatica.txt) | Gramática de traducción dirigida por la sintaxis |

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

- Recorre el código carácter a carácter, siguiendo una [gramática AFD](docs/gramaticas/lexico/gramatica.txt).

- Reconoce identificadores, palabras reservadas, constantes enteras/reales, cadenas, operadores y símbolos.

- Valida rangos: enteros máx. 32767, reales máx. 117549436.0, cadenas máx. 64 caracteres.

- Inserta identificadores en la tabla de símbolos global.

- Maneja comentarios de bloque /* */ y errores léxicos (comentario/cadena sin cerrar, número mal formado, carácter no reconocido).

- En caso de error léxico, devuelve el error en `files/errores.txt` y detiene el análisis.

- Devuelve tokens con su posición (id en la tabla) en `files/tokens.txt`.

#### Analizador Sintáctico (`AnalizadorSintactico`)

- Implementa un parser descendente recursivo LL(1) basado en la [gramática del lenguaje](docs/gramaticas/sintactico/gramatica.txt).

- La gramática es LL(1) comprobado a través de un [análisis](docs/gramaticas/sintactico/gramatica-analisisLL1(1).txt) realizado por la herramienta SDGLL1.

- Utiliza la [tabla sintáctica](docs/gramaticas/sintactico/tablaSintactica.html)  generada con la herramienta SDGLL1.

- Escribe en `parse.txt` la secuencia de reglas de producción aplicadas (Desc 1 4 8 ...).

- En caso de error sintáctico, devuelve el error en `files/errores.txt` y detiene el análisis.

#### Analizador Semántico (`AnalizadorSemantico`)

- Implementa una [gramática](docs/gramaticas/semantico/gramatica.txt) basada en la traducción dirigida por la sintaxis con atributos heredados y sintetizados.

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

- Cada entrada en la Tabla de Símbolos (EntradaTS) almacena: `id`, `lexema`, `tipo`, `desplazamiento`, `tabla`, y para funciones: `numParam`, `tiposParam`, `modosParam`, `tipoRetorno`, `etiqFuncion`.

- El gestor permite crear, destruir, consultar y volcar tablas a `tablaSimbolos.txt`.

#### Gestor de Errores (`GestorErrores`)

- Centraliza la notificación de errores léxicos, sintácticos y semánticos.

- Los errores léxicos y sintácticos se escriben inmediatamente en `files/errores.txt` y detienen la ejecución.

- Los errores semánticos se acumulan y se escriben al final en `files/errores.txt`.

### Funcionamiento

El compilador sigue un flujo de **análisis por fases** donde cada etapa consume la salida de la anterior. A continuación se describe el proceso completo desde que se selecciona un archivo hasta que se generan los resultados.

#### Descripción Detallada del Flujo

##### 1. Selección y Preparación

El programa comienza en `Main.java`:

- **Creación del directorio `files/`** si no existe.

- **Selección del archivo de entrada** mediante `JFileChooser` (interfaz Swing).

- **Limpieza de salidas previas**: se eliminan `errores.txt`, `parse.txt`, `tablaSimbolos.txt` y `tokens.txt` para evitar mezclar resultados de ejecuciones anteriores.

- **Lectura del código fuente** completo en un `String`.

##### 2. Inicialización de Componentes Compartidos

Se crean dos componentes que serán usados por las tres fases:

- **`GestorErrores`**: apunta a `files/errores.txt`, acumula errores semánticos y escribe errores léxicos/sintácticos inmediatamente.

- **`GestorTablas`**: crea la tabla global `TSG #0` y gestiona las tablas locales que se creen al entrar en funciones.

Ambos se pasan como parámetros a los analizadores, por lo que **todas las fases comparten el mismo estado**.

##### 3. Fase 1 — Análisis Léxico

Se instancia un `AnalizadorLexico` con el código, el `GestorTablas` y el `GestorErrores`.

- `TokensToFile.tokenizar()` recorre el código llamando a `lexer.getToken()` en bucle hasta obtener `eof`.

- Cada token se escribe en `files/tokens.txt` con formato `<tipo, lexema>`.

- Los identificadores se insertan en la **TSG** (a través de `GestorTablas`) y el token guarda su `id` como lexema.

- Si se detecta un error léxico, se llama a `gestorErrores.errorLexico()` que escribe el mensaje en `files/errores.txt` y se detiene la ejecución.

##### 4. Fase 2 — Análisis Sintáctico

Se instancia un `AnalizadorSintactico`, compartiendo los mismos `GestorTablas` y `GestorErrores`.

- El parser comienza con `A_Sint()`, que lee el primer token y llama a `S()`.

- Se implementa un **parser descendente recursivo LL(1)**: cada no terminal tiene su método (`A()`, `B()`, `C()`, ...).

- La función `equipara(token)` consume el token actual si coincide con el esperado; en caso contrario, invoca `gestorErrores.errorSintactico()`.

- Cada regla aplicada se escribe en `files/parse.txt` con el número de producción (`Desc 1 4 8 ...`).

- Si hay error sintáctico, se llama a `gestorErrores.errorSintactico()` que escribe en `files/errores.txt` y detiene la ejecución.

##### 5. Fase 3 — Análisis Semántico

Se instancia  el `AnalizadorSemantico`, también con los mismos gestores compartidos `GestorTablas` y `GestorErrores`.

- El método `A_Sm()` inicializa el token y llama a `S()`.
  
- Se implementa la **traducción dirigida por la sintaxis**: cada método devuelve un objeto `Nodo` con atributos (`tipo`, `ancho`, `numParam`, `tipoParametros`, `tipoRetorno`, etc.).

- Se gestionan ámbitos **a través del `GestorTablas`**:
  - Al entrar en una función, se crea una **TSL** con `gestorTablas.crearTSLocal()`.
  - Al salir, se vuelca a `tablaSimbolos.txt` y se destruye con `destroyTSL()`.

- Se realizan comprobaciones semánticas y, si hay errores, se acumulan en `erroresSemanticos` mediante `gestorErrores.errorSemantico()`.

- Al finalizar `S()`, se vuelca la TSG a `tablaSimbolos.txt` y se llama a `gestorErrores.guardarErrores()` para escribir todos los errores semánticos acumulados.

##### 6. Salidas Generadas

| Archivo | Contenido | Cuándo se genera |
|---------|-----------|------------------|
| `files/tokens.txt` | Lista de tokens reconocidos | Tras la fase léxica |
| `files/parse.txt` | Traza de producciones aplicadas | Durante la fase sintáctica |
| `files/tablaSimbolos.txt` | Tablas TSG y TSL | Durante/después de la fase semántica |
| `files/errores.txt` | Errores detectados | Inmediatamente (léx/sint) o al final (semánticos) |

#### Diagrama de Flujo 

```plaintext
                        ┌─────────────────────┐
                        │  Selección archivo  │  JFileChooser → archivo.txt
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │  Limpieza de files/ │  Se eliminan tokens.txt, parse.txt,
                        │                     │  tablaSimbolos.txt y errores.txt de compilaciones anteriores
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │  Lectura del código │  Files.readAllBytes() → String
                        └──────────┬──────────┘
                                   │
                                   ▼
              ┌────────────────────────────────────────────┐
              │        Inicialización de componentes       │
              │  ┌──────────────────┐  ┌─────────────────┐ │
              │  │  GestorErrores   │  │  GestorTablas   │ │
              │  │                  │  │  (crea TSG #0)  │ │
              │  └────────┬─────────┘  └────────┬────────┘ │
              └───────────┼─────────────────────┼──────────┘
                          │                     │
                     (compartidos por todas las fases)
                          │                     │
                          ▼                     ▼
              ┌────────────────────────────────────────────┐
              │            FASE 1: Análisis Léxico         │
              │  AnalizadorLexico + TokensToFile           │
              │  → files/tokens.txt                        │
              │  → Inserta identificadores en TSG #0       │
              └───────────────────┬────────────────────────┘
                                  │  ¿Error léxico?
                                  │  ├─ SÍ ─► GestorErrores.errorLexico()
                                  │  │        → files/errores.txt
                                  │  │        → System.exit(1)
                                  │  └─ NO ─► continúa
                                  ▼
              ┌────────────────────────────────────────────┐
              │         FASE 2: Análisis Sintáctico        │
              │  AnalizadorSintactico                      │
              │  → files/parse.txt                         │
              └───────────────────┬────────────────────────┘
                                  │  ¿Error sintáctico?
                                  │  ├─ SÍ ─► GestorErrores.errorSintactico()
                                  │  │        → files/errores.txt
                                  │  │        → System.exit(1)
                                  │  └─ NO ─► continúa
                                  ▼
              ┌────────────────────────────────────────────┐
              │         FASE 3: Análisis Semántico         │
              │  AnalizadorSemantico                       │
              │  → Crea/imprime/destruye TSL GestorTablas  │ 
              │  → files/tablaSimbolos.txt                 │
              └───────────────────┬────────────────────────┘
                                  │  ¿Error semántico?
                                  │  └─ SÍ ─► GestorErrores.errorSemantico()
                                  │           (acumula en lista)
                                  ▼
              ┌────────────────────────────────────────────┐
              │           Volcado final de errores         │
              │  GestorErrores.guardarErrores()            │
              │  → files/errores.txt (si hay errores sem.) │
              └────────────────────────────────────────────┘
```

### Tecnologías

- Lenguaje: **Java**
- Interfaz Gráfica: **Java Swing (JFileChooser)**

## Casos de Prueba

El proyecto incluye una batería de casos de prueba organizados en [`docs/pruebas/`](docs/pruebas), divididos en [**`correctas/`**](docs/pruebas/correctas) e [**`erroneas/`**](docs/pruebas/erroneas).

### Casos Correctos

| Caso | Objetivo | Características que demuestra |
|------|----------|-------------------------------|
| **Prueba 1** | Recursividad y paso de parámetros | Función recursiva `sumarRango`, retorno de valores `int`, llamadas anidadas |
| **Prueba 2** | Bucle `while` con lógica booleana | Variables globales, variable local dentro del bucle, operadores `==`, `\|\|`, `write` con cadenas y variables |
| **Prueba 3** | Control de flujo con booleanos | Variable `boolean` que controla un `while`, actualización de variables globales, condicional `if` anidado |
| **Prueba 4** | Gestión de ámbitos y solapamiento | Variable local `x` que oculta a la global `x`, llamada anidada `operacion(operacion(x))` |
| **Prueba 5** | Integración completa | Múltiples funciones (`suma`, `comparaciones`, `print`, `getN`, `setN`, `setNPorPantalla`, `contador`, `main`), tipos `void`, `read`, `write`, aritmética compleja, operadores lógicos y relacionales |

### Casos erroneos 

| Caso | Tipo de Error | Descripción | Mensaje esperado en `errores.txt` |
|------|---------------|-------------|-----------------------------------|
| **Prueba 6** | Semántico | Asignación de `string` a `int` (literal) | `Asignación incompatible se esperaba una asignacion de tipo [ int = int ] pero se encontro una asignacion de tipo [ int = string ]` |
| **Prueba 7** | Semántico | Asignación de `string` a `int` (variable) | `Asignación incompatible se esperaba una asignacion de tipo [ int = int ] pero se encontro una asignacion de tipo [ int = string ]` |
| **Prueba 8** | Semántico | Uso de función como variable sin paréntesis | `Uso indebido de funcion, 'calcular' es funcion y no puede usarse como variable` + errores en cadena |
| **Prueba 9** | Léxico | Cadena sin cerrar / supera 64 caracteres | `Error Lexico: superados los caracteres maximos de una cadena (64)` |
| **Prueba 10** | Sintáctico | `while` sin llaves `{ }` | `Error Sintáctico: Se esperaba: '{' Pero se encontro un identificador (contador)` |

### Estructura de Cada Caso de Prueba

Cada carpeta de prueba contiene:

```
pruebaN/
├── entrada.txt              # Código MyJS de entrada
├── tokens.txt               # Salida esperada de la fase léxica
├── parse.txt                # Salida esperada de la fase sintáctica
├── tablaSimbolos.txt        # Salida esperada de la fase semántica
└── errores.txt              # Salida esperada (solo en casos incorrectos)
```
> Los casos incorrectos solo generan los archivos de las fases que se completaron antes del error.

## Estructura del Proyecto

```plaintext
.
├── docs/
│   ├── gramaticas/
│   │   ├── lexico/
│   │   │   └── gramatica.txt          # Gramática regular del analizador léxico (AFD)
│   │   │
│   │   ├── semantico/
│   │   │   └── gramatica.txt          # Gramática de traducción dirigida por la sintaxis
│   │   │
│   │   └── sintactico/
│   │       ├── gramatica.txt          # Gramática LL(1) del analizador sintáctico
│   │       ├── gramatica-analisisLL1(1).txt # Análisis LL(1) generado con SDGLL1
│   │       └── tablaSintactica.html   # Tabla sintáctica LL(1) generada con SDGLL1
│   │ 
│   └── pruebas/
│       ├── correctas/                 # Casos de prueba que compilan sin errores
│       │   ├── prueba1/
│       │   ├── prueba2/
│       │   ├── ...
│       │   └── prueba5/
│       │
│       └── incorrectas/               # Casos de prueba que generan errores
│           ├── prueba6/
│           ├── prueba7/
│           ├── ...
│           └── prueba10/
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
