package server.lobby

import java.io.Serializable

abstract class LobbyInfo(
    val lobbyName: String,
    val isRunning: Boolean,
    val host: String,
) : Serializable

class ChessLobbyInfo(
    lobbyName: String,
    isRunning: Boolean,
    hostName: String,
    val playerWhiteName: String?,
    val playerBlackName: String?,
) : LobbyInfo(
    lobbyName,
    isRunning,
    hostName
)
