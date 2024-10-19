package client

import server.InetPacket
import server.lobby.GameType
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket

fun main(args: Array<String>) {
    println("Connecting...")
    val socket = Socket()
    socket.connect(InetSocketAddress("127.0.0.1", 6666))

    val dataOut = ObjectOutputStream(socket.getOutputStream())
    val dataIn = ObjectInputStream(socket.getInputStream())

    dataOut.writeObject(InetPacket.Connect(args[0], "SuperLobby", GameType.CHESS))

    Thread {
        try {
            while (true) {
                val packet = dataIn.readObject() as InetPacket
                if (packet is InetPacket.LobbyStatus) {
                    val info = packet.lobbyInfo
                    println("${info.lobbyName}, ${info.isRunning}, ${info.host}, ${info.playerWhiteName}, ${info.playerBlackName}")

                } else {
                    println(packet)
                }
            }
        } catch (e: IOException) {
            println("Stopping thread")
        }
    }.start()

    while(true) {
        val cmd = readln()
        when (cmd) {
            "swap" -> dataOut.writeObject(InetPacket.SwapPlayers())
            "exit" -> {
                dataIn.close()
                dataOut.close()
                socket.close()
            }
            "start" -> dataOut.writeObject(InetPacket.StartGame())
        }
    }

}