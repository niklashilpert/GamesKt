package server.lobby


class ChessLobbyInfo(
    lobbyName: String,
    isRunning: Boolean,
    hostName: String,
    val playerWhiteName: String?,
    val playerBlackName: String?,
) : Lobby2.Info(
    lobbyName,
    isRunning,
    hostName
)
