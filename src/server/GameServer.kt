package server

import server.lobby.*
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections


object GameServer {
    private val socket = ServerSocket(6666)

    private val lobbyMap = HashMap<GameType, MutableList<Lobby>>().apply {
        GameType.entries.forEach {
            this[it] = Collections.synchronizedList(mutableListOf<Lobby>())
        }
    }

    fun listen() {
        try {
            while (true) {
                handle(socket.accept())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun handle(socket: Socket) {
        val connection = ClientHandle(socket)
        try {
            val packet = connection.read()
            if (packet is InetPacket.Connect) {
                val lobbyList = lobbyMap[packet.gameType]!!
                val lobby = lobbyList.find { it.name == packet.lobbyName }
                if (lobby != null) {
                    val player = Player(connection, packet.playerName)
                    lobby.queueJoin(player)
                } else {
                    val newLobby = createLobby(packet.gameType, packet.lobbyName)
                    val player = Player(connection, packet.playerName)
                    newLobby.queueJoin(player)
                    println("Created new lobby \"${newLobby.name}\"")
                    lobbyList.add(newLobby)
                }
            } else {
                println("The first packet of the client was not of type InetPacket.LobbyConnect")
                connection.close()
            }
        }
        catch (e: IOException) {
            println("IOException while handling new connection: ${e.javaClass}: ${e.message}")
            connection.close()
        }
        catch (_: ClassNotFoundException) {
            println("Object received by the server was not an InetPacket")
            connection.close()
        }
    }

    fun removeLobby(lobby: Lobby) {
        lobbyMap.values.forEach { it.remove(lobby) }
    }
}