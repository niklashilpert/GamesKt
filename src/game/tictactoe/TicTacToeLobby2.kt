package game.tictactoe

import server.Player
import server.InetPacket
import server.Message
import server.TicTacToePackets
import server.lobby.Lobby2
import server.lobby.TwoPlayerLobby

abstract class TicTacToeState {
    abstract fun onMessageReceived(message : Message)

    class InProgress : TicTacToeState() {
        override fun onMessageReceived(message: Message) {

        }
    }
}

class TicTacToeLobby2(name: String, host: Player) : TwoPlayerLobby(name, host) {
    class Info (
        lobbyName: String,
        isRunning: Boolean,
        hostName: String,
        val playerXName: String?,
        val playerOName: String?,
        val inGameInfo: InGameInfo?,
    ) : Lobby2.Info(lobbyName, isRunning, hostName)

    class InGameInfo(
        val board: Array<Array<Int>>,
        val currentPlayer: String,
        val xWon: Boolean,
        val oWon: Boolean,
        val tie: Boolean
    )

    var ticTacToe: TicTacToe? = null

    override fun onMessageReceivedWhileRunning(message: Message) {
        val client = message.source
        val packet = message.packet
        if (packet is TicTacToePackets.PlaceMark) {
            if (ticTacToe!!.currentPlayerIsX == (client == player1)) {
                val result = ticTacToe!!.placeMark(packet.x, packet.y)
                if (result) {
                    updateClients()
                } else {
                    client.send(InetPacket.SpaceIsOccupied())
                }
            } else {
                client.send(InetPacket.NotYourTurn())
            }
        } else {
            client.send(InetPacket.IllegalPacket())
        }
    }

    override fun getInfoPacket(): InetPacket.LobbyInfo {
        if (isRunning) {
            val currentPlayer = if (ticTacToe!!.currentPlayerIsX) player1!!.name else player2!!.name
            val gameStatus = ticTacToe!!.checkGameStatus()
            val xWon = gameStatus == TicTacToe.Status.X_WON
            val oWon = gameStatus == TicTacToe.Status.O_WON
            val tie = gameStatus == TicTacToe.Status.TIE
            val inGameInfo = InGameInfo(ticTacToe!!.board, currentPlayer, xWon, oWon, tie)

            return TicTacToePackets.LobbyStatus(
                Info(name, isRunning, host.name, player1?.name, player2?.name, inGameInfo)
            )
        } else {
            return TicTacToePackets.LobbyStatus(
                Info(name, isRunning, host.name, player1?.name, player2?.name, null)
            )
        }
    }


    override fun tryToStartGame(): Boolean {
        if (isFull()) {
            _isRunning.set(true)
            ticTacToe = TicTacToe()
            updateClients()
            return true
        } else {
            return false
        }
    }

    override fun handleGameStop() {
        _isRunning.set(false)
        ticTacToe = null
        updateClients()
    }

}