package server

import java.io.Serializable


abstract class DataPacket() : Serializable

class InitPacket(val playerName: String) : DataPacket()
class LobbyConnectPacket(val lobbyName: String) : DataPacket()
class LobbyFullPacket : DataPacket()

class LobbyStatusPacket : DataPacket()