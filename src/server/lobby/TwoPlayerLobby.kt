package server.lobby

import server.Player
import server.InetPacket
import server.Message

abstract class TwoPlayerLobby(name: String, host: Player) : Lobby2(name, host) {
    protected val playerLock = Any()
    protected var player1: Player? = host
    protected var player2: Player? = null

    init {
        updateClients()
    }

    protected fun isFull() = player1 != null && player2 != null

    override fun onMessageReceivedWhileOpen(message: Message) {
        println("${message.source.name} -> ${message.packet}")

        if (message.source == host) {
            when (message.packet) {
                is InetPacket.SwapPlayers -> {
                    synchronized(playerLock) {
                        val temp = player1
                        player1 = player2
                        player2 = temp
                        updateClients(false)
                    }
                }
                else -> message.source.send(InetPacket.IllegalPacket())
            }
        }
    }

    override fun addClient(client: Player): PlayerAddResult {
        synchronized(playerLock) {
            return if (isRunning) {
                PlayerAddResult.ERR_RUNNING
            } else if (player1?.name == client.name || player2?.name == client.name) {
                PlayerAddResult.ERR_PLAYER_EXISTS
            } else if (player1 == null) {
                player1 = client
                updateClients(false)
                PlayerAddResult.SUCCESS
            } else if (player2 == null) {
                player2 = client
                updateClients(false)
                PlayerAddResult.SUCCESS
            } else {
                PlayerAddResult.ERR_FULL
            }
        }
    }

    override fun removeClient(client: Player) {
        synchronized(playerLock) {
            if (client == host) {
                if (host == player1 && player2 != null) {
                    host = player2!!
                } else if (host == player2 && player1 != null) {
                    host = player1!!
                }
            }

            if (client == player1) {
                player1?.close()
                player1 = null
            } else {
                player2?.close()
                player2 = null
            }

            if (player1 == null && player2 == null) {
                terminateLobby()
            } else {
                updateClients(false)
            }
        }
    }

    final override fun updateClients(synchronized: Boolean) {
        if (synchronized) {
            synchronized(playerLock) {
                updateClientsUnsafe()
            }
        } else {
            updateClientsUnsafe()
        }
    }

    private fun updateClientsUnsafe() {
        val lobbyInfo = getInfoPacket()
        player1?.send(lobbyInfo)
        player2?.send(lobbyInfo)
    }
}