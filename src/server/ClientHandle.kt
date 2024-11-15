package server

import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class ClientHandle(private val socket: Socket) : Closeable {
    private val dataIn: ObjectInputStream = ObjectInputStream(socket.getInputStream())
    private val dataOut: ObjectOutputStream = ObjectOutputStream(socket.getOutputStream())

    private var closed = false
    val isClosed get() = closed

    /**
     * Writes a DataPacket to the connection's output stream.
     * @throws IOException If an exception occurred while trying to write the packet.
     */
    fun send(data: InetPacket) {
        dataOut.writeObject(data)
        dataOut.reset()
    }

    /**
     * Reads an object from the connection's input stream.
     * @return The DataPacket that was read from the input stream.
     * @throws IOException If an exception occurred while trying to read data from the input stream.
     * @throws ClassNotFoundException If the read data is not of type DataPacket.
     */
    fun read(): InetPacket {
        return dataIn.readObject() as InetPacket
    }

    override fun close() {
        closed = true
        dataOut.close()
        dataIn.close()
        socket.close()
    }
}