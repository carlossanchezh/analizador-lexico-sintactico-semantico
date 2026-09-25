# Instrucciones de instalación y ejecución

## Requisitos

- **Java** 8 o superior

- **IDE** recomendado

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/carlossanchezh/analizador-lexico-sintactico-semantico.git
```
> Si no usas Git, descarga el `.zip` del proyecto y descomprímelo en una carpeta local.

## Ejecución

### Desde la línea de comandos

1. Accede al directorio donde haya sido clonado el proyecto

2. Compilar todos los archivos `.java`:

```bash
javac -d out -sourcepath src src/Main.java
```

> En Windows:

```bash
javac -d out -sourcepath src src\Main.java
```

3. Ejecutar el analizador

```bash
java -cp out Main
```

> En Windows:

```bash
java -cp out Main
```

Al ejecutarse, se abrirá un diálogo `JFileChooser` para seleccionar el archivo `.txt` con código MyJS a analizar.

### Desde un IDE

1. Abre el proyecto en tu IDE.

2. Ejecuta la clase principal `Main` (botón derecho → Run 'Main.main()').

## Uso del compilador

Al iniciar la aplicación se abre un **diálogo Swing** (`JFileChooser`) con el título *"Selecciona el archivo .txt a analizar"*:

- **Selector de archivos** — navega por el sistema y selecciona un archivo `.txt` con código MyJS.

- **Filtro de archivos** — solo muestra archivos con extensión `.txt`.

- **Botón Abrir** — inicia el análisis del archivo seleccionado.

- **Botón Cancelar** — cierra el diálogo sin analizar nada y termina el programa.

Una vez seleccionado el archivo, el compilador ejecuta automáticamente las tres fases del análisis (léxica, sintáctica y semántica) y genera los archivos de salida en la carpeta `files/`. Consulta la sección [**Salidas Generadas**](README.md#6-salidas-generadas) del README para más detalles.

El proyecto cuenta con una batería de casos de prueba para comprobar su correcto funcionamiento. Puedes consultar la sección [**Casos de Prueba**](README.md#casos-de-prueba) del README para más detalles.
