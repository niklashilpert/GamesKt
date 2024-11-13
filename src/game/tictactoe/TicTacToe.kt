package game.tictactoe

class TicTacToe {
    enum class Status {
        TIE,
        X_WON,
        O_WON,
        IN_PROGRESS,
    }

    val board = Array(3) { arrayOf(0, 0, 0) }

    var _currentPlayerIsX = true
    val currentPlayerIsX get() = _currentPlayerIsX

    fun placeMark(x: Int, y: Int): Boolean {
        if (inBounds(x, y)) {
            if (board[y][x] == 0) {
                board[y][x] = if (currentPlayerIsX) 1 else -1
                _currentPlayerIsX = !_currentPlayerIsX
                return true
            }
        }
        return false
    }

    fun checkGameStatus(): Status {
        val winner = checkForWinner()
        when (winner) {
            0 -> {
                var full = true
                for (row in board) {
                    for (cell in row) {
                        if (cell == 0) full = false
                    }
                }
                return if (full) Status.TIE else Status.IN_PROGRESS
            }
            1 -> {
                return Status.X_WON
            }
            else -> { // -1
                return Status.O_WON
            }
        }
    }

    private fun checkForWinner(): Int {
        val winningConditions = arrayOf(
            board[0][0] + board[0][1] + board[0][2],
            board[1][0] + board[1][1] + board[1][2],
            board[2][0] + board[2][1] + board[2][2],
            board[0][0] + board[1][0] + board[2][0],
            board[0][1] + board[1][1] + board[2][1],
            board[0][2] + board[1][2] + board[2][2],
            board[0][0] + board[1][1] + board[2][2],
            board[2][0] + board[1][1] + board[0][2]
        )

        return if (winningConditions.contains(3)) {
            1
        } else if (winningConditions.contains(-3)) {
            -1
        } else {
            0
        }
    }

    private fun inBounds(x: Int, y: Int) = x in 0..2 && y in 0..2

}