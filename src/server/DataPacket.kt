package server

import server.lobby.ChessLobbyInfo
import server.lobby.GameType
import java.io.Serializable


abstract class DataPacket() {
    class ConnectionLost() : DataPacket()
}

abstract class InetPacket : DataPacket(), Serializable {
    class Connect(val playerName: String, val lobbyName: String, val gameType: GameType) : InetPacket()
    class PlayerExists : InetPacket()
    class LobbyIsPlaying : InetPacket()
    class LobbyIsFull : InetPacket()
    class IllegalPacket : InetPacket()

    class NotAuthorized : InetPacket()

    class LobbyStatus(val lobbyInfo: ChessLobbyInfo) : InetPacket()
    class SwapPlayers : InetPacket()
    class StartGame : InetPacket()
    class CannotStartGame : InetPacket()

}



class Message(val source: Client, val packet: DataPacket)