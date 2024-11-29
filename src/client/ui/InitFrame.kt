package client.ui

import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class InitFrame : JFrame() {
    val titleLabel = JLabel("Welcome to GamesKt!")
    val connectionLabel = JLabel("Enter connection details:")
    val connectionInputField = JTextField()
    val playerNameLabel = JLabel("Enter your player name:")
    val playerNameInputField = JTextField()
    val lobbyNameLabel = JLabel("Enter lobby name:")
    val lobbyNameInputField = JTextField()

    val panel = JPanel().apply {
        add(titleLabel)
        add(connectionLabel)
        add(connectionInputField)
        add(playerNameLabel)
        add(playerNameInputField)
        add(lobbyNameLabel)
        add(lobbyNameInputField)
    }
    init {
        contentPane = panel
        pack()
        setLocationRelativeTo(null)
    }
}