package server.lobby

import client.printBoard
import game.tictactoe.TicTacToe
import server.*

class TicTacToeLobby(name: String) : TwoPlayerLobby(name) {
    class Info (
        lobbyName: String,
        isOpen: Boolean,
        hostName: String?,
        player1: String?,
        player2: String?,
        val ticTacToeInfo: TicTacToe.Info?,
    ) : TwoPlayerLobby.Info(lobbyName, isOpen, hostName, player1, player2)

    private inner class MarkPlacementTask(source: Player, val x: Int, val y: Int) : Task(source, ::performMarkPlacement)

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

    private fun performMarkPlacement(task: Task): Boolean {
        val source = task.source
        val isTheirTurn = ticTacToe?.currentPlayerIsX == (source == player1)
        if (isOpen) {
            source.tryRespond(ResultCode.LOBBY_IS_OPEN)
            return false
        } else if (task !is MarkPlacementTask) {
            throw IllegalArgumentException("Task is not of type MarkPlacementTask")
        } else if (ticTacToe!!.checkGameStatus() != TicTacToe.Status.IN_PROGRESS) {
            source.tryRespond(ResultCode.GAME_IS_OVER)
            return false
        } else if (!isTheirTurn) {
            source.tryRespond(ResultCode.NOT_YOUR_TURN)
            return false
        } else if (ticTacToe?.inBounds(task.x, task.y) != true) {
            source.tryRespond(ResultCode.OUT_OF_BOUNDS)
            return false
        } else {
            val placedMark = ticTacToe!!.placeMark(task.x, task.y)
            if (!placedMark) {
                source.tryRespond(ResultCode.PLACE_IS_OCCUPIED)
                return false
            } else {
                source.tryRespond(ResultCode.SUCCESS)
                return true
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