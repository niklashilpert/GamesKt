package server

import game.tictactoe.TicTacToeLobby2
import server.lobby.GameType
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
    class SpaceIsOccupied : InetPacket()
}


abstract class TicTacToePackets {
    // Sent from server
    class LobbyStatus(val lobbyInfo: TicTacToeLobby2.Info) : InetPacket.LobbyInfo()

    // Send from client
    class PlaceMark(val x: Int, val y: Int) : InetPacket()
}

class Message(val source: Player, val packet: DataPacket)