package server

import server.lobby.Lobby
import java.io.Closeable
import java.io.IOException
import java.io.Serializable

class Player(private val client: ClientHandle, val name: String) : Serializable, Closeable {
    private val readLock = Any()
    private val sendLock = Any()
    private var _isClosed = false 
    val isClosed: Boolean get() = _isClosed

    fun read(): InetPacket {
        synchronized(readLock) {
            return client.read()
        }
    }

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
        _isClosed = true
        if (!client.isClosed) {
            client.close()
        }
    }
}