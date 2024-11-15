package server.lobby

import server.DataPacket
import server.Player
import server.InetPacket

abstract class TwoPlayerLobby(name: String) : Lobby(name, 2) {
    abstract class Info(
        lobbyName: String,
        isOpen: Boolean,
        host: String?,
        val player1: String?,
        val player2: String?
    ) : Lobby.Info(lobbyName, isOpen, host)

    protected inner class PlayerSwapTask(source: Player) : Task(source, ::performPlayerSwap)

    var player1: Player? = null
        private set
    var player2: Player? = null
        private set

    override fun handleIncomingPacket(packet: DataPacket, source: Player): Boolean {
        return if (!super.handleIncomingPacket(packet, source)) {
            when (packet) {
                is InetPacket.SwapPlayers -> {
                    handleIfOpen(source) {
                        queuePlayerSwap(source)
                        true
                    }
                }

                else -> false
            }
        } else true
    }

    private fun queuePlayerSwap(source: Player) {
        taskQueue.put(PlayerSwapTask(source))
    }

    private fun performPlayerSwap(task: Task): Boolean {
        return handleIfHost(task.source) {
            handleIfOpen(task.source) {
                val tmp = player1
                player1 = player2
                player2 = tmp
                true
            }
        }
    }

    override fun put(player: Player) {
        if (player1 == null) {
            player1 = player
        } else {
            player2 = player
        }
    }

    override fun pop(player: Player) {
        if (player1 == player) {
            player1 = null
        } else if (player2 == player) {
            player2 = null
        }
    }

    override fun getPlayers(): List<Player> {
        val playerList = mutableListOf<Player>()
        if (player1 != null) {
            playerList.add(player1!!)
        }
        if (player2 != null) {
            playerList.add(player2!!)
        }
        return playerList
    }
}