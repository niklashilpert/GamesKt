package server.lobby

import server.*
import java.io.IOException
import java.io.Serializable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

enum class GameType {
    CHESS,
    CHECKERS,
    TIC_TAC_TOE
}

fun createLobby(type: GameType, name: String) : Lobby {
    return when (type) {
        GameType.CHESS -> {
            TODO("Chess is not implemented yet")
        }
        GameType.CHECKERS -> {
            TODO("Checkers is not implemented yet")
        }
        GameType.TIC_TAC_TOE -> {
            TicTacToeLobby(name)
        }
    }
}


abstract class Lobby internal constructor(val name: String, private val maxPlayerCount: Int) {
    abstract class Info(val lobbyName: String, val isOpen: Boolean, val host: String?) : Serializable

    abstract inner class Task(val source: Player, val perform: (Task) -> Boolean)
    protected inner class JoinTask(source: Player) : Task(source, ::performJoin)
    protected inner class LeaveTask(source: Player) : Task(source, ::performLeave)
    protected inner class StartGameTask(source: Player) : Task(source, ::performStartGame)
    protected inner class StopGameTask(source: Player) : Task(source, ::performStopGame)
    protected inner class UpdatePlayerTask(source: Player): Task(source, ::performPlayerUpdate)

    private var _isOpen = AtomicBoolean(true)
    var isOpen
        get() = _isOpen.get()
        protected set(value) = _isOpen.set(value)

    protected val taskQueue = LinkedBlockingQueue<Task>()
    protected var host: Player? = null

    private var keepTaskThreadRunning = AtomicBoolean(true)
    private val taskThread: Thread = createTaskHandlerThread().also { it.start() }

    private fun createTaskHandlerThread(): Thread {
        return Thread {
            while(keepTaskThreadRunning.get()) {
                try {
                    val nextTask = taskQueue.take()
                    log("Next task in queue: ${nextTask.javaClass.simpleName}")
                    val notify = nextTask.perform(nextTask)
                    if (notify) notifyPlayers()
                } catch (e: InterruptedException) {
                    keepTaskThreadRunning.set(false)
                }
            }
            log("Stopping task handling thread for lobby $name.")
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
                }
            }
            log("Stopped player listening thread for ${player.name}")
            queueLeave(player)
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
        val isNotMarkedForDeletion = taskQueue.find { it is LeaveTask && it.source == player } == null
        if (isNotMarkedForDeletion) {
            taskQueue.put(LeaveTask(player))
        }
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
    private fun performJoin(task: Task): Boolean {
        val source = task.source
        if (getPlayers().find { it.name == source.name } != null) {
            source.send(InetPacket.PlayerExists())
            source.close()
            return false
        } else if (isFull()) {
            source.send(InetPacket.LobbyIsFull())
            source.close()
            return false
        } else if (!isOpen) {
            source.send(InetPacket.LobbyIsPlaying())
            source.close()
            return false
        } else {
            if (host == null) {
                host = source
            }
            createPlayerListeningThread(source).start()
            source.send(InetPacket.Success())
            put(source)
            log("Player ${source.name} joined lobby $name.")
            return true
        }
    }

    /**
     * Removes the player from the players list.
     * If they are the host, the next host candidate is promoted.
     * If they are the last player in the lobby, it will be terminated.
     * If the lobby's game was in progress, it is interrupted and the lobby returns to the open state.
     */
    private fun performLeave(task: Task): Boolean {
        if (!isOpen) performStopGameUnchecked()
        val source = task.source
        if (getPlayers().contains(source)) {
            pop(source)
            log("Player ${source.name} left lobby $name.")
            if (source == host) {
                host = getPlayers().getOrNull(0)
                if (host != null) {
                    log("Player ${host!!.name} is now host of lobby $name.")
                } else {
                    terminateLobby()
                }
            }
            return true
        }
        return false
    }

    /**
     * Tries to start the game if it can.
     * Sends an error to back to the client if it can't start.
     */
    private fun performStartGame(task: Task): Boolean {
        val source = task.source

        return handleIfHost(source) {
            if (!isOpen) return@handleIfHost false
            if (isFull()) {
                log("Starting game")
                isOpen = false
                handleGameStart()
                return@handleIfHost true
            } else {
                try {
                    source.send(InetPacket.CannotStartGame())
                } catch (e: IOException) {
                    source.close()
                }
                return@handleIfHost false
            }
        }
    }
    
    private fun performStopGame(task: Task): Boolean {
        return handleIfHost(task.source) {
            if (isOpen) return@handleIfHost false
            performStopGameUnchecked()
            return@handleIfHost true
        }
    }

    protected fun performStopGameUnchecked() {
        log("Stopping game")
        handleGameStop()
        isOpen = true
    }

    private fun performPlayerUpdate(task: Task): Boolean {
        log("Updating ${task.source.name}")
        notifyPlayer(task.source)
        return false
    }

    /**
     * Performs the operations that are required for a specific game to start.
     */
    protected abstract fun handleGameStart()

    /**
     * Performs the operations that are required for a specific game to stop.
     */
    protected abstract fun handleGameStop()

    /**
     * Returns a LobbyInfo packet that can be sent to notify clients abount updates.
     */
    protected abstract fun getLobbyInfoPacket(): InetPacket.LobbyInfo

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
    private fun notifyPlayers() {
        for (player in getPlayers()) {
            notifyPlayer(player)
        }
    }

    private fun notifyPlayer(player: Player) {
        if (!player.isClosed) {
            log("Notifying player")
            try {
                player.send(getLobbyInfoPacket())
            } catch (e: IOException) {
                player.close()
            }
        }
    }

    /**
     * Returns a list of all registered players.
     */
    protected abstract fun getPlayers(): List<Player>

    /**
     * Returns whether the maximum player amount has been reached.
     */
    protected fun isFull(): Boolean {
        return getPlayers().size == maxPlayerCount
    }

    /**
     * Only performs the provided action if the player is the host of the lobby.
     * If not, this method sends a NotAuthorized packet to the player.
     *
     * If the provided player is null, the action is executed.
     */
    protected fun handleIfHost(player: Player, action: (Player) -> Boolean): Boolean {
        if (player == host) {
            return action(player)
        } else {
            player.send(InetPacket.NotAuthorized())
            return false
        }
    }

    /**
     * This function returns false if the lobby is open. Will execute the action and return true otherwise.
     */
    protected fun handleIfRunning(source: Player, action: () -> Boolean): Boolean {
        return if (!isOpen) {
            action()
            true
        } else {
            false
        }
    }

    /**
     * This function returns false if the game is running. Will execute the action and return true otherwise.
     */
    protected fun handleIfOpen(source: Player, action: () -> Boolean): Boolean {
        return if (isOpen) {
            action()
            true
        } else {
            false
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

    protected fun log(msg: String) {
        println("[$name] $msg")
    }
    
}
