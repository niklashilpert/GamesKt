package game.chess


/*class ChessLobby(lobbyName: String, host: Client) : Lobby(lobbyName, host) {
    private val playerLock = Any()

    private var playerWhite: Client? = host
    private var playerBlack: Client? = null

    init {
        updateClients()
    }

    override fun onMessageReceivedWhileOpen(message: Message) {
        val packet = message.packet
        val client = message.source
        try {
            if (client == host) {
                when (packet) {
                    is InetPacket.SwapPlayers -> {
                        val temp = playerWhite
                        playerWhite = playerBlack
                        playerBlack = temp
                        updateClients()
                    }

                    is InetPacket.StartGame -> {
                        if (playerWhite != null && playerBlack != null) {
                            _isRunning.set(true)
                            println("Starting lobby $name")
                            updateClients()
                        } else {
                            client.send(InetPacket.CannotStartGame())
                        }
                    }

                    else -> {
                        client.send(InetPacket.IllegalPacket())
                    }
                }
            } else {
                client.send(InetPacket.NotAuthorized())
            }
        } catch (e: IOException) {
            client.close()
            removeClient(client)
        }
    }

    override fun onMessageReceivedWhileRunning(message: Message) {
        TODO("Not yet implemented")
    }

    override fun playerExists(playerName: String): Boolean {
        synchronized(playerLock) {
            return (playerWhite != null && playerWhite!!.name == playerName) ||
                    (playerBlack != null && playerBlack!!.name == playerName)
        }
    }

    override fun addClient(client: Client) {
        synchronized(playerLock) {
            if (playerWhite == null) {
                playerWhite = client
            } else {
                playerBlack = client
            }
        }
        updateClients()
    }

    override fun isFull(): Boolean {
        synchronized(playerLock) {
            return playerWhite != null && playerBlack != null
        }
    }

    private fun getInfoPacket(): ChessLobbyInfo {
        return ChessLobbyInfo(name, isRunning, host.name, playerWhite?.name, playerBlack?.name)
    }

    private fun updateClients() {
        synchronized(playerLock) {
            val lobbyInfo = InetPacket.LobbyStatus(getInfoPacket())
            playerWhite?.send(lobbyInfo)
            playerBlack?.send(lobbyInfo)
        }
    }

    override fun removeClient(client: Client) {
        synchronized(playerLock) {
            if (client == host) {
                if (client == playerWhite && playerBlack != null) {
                    host = playerBlack!!
                } else if (client == playerBlack && playerWhite != null) {
                    host = playerWhite!!
                }
            }

            if (playerWhite == client) {
                playerWhite?.close()
                playerWhite = null
            } else {
                playerBlack?.close()
                playerBlack = null
            }
            if (playerWhite == null && playerBlack == null) {
                terminateLobby()
            }
        }
        updateClients()
    }
}*/