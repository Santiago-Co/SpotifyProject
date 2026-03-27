# True Shuffle

Un proyecto personal desarrollado en Java para crear playlists de Spotify verdaderamente aleatorias.

## Sobre el proyecto

La idea principal de este proyecto nace para solucionar el sesgo del modo aleatorio ("shuffle") nativo de Spotify, el cual tiende a agrupar artistas o priorizar ciertas canciones. 

Esta aplicación de consola extrae todas las canciones guardadas en la biblioteca ("Me gusta") del usuario, las mezcla utilizando un algoritmo matemático imparcial (`Collections.shuffle`), y crea automáticamente una nueva playlist privada con el resultado.

## Tecnologías y retos técnicos

Al ser un proyecto de portafolio, está construido sin frameworks pesados para profundizar en los fundamentos del lenguaje y la integración de sistemas:

* **Lenguaje:** Java puro.
* **Integración:** Spotify Web API (peticiones REST GET, POST y DELETE).
* **Autenticación:** Implementación desde cero del flujo OAuth 2.0, incluyendo la persistencia y refresco automático de tokens.
* **Gestión de datos:** Parseo manual de respuestas con `org.json`.
* **Rendimiento y límites:** Implementación de paginación automática para la descarga masiva de canciones y envío en lotes (batching) de 100 en 100 para sortear las restricciones de subida de la API.
