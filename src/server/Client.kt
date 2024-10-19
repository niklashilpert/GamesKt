package server

import java.io.Closeable
import java.io.IOException
import java.io.Serializable

class Client(private val connection: Connection, val name: String) : Serializable, Closeable {
    private var keepListening = true
    private var sendLock = Any()
    /**
     * Sends the InetPacket to the client. This call is synchronized.
     */
    fun send(data: InetPacket) {
        synchronized(sendLock) {
            connection.send(data)
        }
    }

    /**
     * Closes the connection and stops the listening thread.
     */
    override fun close() {
        keepListening = false
        if (!connection.isClosed) {
            connection.close()
        }
    }

    fun startReceivingMessages(handleMessageFunction: (Message) -> Unit) {
        Thread {
            while(keepListening) {
                try {
                    val packet = connection.read()
                    handleMessageFunction(Message(this, packet))
                } catch (e: IOException) {
                    close()
                    handleMessageFunction(Message(this, DataPacket.ConnectionLost()))
                }
            }
        }.start()
    }
}