package server

import server.lobby.Lobby
import java.io.Closeable
import java.io.IOException
import java.io.Serializable

class Player(private val client: ClientHandle, val name: String, val lobby: Lobby) : Serializable, Closeable {
    private var keepListening = true
    private var sendLock = Any()

    /**
     * Sends the InetPacket to the client. This call is synchronized.
     */
    fun send(data: InetPacket) {
        synchronized(sendLock) {
            client.send(data)
        }
    }

    /**
     * Closes the connection and stops the listening thread.
     */
    override fun close() {
        keepListening = false
        if (!client.isClosed) {
            client.close()
        }
    }

    fun startReceivingMessages(handleMessageFunction: (Message) -> Unit) {
        Thread {
            while(keepListening) {
                try {
                    val packet = client.read()
                    handleMessageFunction(Message(this, packet))
                } catch (e: IOException) {
                    close()
                    handleMessageFunction(Message(this, DataPacket.ConnectionLost()))
                }
            }
        }.start()
    }
}