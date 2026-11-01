# Snail

<p align="center">
  <img src="assets/snail_icon-rounded.png" alt="Icono de Snail" width="160" />
</p>

<p align="center">Strength Notes &amp; Activity Improvement Log</p>

<p align="center">
  <a href="https://github.com/jstihl01/Snail/releases/latest/download/Snail.apk">
    <img src="assets/descargar-apk-android.svg" alt="Descargar APK para Android" width="240" />
  </a>
</p>

App Android para crear rutinas de fuerza y cardio, registrar entrenamientos y consultar su progreso. Los datos se guardan en el dispositivo y permanecen tras reiniciar la app.

Android 7.0 o superior. Descarga el APK, ábrelo y permite la instalación desde esa fuente si Android lo solicita.

## Crear una rutina

1. En el inicio, pulsa **+ Rutina**.
2. Escribe el **Nombre de la Rutina**.
3. Pulsa **Añadir Ejercicio** para abrir el catálogo.
4. Si necesitas un ejercicio nuevo, pulsa **Crear Ejercicio**. Se abre el teclado sobre su nombre, con la primera letra en mayúscula. Puedes añadir una **nota**, un **grupo muscular** y marcarlo como **Cardio**. Pulsa **Guardar**: queda seleccionado en el catálogo, ordenado alfabéticamente por grupos.
5. Toca los ejercicios en el orden en que quieras añadirlos: un número indica su posición. Puedes desmarcarlos, seleccionar varios y volver al catálogo para añadir más, incluso repetir ejercicios.
6. De vuelta en la rutina, completa **Series** y **Mín. Reps.**; **Máx. Reps.** y **RIR** son opcionales. En cardio, completa Series, **Intensidad** y **Minutos**. La papelera retira el ejercicio de esta rutina sin borrarlo del catálogo.
7. Pulsa **Guardar** para conservarla. Se habilita con un nombre válido, al menos un ejercicio y sus campos obligatorios completos.

El lápiz del catálogo abre el mismo formulario para editar un ejercicio. Guardarlo reemplaza la entrada del catálogo sin modificar las rutinas ni los entrenamientos que ya lo usaban. Cancelar la creación descarta el borrador; Cancelar o Añadir en el catálogo limpia la selección.

## Empezar un entrenamiento

1. En el inicio, pulsa **+ Entrenamiento**. Se habilita cuando hay alguna rutina guardada.
2. Consulta las rutinas y sus tablas de ejercicios. El icono **carpeta +** crea carpetas con nombre; puedes desplegar o contraer varias de forma independiente.
3. El menú **⋮** de cada rutina permite **editarla**, elegir entre gris y siete **colores**, **moverla arriba o abajo** dentro de su carpeta y **cambiarla de carpeta**. Su color se conserva y se aplica a sus entrenamientos, incluidos los anteriores.
4. Toca una rutina para seleccionarla: su fondo pasa a blanco. Tócala otra vez para desmarcarla o elige otra para sustituir la selección.
5. Pulsa **Empezar**, disponible cuando hay una rutina seleccionada.

Editar una rutina abre el formulario de creación con sus datos. Al guardar, reemplaza la rutina en su posición, carpeta y color; los entrenamientos anteriores conservan su contenido y dejan de vincularse a la rutina reemplazada.

## Registrar las series

1. Elige la **motivación inicial** y, al final de la lista, la **motivación final** entre 🤕, 😞, 🙂, 😄 y 🔥.
2. Cada ejercicio muestra su nombre, su nota si la tiene y una fila por serie. En fuerza registra **KG** y **Reps.**; en cardio, **Intensidad** y **Minutos**.
3. Admite números enteros o decimales con coma o punto; se guardan con coma. **RIR** sólo aparece en fuerza si se definió en la rutina y no se edita. El botón **+**, a la derecha, añade una serie vacía identificada como extra con un **+** junto a su número.
4. El placeholder de KG muestra en negrita el mayor peso histórico del ejercicio que alcance Mín. Reps. El de Reps. muestra el objetivo definido en la rutina.
5. Pulsa **Guardar** para volver al inicio. Requiere ambas motivaciones y al menos una serie completa: KG y Reps. en fuerza, o Intensidad y Minutos en cardio. Sólo se guardan las series completas.

## Consultar el historial

El inicio muestra los entrenamientos con **fecha y hora**, rutina, motivación inicial y final, ejercicios y cifras de cada serie. El color se muestra atenuado. El **lápiz** abre un entrenamiento pasado en el mismo formulario para corregirlo, conservando su fecha y su posición en el historial.

El historial se abre desde el fondo, con los más recientes abajo. Puedes desplazarte para consultar los anteriores; una línea separa los de distintas semanas.

## Ver el progreso

Pulsa **Progreso**, entre el historial y los botones inferiores. Muestra gráficos del volumen total **KG × Reps.** por entrenamiento, del volumen por ejercicio agrupado por músculo y de la variación de **motivación final − inicial**. En cardio, muestra los minutos por ejercicio. **Ver valores** despliega las cifras y fechas de cada gráfico.

## Exportar e importar

El icono de **exportar/importar** de la esquina superior derecha del inicio permite:

- **Exportar y compartir** una copia de ejercicios, rutinas, carpetas e historial con sus cifras, notas, colores y motivaciones. Se abre el selector de aplicaciones para compartir el archivo.
- **Importar archivo** y elegir todas las categorías o sólo ejercicios, rutinas con sus carpetas, o entrenamientos e historial. Se añaden a los datos existentes sin sustituirlos.

## Cancelar o borrar

- **Cancelar** o el botón atrás vuelve a la pantalla anterior. En una rutina o entrenamiento con datos, pide confirmación antes de descartar el borrador; cancelar la rutina la deja vacía para empezar desde cero.
- Las **papeleras** del catálogo, las rutinas y el historial piden confirmación antes de borrar permanentemente el elemento. Descartar un ejercicio en creación no pide confirmación.
- En una confirmación, **Sí** ejecuta la acción; **No** o el botón atrás la cierra sin cambios. El resto de la pantalla queda bloqueado.

## Datos e interfaz

Los ejercicios del catálogo se reutilizan entre rutinas. Los datos se almacenan localmente; puedes conservar una copia mediante la exportación.

La interfaz utiliza orientación vertical, fondo negro, texto claro y listas desplazables. Los botones mantienen su texto en una línea. El teclado numérico avanza a la derecha y después a la siguiente fila, incluidas las series extra; desplaza el campo hasta hacerlo visible y se cierra tras el último.
