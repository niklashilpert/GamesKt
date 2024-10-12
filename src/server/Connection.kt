package server

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class Connection(private val socket: Socket) {
    private val dataIn = ObjectInputStream(socket.getInputStream())
    private val dataOut = ObjectOutputStream(socket.getOutputStream())

    var name: String? = null

    fun send(data: DataPacket) {
        dataOut.writeObject(data)
    }

    fun read(): DataPacket? {
        try {
            val packet = dataIn.readObject() as DataPacket
            return packet
        } catch (e: Exception) {
            return null
        }
    }

    fun close() {
        dataOut.close()
        dataIn.close()
        socket.close()
    }

}