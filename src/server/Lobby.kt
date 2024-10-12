package server

class Lobby(val name: String, val host: Connection) {
    var player2: Connection? = null
}