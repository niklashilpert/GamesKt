package server.lobby

import game.tictactoe.TicTacToe
import server.DataPacket
import server.InetPacket
import server.Player
import server.TicTacToePackets
import java.io.IOException

class TicTacToeLobby(name: String) : TwoPlayerLobby(name) {

    private var ticTacToe: TicTacToe? = null

    override fun handleIncomingPacket(packet: DataPacket, source: Player): Boolean {
        return when (packet) {
            is TicTacToePackets.PlaceMark -> {
                handleIfRunning {
                    handlePlaceMarkAction(source, packet.x, packet.y)
                }
            }
            else -> {
                false
            }
        }
    }

    private fun handlePlaceMarkAction(source: Player, x: Int, y: Int) {
        return if (ticTacToe!!.currentPlayerIsX == (source == player1)) {
            val result = ticTacToe!!.placeMark(x, y)
            if (result) {
                notifyPlayers()
            } else {
                source.send(InetPacket.SpaceIsOccupied())
            }
        } else {
            source.send(InetPacket.NotYourTurn())
        }
    }

    private fun handleIfRunning(action: () -> Unit): Boolean {
        return if (!isOpen) {
            action()
            true
        } else {
            false
        }
    }

    override fun notifyPlayers() {
        if (player0 != null && !player0!!.isClosed) {
            notifyPlayer(player0!!)
        }
        if (player1 != null && !player1!!.isClosed) {
            notifyPlayer(player1!!)
        }
    }

    override fun getPlayerWithName(name: String): Player? {
        TODO("Not yet implemented")
    }

    private fun notifyPlayer(player: Player) {
        try {
            player0!!.send(getLobbyInfo())
        } catch (e: IOException) {
            player0!!.close()
            queueLeave(player0!!)
        }
    }

    private fun getLobbyInfo(): TicTacToePackets.LobbyStatus {
        
    }

    override fun handleGameStart() {
        ticTacToe = TicTacToe()
    }

    override fun handleGameStop() {
        ticTacToe = null
    }
}