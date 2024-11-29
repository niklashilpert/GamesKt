package client

import client.ui.InitFrame
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities

class ClientManager {
    val initFrame = InitFrame()
    init {
        SwingUtilities.invokeLater {
            initFrame.isVisible = true
        }
    }
}