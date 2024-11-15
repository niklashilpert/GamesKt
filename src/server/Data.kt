package server

import server.lobby.GameType
import server.lobby.TicTacToeLobby
import java.io.Serializable


abstract class DataPacket() {
    class ConnectionLost() : DataPacket()
}

abstract class InetPacket : DataPacket(), Serializable {
    abstract class LobbyInfo : InetPacket()

    // Sent from server
    class IllegalPacket : InetPacket()
    class PlayerExists : InetPacket()
    class LobbyIsPlaying : InetPacket()
    class LobbyIsFull : InetPacket()

    class CannotStartGame : InetPacket()
    class NotAuthorized : InetPacket()

    // Sent from client
    class Connect(val playerName: String, val lobbyName: String, val gameType: GameType) : InetPacket()

    class SwapPlayers : InetPacket()
    class StartGame : InetPacket()
    class StopGame : InetPacket()

    class Success : InetPacket()
    class NotYourTurn : InetPacket()
    class NotAValidLocation : InetPacket()
}


abstract class TicTacToePackets {
    // Sent from server
    class LobbyInfo(val lobbyInfo: TicTacToeLobby.Info) : InetPacket.LobbyInfo()

    // Send from client
    class PlaceMark(val x: Int, val y: Int) : InetPacket()
    class CannotPlaceAfterEnd : InetPacket()
}

