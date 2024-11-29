package client

import server.InetPacket
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class ConnectionHandle(val name: String, val socket: Socket) : Closeable {
    val dataOut = ObjectOutputStream(socket.getOutputStream())
    val dataIn = ObjectInputStream(socket.getInputStream())
    var isClosed = false
        private set

    fun send(packet: InetPacket) {
    }

    fun read(): InetPacket? {
        return null
    }

    override fun close() {
        if (!isClosed) {
            isClosed = true
            dataIn.close()
            dataOut.close()
            socket.close()
        }
    }


}