package server

import server.lobby.GameType
import server.lobby.Lobby
import server.lobby.createLobby
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
        val connection = Connection(socket)
        try {
            val packet = connection.read()
            if (packet is InetPacket.Connect) {
                val lobbyList = lobbyMap[packet.gameType]!!
                val lobby = lobbyList.find { it.name == packet.lobbyName }
                if (lobby != null) {
                    if (lobby.isRunning) {
                        connection.send(InetPacket.LobbyIsPlaying())
                        connection.close()
                        println("The requested lobby is already playing.")
                    } else if (lobby.playerExists(packet.playerName)) {
                        connection.send(InetPacket.PlayerExists())
                        connection.close()
                        println("Player with that name already exists.")
                    } else if (lobby.isFull()) {
                        connection.send(InetPacket.LobbyIsFull())
                        connection.close()
                        println("The requested lobby is full.")
                    } else {
                        val client = Client(connection, packet.playerName)
                        client.startReceivingMessages(lobby::registerMessage)
                        lobby.addClient(client)
                        println("\"${packet.playerName}\" joined successfully.")
                    }
                } else {
                    val client = Client(connection, packet.playerName)
                    val newLobby = createLobby(packet.gameType, packet.lobbyName, client)
                    client.startReceivingMessages(newLobby::registerMessage)
                    lobbyList.add(newLobby)
                    println("Adding lobby \"${packet.lobbyName}\" and made \"${packet.playerName}\" host")
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