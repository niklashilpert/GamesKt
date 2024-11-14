package server.lobby

import server.DataPacket
import server.Player
import server.InetPacket

abstract class TwoPlayerLobby(name: String) : Lobby(name, 2) {
    protected inner class PlayerSwapTask(source: Player) : Task(source, ::performPlayerSwap)

    var player0: Player? = null
        private set
    var player1: Player? = null
        private set

    override fun handleIncomingPacket(packet: DataPacket, source: Player): Boolean {
        return if (!super.handleIncomingPacket(packet, source)) {
            when (packet) {
                is InetPacket.SwapPlayers -> {
                    queuePlayerSwap(source)
                    true
                }
                else -> false
            }
        } else true
    }

    private fun queuePlayerSwap(source: Player) {
        taskQueue.put(PlayerSwapTask(source))
    }

    private fun performPlayerSwap(source: Player) {
        doIfHost(source) {
            if (isOpen) {
                val tmp = player0
                player0 = player1
                player1 = tmp
            }
        }
    }

    override fun put(player: Player) {
        if (player0 != null) {
            player0 = player
        } else {
            player1 = player
        }
    }

    override fun pop(player: Player) {
        if (player0 == player) {
            player0 = null
        } else if (player1 == player) {
            player1 = null
        }
    }

    override fun isFull(): Boolean {
        return player0 != null && player1 != null
    }

    override fun getFirstPlayer(): Player? {
        if (player0 != null) {
            return player0
        }
        return player1
    }

    override fun findPlayerWithName(name: String): Player? {
        if (player0?.name == name) {
            return player0
        } else if (player1?.name == name) {
            return player1
        }
        return null
    }
}