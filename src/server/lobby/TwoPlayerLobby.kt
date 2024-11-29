package server.lobby

import server.DataPacket
import server.Player
import server.InetPacket
import server.ResultCode

abstract class TwoPlayerLobby(name: String) : Lobby(name, 2) {
    protected inner class PlayerSwapTask(private val source: Player) : Task() {
        override fun perform(): Boolean {
            if (host != source) {
                source.tryRespond(ResultCode.NOT_AUTHORIZED)
                return false
            } else if (!isOpen) {
                source.tryRespond(ResultCode.LOBBY_IS_PLAYING)
                return false
            } else {
                val tmp = player1
                player1 = player2
                player2 = tmp
                source.tryRespond(ResultCode.SUCCESS)
                return true
            }
        }
    }

    var player1: Player? = null
        private set
    var player2: Player? = null
        private set

    override fun handleIncomingPacket(packet: DataPacket, source: Player): Boolean {
        return if (!super.handleIncomingPacket(packet, source)) {
            when (packet) {
                is InetPacket.SwapPlayers -> {
                    queue(PlayerSwapTask(source))
                    true
                }
                else -> {
                    false
                }
            }
        } else true
    }

    override fun store(player: Player) {
        if (player1 == null) {
            player1 = player
        } else {
            player2 = player
        }
    }

    override fun remove(player: Player) {
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