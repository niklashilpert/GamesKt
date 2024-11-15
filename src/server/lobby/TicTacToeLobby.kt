package server.lobby

import client.printBoard
import game.tictactoe.TicTacToe
import server.DataPacket
import server.InetPacket
import server.Player
import server.TicTacToePackets

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
        return if (!super.handleIncomingPacket(packet, source)) {
            when (packet) {
                is TicTacToePackets.PlaceMark -> {
                    scheduleMarkPlacement(source, packet.x, packet.y)
                    true
                }
                else -> false
            }
        } else true
    }

    private fun scheduleMarkPlacement(source: Player, x: Int, y: Int) {
        taskQueue.put(MarkPlacementTask(source, x, y))
    }

    private fun performMarkPlacement(task: Task): Boolean {
        return handleIfRunning(task.source) {
            if (task is MarkPlacementTask) {
                val source = task.source
                if (ticTacToe!!.checkGameStatus() == TicTacToe.Status.IN_PROGRESS) {
                    val isSourcesTurn = ticTacToe!!.currentPlayerIsX == (source == player1)
                    log("Is Source's turn: $isSourcesTurn")
                    if (isSourcesTurn) {
                        val result = ticTacToe!!.placeMark(task.x, task.y)
                        if (result) {
                            return@handleIfRunning true
                        } else {
                            source.send(InetPacket.NotAValidLocation())
                        }
                    } else {
                        source.send(InetPacket.NotYourTurn())
                    }
                } else {
                    source.send(TicTacToePackets.CannotPlaceAfterEnd())
                }
                false
            } else {
                throw IllegalArgumentException("Task is not a MarkPlacementTask")
            }
        }
    }

    override fun getLobbyInfoPacket(): InetPacket.LobbyInfo {
        if (ticTacToe != null) {
            printBoard(ticTacToe!!.getInfo().board)
        }

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