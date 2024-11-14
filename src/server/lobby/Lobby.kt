package server.lobby

import server.*
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

fun createLobby(type: GameType, name: String) : Lobby {
    return when (type) {
        GameType.CHESS -> {
            TODO("Chess is not implemented yet")
        }
        GameType.CHECKERS -> {
            TODO("Checkers is not implemented yet")
        }
        GameType.TICTACTOE -> {
            TicTacToeLobby(name)
        }
    }
}


abstract class Lobby(val name: String, private val maxPlayerCount: Int) {
    abstract inner class Task(val source: Player, val perform: (Player) -> Unit)
    protected inner class JoinTask(source: Player) : Task(source, ::performJoin)
    protected inner class LeaveTask(source: Player) : Task(source, ::performLeave)
    protected inner class StartGameTask(source: Player) : Task(source, ::performStartGame)
    protected inner class StopGameTask(source: Player) : Task(source, ::performStopGame)

    private var _isOpen = AtomicBoolean(true)
    var isOpen
        get() = _isOpen.get()
        protected set(value) = _isOpen.set(value)


    protected val taskQueue = LinkedBlockingQueue<Task>()

    private var host: Player? = null
    private var keepTaskThreadRunning = AtomicBoolean(true)
    private val taskThread: Thread = createTaskHandlerThread()

    private fun createTaskHandlerThread(): Thread {
        return Thread {
            while(keepTaskThreadRunning.get()) {
                try {
                    val nextTask = taskQueue.take()
                    nextTask.perform(nextTask.source)
                    notifyPlayers()
                } catch (e: InterruptedException) {
                    keepTaskThreadRunning.set(false)
                }
            }
            println("Stopping task handling thread for lobby $name.")
        }
    }

    /**
     * Creates a thread that permanently tries to read packets from the provided player's input stream.
     * If an IOException occurs, the connection to the player is closed and a ConnectionClosed packet is generated.
     */
    private fun createPlayerListeningThread(player: Player): Thread {
        return Thread {
            while(!player.isClosed) {
                try {
                    val packet = player.read()
                    if (!handleIncomingPacket(packet, player)) {
                        player.send(InetPacket.IllegalPacket())
                    }
                } catch (e: IOException) {
                    player.close()
                    queueLeave(player)
                }
            }
            println("Stopped player listening thread for ${player.name}")
        }
    }

    /**
     * Handles the provided packet. Returns false, if the packet could not be handled.
     */
    protected open fun handleIncomingPacket(packet: DataPacket, source: Player): Boolean {
        return when (packet) {
            is InetPacket.StartGame -> {
                queueGameStart(source)
                true
            }
            is InetPacket.StopGame -> {
                queueGameStop(source)
                true
            }
            else -> {
                false
            }
        }
    }

    /**
     * Queues a join task in the lobby's task queue.
     * The player will be sent an error response if they could not join.
     *
     * Reasons for rejection are:
     * - a player with the same name already exists
     * - the lobby is full
     * - the lobby is already playing
     */
    fun queueJoin(player: Player) {
        taskQueue.put(JoinTask(player))
    }

    /**
     * Queues a player leave task.
     */
    protected fun queueLeave(player: Player) {
        taskQueue.put(LeaveTask(player))
    }

    /**
     * Queues a game start task if the player is the host.
     */
    private fun queueGameStart(player: Player) {
        taskQueue.put(StartGameTask(player))
    }

    /**
     * Queues a game stop task if the player is the host.
     * The stop task will also be queued if player is null.
     */
    private fun queueGameStop(player: Player) {
        taskQueue.put(StopGameTask(player))
    }
    
    /**
     * Adds the player to the players list if it is allowed.
     * The player will be notified if it can't join the lobby.
     */
    private fun performJoin(player: Player) {
        if (findPlayerWithName(player.name) != null) {
            player.send(InetPacket.PlayerExists())
            player.close()
        } else if (isFull()) {
            player.send(InetPacket.LobbyIsFull())
            player.close()
        } else if (!isOpen) {
            player.send(InetPacket.LobbyIsPlaying())
            player.close()
        } else {
            if (host == null) {
                host = player
            }
            createPlayerListeningThread(player).start()
            player.send(InetPacket.Success())
            put(player)
            println("Player ${player.name} joined lobby $name.")
        }
    }

    /**
     * Removes the player from the players list.
     * If they are the host, the next host candidate is promoted.
     * If they are the last player in the lobby, it will be terminated.
     * If the lobby's game was in progress, it is interrupted and the lobby returns to the open state.
     */
    private fun performLeave(player: Player) {
        if (!isOpen) performStopGameUnchecked()
        pop(player)
        println("Player ${player.name} left lobby $name.")
        if (player == host) {
            host = getFirstPlayer()
            if (host != null) {
                println("Player ${player.name} is now host of lobby $name.")
            } else {
                terminateLobby()
            }
        }
    }

    /**
     * Tries to start the game if it can.
     * Sends an error to back to the client if it can't start.
     */
    private fun performStartGame(source: Player) {
        if (isFull()) {
            handleGameStart()
        } else {
            try {
                source.send(InetPacket.CannotStartGame())
            } catch (e: IOException) {
                source.close()
                queueLeave(source)
            }
        }
    }
    
    private fun performStopGame(source: Player) {
        doIfHost(source) {
            performStopGameUnchecked()
        }
    }
    
    private fun performStopGameUnchecked() {
        handleGameStop()
        isOpen = true
    }

    protected abstract fun handleGameStart()
    protected abstract fun handleGameStop()

    /**
     * Stores the player in an implementation specific object.
     */
    protected abstract fun put(player: Player)

    /**
     * Removes the player from the implementation specific storage.
     */
    protected abstract fun pop(player: Player)

    /**
     * Notifies all stored players.
     */
    protected abstract fun notifyPlayers()
    protected abstract fun getFirstPlayer(): Player?
    protected abstract fun isFull(): Boolean
    protected abstract fun findPlayerWithName(name: String): Player?


    /**
     * Only performs the provided action if the player is the host of the lobby.
     * If not, this method sends a NotAuthorized packet to the player.
     *
     * If the provided player is null, the action is executed.
     */
    protected fun doIfHost(player: Player, action: (Player) -> Unit) {
        if (player == host) {
            action(player)
        } else {
            player.send(InetPacket.NotAuthorized())
        }
    }

    /**
     * Stops the task thread and removes the lobby from the game server's registry.
     */
    private fun terminateLobby() {
        keepTaskThreadRunning.set(false)
        taskThread.interrupt()
        GameServer.removeLobby(this)
    }

}
