# Snail

<p align="center">
  <img src="icon/snailtracker_icon.png" alt="Logo de Snail" width="160" />
</p>

<p align="center">Strength Notes &amp; Activity Improvement Log</p>

App Android para crear rutinas de fuerza y registrar entrenamientos. Los ejercicios, rutinas y entrenamientos se guardan en el dispositivo y permanecen tras reiniciar la app.

<p align="center">
  <a href="https://github.com/jstihl01/Snail/releases/latest/download/Snail.apk">
    <img src="https://img.shields.io/badge/⬇_DESCARGAR_APK-Última_versión-2d7048?style=for-the-badge" alt="Descargar APK — Última versión" />
  </a>
</p>

Android 7.0 o superior. Descarga el APK, ábrelo y permite la instalación desde esa fuente si Android lo solicita.

## Crear una rutina

1. En el inicio, pulsa **+ Rutina**.
2. Escribe el **Nombre de la Rutina**.
3. Pulsa **Añadir Ejercicio** para abrir el catálogo.
4. Si necesitas un ejercicio nuevo, pulsa **Crear Ejercicio**, escribe su nombre y confírmalo con el teclado o al salir del campo. El nombre pasa a ser texto no editable y el ejercicio queda seleccionado.
5. Toca los ejercicios que quieras seleccionar o desmarcar y pulsa **Añadir**. Puedes seleccionar varios y volver al catálogo para añadir más, incluso repetir ejercicios.
6. De vuelta en la rutina, completa **Series** y **Reps.** de cada ejercicio. **RIR** (repeticiones en reserva) es opcional. La papelera retira un ejercicio de esta rutina sin borrarlo del catálogo.
7. Pulsa **Guardar** para conservar la rutina y volver al inicio. Se habilita con un nombre válido, al menos un ejercicio y Series y Reps. completas en todos ellos.

El catálogo se ordena alfabéticamente al abrirlo y al confirmar un ejercicio. **Añadir** queda deshabilitado mientras haya uno en creación; **Crear Ejercicio**, mientras haya uno sin nombre válido. Cancelar o añadir descarta los nombres vacíos o sólo con espacios y limpia la selección.

## Empezar un entrenamiento

1. En el inicio, pulsa **+ Entrenamiento**. Se habilita cuando hay alguna rutina guardada.
2. Consulta las rutinas y sus tablas de ejercicios, Series, Reps. y RIR.
3. Opcionalmente, pulsa la **paleta** de una rutina para alternar entre gris y siete colores. El color se conserva y se aplica a los ejercicios durante el entrenamiento y a todo su historial, incluidos los entrenamientos anteriores.
4. Toca una rutina para seleccionarla: su fondo pasa a blanco. Tócala otra vez para desmarcarla o elige otra para sustituir la selección.
5. Pulsa **Empezar**, disponible cuando hay una rutina seleccionada.

## Registrar las series

1. Cada ejercicio muestra una fila por serie, con **Serie**, **KG**, **Reps.** y el **RIR** previsto, no editable.
2. Introduce los **KG** y las **Reps.** realizadas. El placeholder de KG muestra el valor de esa serie y ejercicio del último entrenamiento de la rutina, o `...` si no existe. El de Reps. muestra el objetivo definido en la rutina.
3. Desplázate por la lista para completar las series realizadas. Los valores de referencia no cuentan como datos introducidos.
4. Pulsa **Guardar** para volver al inicio. Se habilita cuando al menos una serie tiene KG y Reps.; sólo se guardan las series con ambos campos completos.

## Consultar el historial

El inicio muestra los entrenamientos guardados con **fecha y hora** encima de cada recuadro. Dentro aparecen la rutina y sus ejercicios, con el número de cada serie, KG y repeticiones realizadas.

Los entrenamientos más recientes quedan abajo, sobre **+ Rutina** y **+ Entrenamiento**. Puedes desplazarte para consultar los anteriores; una línea separa los de distintas semanas.

## Cancelar o borrar

- **Cancelar** o el botón atrás vuelve a la pantalla anterior. En una rutina o entrenamiento con datos, pide confirmación antes de descartar el borrador; cancelar la rutina la deja vacía para empezar desde cero.
- Las **papeleras** del catálogo, las rutinas y el historial piden confirmación antes de borrar permanentemente el elemento correspondiente.
- En una confirmación, **Sí** ejecuta la acción; **No** o el botón atrás la cierra sin cambios. El resto de la pantalla queda bloqueado.

## Datos e interfaz

Los ejercicios del catálogo se reutilizan entre rutinas. Ejercicios, rutinas, colores y entrenamientos se almacenan localmente y permanecen tras reiniciar la app.

La interfaz utiliza fondo negro, texto claro y listas desplazables. Series, Reps., RIR y KG abren un teclado numérico.
