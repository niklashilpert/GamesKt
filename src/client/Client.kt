package client

import server.InetPacket
import server.TicTacToePackets
import server.lobby.GameType
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket

fun main(args: Array<String>) {
    val name = args[0]
    val clientManager = ClientManager()

    println("Connecting...")
    val socket = Socket()
    socket.connect(InetSocketAddress("127.0.0.1", 6666))

    val dataOut = ObjectOutputStream(socket.getOutputStream())
    val dataIn = ObjectInputStream(socket.getInputStream())

    dataOut.writeObject(InetPacket.Connect(name, "SuperLobby", GameType.TIC_TAC_TOE))

    Thread {
        try {
            while (true) {
                val packet = dataIn.readObject() as InetPacket
                if (packet is TicTacToePackets.LobbyInfo) {
                    val info = packet.lobbyInfo
                    if (!info.isOpen) {
                        val inGameInfo = info.ticTacToeInfo
                        println("Running: ${info.lobbyName}[${info.host}] - ${info.playerXName} vs. ${info.playerOName}")
                        printBoard(inGameInfo!!.board)
                        if (inGameInfo.tie) {
                            println("TIE")
                        } else if (inGameInfo.xWon) {
                            println("X WON")
                        } else if (inGameInfo.oWon) {
                            println("O WON")
                        } else if (inGameInfo.currentPlayerIsX == (name == info.playerXName)) {
                            println("Your turn")
                        } else {
                            println("Enemy's turn")
                        }
                    } else {
                        println("${info.lobbyName}, ${info.isOpen}, ${info.host}, ${info.playerXName}, ${info.playerOName}")
                    }


                } else if (packet is InetPacket.Result){
                    println(packet.code)
                } else {
                    println(packet)
                }
            }
        } catch (e: IOException) {
            println(e.javaClass.simpleName + " " + (e.message ?: ""))
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
            "u" -> dataOut.writeObject(InetPacket.StatusRequest())
            "stop" -> dataOut.writeObject(InetPacket.StopGame())
            else -> {
                if (cmd.startsWith("place")) {
                    val parts = cmd.split(" ")
                    dataOut.writeObject(TicTacToePackets.PlaceMark(parts[1].toInt(), parts[2].toInt()))
                }
            }
        }
    }

}

fun printBoard(board: Array<IntArray>) {
    println("${board[0][0]} ${board[0][1]} ${board[0][2]}")
    println("${board[1][0]} ${board[1][1]} ${board[1][2]}")
    println("${board[2][0]} ${board[2][1]} ${board[2][2]}")
}