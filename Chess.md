# Games-Structure

## Networking

Server, der Verbindungen annimmt

Verbindung mit einer Lobby (nach Namen) aufnehmen.
Falls die Lobby noch nicht existiert, wird eine neue erstellt.
In der Lobby kann man verschiedene Einstellungen treffen.

Der Spielmodus wird auch mit einer Property erstellt.

Properties werden als Objekte in einer HashMap gespeichert.
Beispielattribute:
- type: "game.chess"
- white: Connection
- black: Connection

Clients only reply to messages from the server.
Messages are sent by ObjectOutput- and ObjectInputStreams.

## Verbindungsaufbau

C: Init
C: LobbyConnect(player name, lobby name)
S: LobbyInfo(connected players, properties of the lobby, host player)

Connect
Info
UpdateSetting (which client is what player etc.)

