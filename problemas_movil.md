
##  Desglose de Fallos por Requisito

### 🔴 Gravedad: CRÍTICA (Bloquean funcionalidad o incumplen reglas de negocio)

| Requisito | Descripción del Error | Evidencia Técnica |
| :--- | :--- | :--- |
| **RF-30** | **Inexistencia de búsqueda de amigos.** No existe barra de búsqueda ni lógica de filtrado en la lista de amigos. | `PerfilActivity.java` y `activity_perfil.xml` carecen de componentes de búsqueda. |
| **RF-23.4** | **Imposibilidad de configurar partida en Lobby.** El creador no puede modificar el tiempo ni el paquete de cartas una vez en la sala de espera. | El listener `btn_config_sala.setOnClickListener` en `SalaEsperaActivity.java` está vacío. |
| **RF-38** | **Persistencia indebida de ajustes de audio.** El requisito prohíbe guardar estos ajustes entre sesiones, pero el código los hace persistentes. | `HomeActivity.java` utiliza `SharedPreferences ("Ajustes_Audio")` para salvar los valores. |
| **RF-35** | **Flujo de abandono inconsistente.** Si un jugador abandona pero la partida sigue, la resolución final llega al cliente provocando redirecciones y errores inesperados. | Falta de limpieza de suscripciones o estado de "partida abandonada" en el ciclo de vida. |
| **UX-Game** | **Salida de partida sin confirmación.** Al pulsar el botón "atrás" del sistema, el usuario sale de la partida sin que se le pregunte si realmente desea abandonar. | `PartidaActivity.java` no sobreescribe `onBackPressed()` ni utiliza `OnBackPressedCallback`. |

### 🟡 Gravedad: MODERADA (Inconsistencias de interfaz o lógica débil)

| Requisito | Descripción del Error | Evidencia Técnica |
| :--- | :--- | :--- |
| **RF-7** | **Límite de historial no garantizado.** No hay lógica en el cliente que asegure que solo se muestran las últimas 30 partidas. | `cargarHistorialServidor` en `HomeActivity.java` vuelca todo el array recibido del backend. |
| **RF-17** | **Rigidez del tablero.** Aunque se usa un `GridLayout` de 5 columnas, no hay validación para asegurar la estructura de 4 filas. | Lógica de `pintarTablero` en `PartidaActivity.java` dependiente 100% de la respuesta del servidor. |
| **RF-19** | **Validación de pista laxa.** El campo de texto para la pista no limita físicamente los 20 caracteres solicitados en el diccionario de datos. | `dialog_anadir_pista.xml` no tiene configurado el atributo `maxLength`. |
| **UI-Match** | **Inconsistencia en contador de equipos.** El indicador visual de la catnidad de personas por equipo en la lobby aparece en color rojo a veces y a veces en azul 
| **UI-Lobby** | **Formato de contador de jugadores erróneo.** El contador muestra valores como 1/1 o 2/2 en lugar de 1/Total o el número simple de participantes. | `JugadorSalaAdapter` o la actualización de la UI en `SalaEsperaActivity` calcula mal el total. |
| **RF-20** | **Falta de visor de votos.** No se ha implementado el componente visual para ver qué agentes han votado cada carta durante el turno actual. | El `gridTablero` no renderiza los indicadores de votación individual de otros jugadores. |
| **API-Err** | **Gestión de errores incompleta.** Faltan por implementar o mapear los códigos de error definidos en el apartado 9 de la documentación de Gestión. | `ErrorUtils.java` no contempla todos los casos del contrato de la API. |

### 🟢 Gravedad: MENOR (Estética y Convenciones)

| **Feedback de Empate**| La pérdida de turno por empate en votación (RF-22) no genera una notificación visual clara al usuario, pasando el turno de forma abrupta. |
| **Personalización** | El cambio de selección de skins en el lobby solo altera el color del borde, no el tamaño o escala de la tarjeta seleccionada (falta feedback de selección activa). |
