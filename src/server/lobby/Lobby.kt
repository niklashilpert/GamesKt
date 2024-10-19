package server.lobby

import server.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean


enum class GameType {
    CHESS,
    CHECKERS
}


fun createLobby(type: GameType, name: String, host: Client) : Lobby {
    return when (type) {
        GameType.CHESS -> {
            ChessLobby(name, host)
        }
        GameType.CHECKERS -> {
            ChessLobby(name, host)
        }
    }
}

abstract class Lobby(var name: String, var host: Client) {
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


    abstract fun isFull(): Boolean
    abstract fun playerExists(playerName: String): Boolean
    abstract fun addClient(client: Client)
    protected abstract fun removeClient(client: Client)
    protected abstract fun onMessageReceivedWhileOpen(message: Message)
    protected abstract fun onMessageReceivedWhileRunning(message: Message)


    fun registerMessage(message: Message) {
        messageQueue.put(message)
    }

    protected fun terminateLobby() {
        keepThreadRunning = false
        GameServer.removeLobby(this)
    }
}