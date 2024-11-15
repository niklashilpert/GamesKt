package server

import java.io.Closeable
import java.io.IOException
import java.io.Serializable

class Player(private val client: ClientHandle, val name: String) : Serializable, Closeable {
    private val readLock = Any()
    private val sendLock = Any()
    private var _isClosed = false 
    val isClosed: Boolean get() = _isClosed

    fun tryRead(): InetPacket? {
        if (!isClosed) {
            synchronized(readLock) {
                try {
                    return client.read()
                } catch (e: Exception) {
                    if (e !is IOException) {
                        println("[Reading Error] $name: ${e.message ?: ""}")
                    }
                    close()
                    return null
                }
            }
        }
        return null

    }

    /**
     * Tries to send the packet to the player.
     * This method will close the connection if an error occurs.
     * @return Whether the packet was sent successfully.
     */
    fun trySend(packet: InetPacket): Boolean {
        if (!isClosed) {
            synchronized(sendLock) {
                try {
                    client.send(packet)
                    return true
                } catch (e: Exception) {
                    if (e !is IOException) {
                        println("[Writing Error] $name: ${e.message ?: ""}")
                    }
                    close()
                    return false
                }
            }
        }
        return false
    }

    fun tryRespond(resultCode: ResultCode): Boolean {
        return trySend(InetPacket.Result(resultCode))
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