package server.lobby

import server.GameServer
import server.InetPacket
import server.Player
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

fun createLobby(type: GameType, name: String) : Lobby {
    return when (type) {
        GameType.CHESS -> {
            TODO("Chess is not implemented yet")
        }
        GameType.CHECKERS -> {
            TODO("Checkers is not implemented yet")
        }
        GameType.TICTACTOE -> {
            TODO("TicTacToe is not implemented yet")
            //TicTacToeLobby(name, host)
        }
    }
}

class Lobby(val name: String, val playerCount: Int) {

    abstract inner class Task {
        abstract fun perform()
    }

    inner class JoinTask(val player: Player) : Task() {
        override fun perform() {
            add(player)
        }
    }

    inner class LeaveTask(val player: Player) : Task() {
        override fun perform() {
            leave(player)
        }
    }

    private var nullableHost: Player? = null
    private var host: Player
        get() = nullableHost!!
        set(value) {
            nullableHost = value
        }

    private var keepTaskThreadRunning = AtomicBoolean(true)
    private var isOpen = AtomicBoolean(true)

    private val taskQueue = LinkedBlockingQueue<Task>()

    private val players = Collections.synchronizedList(ArrayList<Player>())

    private val taskThread: Thread = createTaskHandlerThread()

    private fun createTaskHandlerThread(): Thread {
        return Thread {
            while(keepTaskThreadRunning.get()) {
                try {
                    taskQueue.take().perform()
                    notifyPlayers()
                } catch (e: InterruptedException) {
                    keepTaskThreadRunning.set(false)
                }
            }
            println("Stopping task handling thread for lobby $name.")
        }
    }

    /**
     * Schedules a join task in the task queue.
     */
    fun scheduleJoin(player: Player) {
        taskQueue.put(JoinTask(player))
    }

    /**
     * Schedules a leave task in the task queue.
     */
    private fun scheduleLeave(player: Player) {
        taskQueue.put(LeaveTask(player))
    }

    /**
     * Adds the player to the players list if it is allowed.
     * The player will be notified if it can't join the lobby.
     * Reasons for rejection are:
     * - a player with the same name already exists
     * - the lobby is full
     * - the lobby is already playing
     */
    private fun add(player: Player) {
        if (players.find { it.name == player.name } != null) {
            player.send(InetPacket.PlayerExists())
            player.close()
        } else if (players.size == playerCount) {
            player.send(InetPacket.LobbyIsFull())
            player.close()
        } else if (!isOpen()) {
            player.send(InetPacket.LobbyIsPlaying())
            player.close()
        } else {
            players.addLast(player)
            player.send(InetPacket.Success())
            println("Player ${player.name} joined lobby $name.")
        }
    }


    /**
     * Removes the player from the players list.
     * If they are the host, the next player in the list is promoted.
     * If they are the last player in the lobby, it will be terminated.
     * If the lobby's game was in progress, it is interrupted and the lobby returns to the open state.
     */
    private fun leave(player: Player) {
        players.remove(player)
        if (player == nullableHost) {
            if (players.size > 0) {
                host = players[0]
            } else {
                terminateLobby()
            }
        }

        if (!isOpen()) {
            stopGame()
        }
    }

    private fun notifyPlayers() {

    }

    private fun stopGame() {

    }

    /**
     * Returns whether the lobby is open, and it's game is not in progress.
     */
    fun isOpen() = isOpen.get()

    /**
     * Stops the task thread and removes the lobby from the game server's registry.
     */
    private fun terminateLobby() {
        keepTaskThreadRunning.set(false)
        taskThread.interrupt()
        GameServer.removeLobby(this)
    }

}
