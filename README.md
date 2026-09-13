# Snail

App Android para crear rutinas de fuerza y registrar entrenamientos. Los ejercicios, rutinas y entrenamientos se guardan en el dispositivo y permanecen tras reiniciar la app.

<p align="center">
  <a href="https://github.com/jstihl01/Snail/releases/latest/download/Snail.apk">
    <img src="https://img.shields.io/badge/⬇_DESCARGAR_APK-Última_versión-2d7048?style=for-the-badge" alt="Descargar APK — Última versión" />
  </a>
</p>

Android 7.0 o superior. Descarga el APK, ábrelo y permite la instalación desde esa fuente si Android lo solicita.

## Funcionamiento

- **Ejercicios:** crea un nombre y confírmalo con el teclado o al salir del campo. Selecciona uno o varios tocándolos para añadirlos a una rutina, incluso repetidos. El catálogo se ordena alfabéticamente al abrirlo y al confirmar un ejercicio; los ejercicios sin nombre válido se descartan al cancelar o añadir. No se puede añadir mientras haya uno en creación.
- **Rutinas:** pulsa **+ Rutina**, escribe un nombre y añade ejercicios. Define **Series** y **Reps.** para cada uno; **RIR** (repeticiones en reserva) es opcional. Puedes retirar ejercicios. Guardar exige un nombre, al menos un ejercicio y todos los valores obligatorios completos.
- **Selección y colores:** **+ Entrenamiento**, disponible sólo si hay rutinas, muestra sus ejercicios y valores en tablas. Selecciona una rutina y pulsa **Empezar**. La paleta alterna entre gris y siete colores; cambiarlo actualiza también sus entrenamientos anteriores y los fondos durante el entrenamiento.
- **Entrenamiento:** cada ejercicio muestra una fila por serie con su número, campos numéricos **KG** y **Reps.**, y el **RIR** previsto. KG muestra como referencia el valor de la misma serie del último entrenamiento de esa rutina, o `...`; Reps. muestra el objetivo de la rutina. Guardar registra sólo las series con ambos campos completados y vuelve al inicio; requiere al menos una serie completa.
- **Historial:** el inicio muestra los entrenamientos con fecha, hora, ejercicios, series, KG y repeticiones. Los más recientes quedan abajo, sobre los botones principales; una línea separa semanas distintas.
- **Borrado y cancelación:** las papeleras del catálogo, las rutinas y el historial piden confirmación antes de borrar permanentemente. Cancelar una rutina o un entrenamiento con datos pide confirmación y descarta el borrador. Cancelar o añadir ejercicios limpia su selección. El botón atrás vuelve a la pantalla anterior; en una confirmación la cierra sin ejecutar la acción.
- **Interfaz:** fondo negro, texto claro, listas desplazables y teclado numérico para Series, Reps., RIR y KG. Las confirmaciones bloquean el resto de la pantalla.
