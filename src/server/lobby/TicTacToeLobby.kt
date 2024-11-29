package server.lobby

import game.tictactoe.TicTacToe
import server.*

class TicTacToeLobby(name: String) : TwoPlayerLobby(name) {
    class Info (
        lobbyName: String,
        isOpen: Boolean,
        hostName: String?,
        val playerXName: String?,
        val playerOName: String?,
        val ticTacToeInfo: TicTacToe.Info?,
    ) : Lobby.Info(lobbyName, isOpen, hostName)

    private inner class MarkPlacementTask(private val source: Player, private val x: Int, private val y: Int) : Task() {
        override fun perform(): Boolean {
            val isTheirTurn = ticTacToe?.currentPlayerIsX == (source == player1)
            if (isOpen) {
                source.tryRespond(ResultCode.LOBBY_IS_OPEN)
                return false
            } else if (ticTacToe!!.checkGameStatus() != TicTacToe.Status.IN_PROGRESS) {
                source.tryRespond(ResultCode.GAME_IS_OVER)
                return false
            } else if (!isTheirTurn) {
                source.tryRespond(ResultCode.NOT_YOUR_TURN)
                return false
            } else if (ticTacToe?.inBounds(x, y) != true) {
                source.tryRespond(ResultCode.OUT_OF_BOUNDS)
                return false
            } else {
                val placedMark = ticTacToe!!.placeMark(x, y)
                if (!placedMark) {
                    source.tryRespond(ResultCode.PLACE_IS_OCCUPIED)
                    return false
                } else {
                    source.tryRespond(ResultCode.SUCCESS)
                    return true
                }
            }        }
    }

    private var ticTacToe: TicTacToe? = null

    override fun handleIncomingPacket(packet: DataPacket, source: Player): Boolean {
        return if (super.handleIncomingPacket(packet, source)) {
            true
        } else {
            when (packet) {
                is TicTacToePackets.PlaceMark -> {
                    queue(MarkPlacementTask(source, packet.x, packet.y))
                    true
                }
                else -> false
            }
        }
    }

    override fun getLobbyInfoPacket(): InetPacket.LobbyInfo {
        return TicTacToePackets.LobbyInfo(
            Info(name, isOpen, host?.name, player1?.name, player2?.name, ticTacToe?.getInfo())
        )
    }

    override fun handleGameStart() {
        ticTacToe = TicTacToe()
    }

    override fun handleGameStop() {
        ticTacToe = null
    }
}