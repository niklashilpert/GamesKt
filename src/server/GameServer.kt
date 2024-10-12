package server

import java.io.IOException
import java.net.ServerSocket

class GameServer(port: Int) {
    private val socket = ServerSocket(port)

    private val lobbies = ArrayList<Lobby>()

    fun listen() {
        try {
            while (true) {
                val client = Connection(socket.accept())
                if (isValid(client)) {
                    handle(client)
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Verifies that the client is the corresponding game client by checking if the first packet is an InitPacket.
     */
    private fun isValid(client: Connection): Boolean {
        val packet = client.read()
        if (packet is InitPacket) {
            client.name = packet.playerName
            return true
        }  else {
            return false
        }
    }

    private fun handle(client: Connection) {
        val clientThread = Thread {
            val packet = client.read()
            if (packet is LobbyConnectPacket) {
                joinLobby(client, packet.lobbyName)
            } else {
                client.close()
            }
        }
        clientThread.start()
    }

    private fun joinLobby(client: Connection, lobbyName: String) {
        val lobby = lobbies.find { it.name == lobbyName }
        if (lobby != null) {
            if (lobby.player2 == null) {
                lobby.player2 = client
                client.send(LobbyStatusPacket())
            } else {
                client.send(LobbyFullPacket())
            }
        } else {
            val newLobby = Lobby(lobbyName, client)
            lobbies.add(newLobby)
            client.send(LobbyStatusPacket())
        }
    }
}