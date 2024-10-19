package server.lobby

import server.Client
import server.Message

class CheckersLobby(lobbyName: String, host: Client): Lobby(lobbyName, host) {
    override fun isFull(): Boolean {
        TODO("Not yet implemented")
    }

    override fun playerExists(playerName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun addClient(client: Client) {
        TODO("Not yet implemented")
    }

    override fun removeClient(client: Client) {
        TODO("Not yet implemented")
    }

    override fun onMessageReceivedWhileOpen(message: Message) {
        TODO("Not yet implemented")
    }

    override fun onMessageReceivedWhileRunning(message: Message) {
        TODO("Not yet implemented")
    }

}