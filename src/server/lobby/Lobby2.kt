package server.lobby

import game.tictactoe.TicTacToeLobby2
import server.*
import java.io.Serializable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean


enum class GameType {
    CHESS,
    CHECKERS,
    TICTACTOE,
}


fun createLobby2(type: GameType, name: String, host: Player) : Lobby2 {
    return when (type) {
        GameType.CHESS -> {
            TODO("Chess is not implemented yet")
        }
        GameType.CHECKERS -> {
            TODO("Checkers is not implemented yet")
        }
        GameType.TICTACTOE -> {
            TicTacToeLobby2(name, host)
        }
    }
}

enum class PlayerAddResult {
    SUCCESS,
    ERR_FULL,
    ERR_PLAYER_EXISTS,
    ERR_RUNNING,
}

abstract class Lobby2(var name: String, var host: Player) {
    abstract class Info(val lobbyName: String, val isRunning: Boolean, val host: String, ) : Serializable

    private val messageQueue = LinkedBlockingQueue<Message>()

    protected var _isRunning = AtomicBoolean(false)
    val isRunning get() = _isRunning.get()

    protected var keepThreadRunning = true

    private val messageHandlingThread = Thread {
        while (keepThreadRunning) {
            try {
                val message = messageQueue.take()
                if (message.packet is DataPacket.ConnectionLost) {
                    removeClient(message.source)
                } else if (message.source == host && message.packet is InetPacket.StartGame) {
                    if (!tryToStartGame()) {
                        message.source.send(InetPacket.CannotStartGame())
                    }
                } else if (message.source == host && message.packet is InetPacket.StopGame) {
                    stopGame()
                } else if (isRunning) {
                    onMessageReceivedWhileRunning(message)
                } else {

                    onMessageReceivedWhileOpen(message)
                }
            } catch (e: InterruptedException) {
                terminateLobby()
            }
        }
    }.also { it.start() }

    abstract fun addClient(client: Player): PlayerAddResult
    protected abstract fun removeClient(client: Player)
    protected abstract fun onMessageReceivedWhileOpen(message: Message)
    protected abstract fun onMessageReceivedWhileRunning(message: Message)
    protected abstract fun updateClients(synchronized: Boolean = true)
    protected abstract fun getInfoPacket(): InetPacket.LobbyInfo

    protected abstract fun tryToStartGame(): Boolean
    protected abstract fun stopGame()

        fun registerMessage(message: Message) {
        messageQueue.put(message)
    }

    protected fun terminateLobby() {
        keepThreadRunning = false
        messageHandlingThread.interrupt()
        GameServer.removeLobby(this)
    }
}