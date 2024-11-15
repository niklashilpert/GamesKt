package game.tictactoe

import java.io.Serializable

class TicTacToe {
    enum class Status {
        TIE,
        X_WON,
        O_WON,
        IN_PROGRESS,
    }

    class Info(
        val board: Array<IntArray>,
        val currentPlayerIsX: Boolean,
        val xWon: Boolean,
        val oWon: Boolean,
        val tie: Boolean,
        val inProgress: Boolean
    ) : Serializable

    fun getInfo(): Info {
        val gameStatus = checkGameStatus()
        return Info(
            board = board,
            currentPlayerIsX = currentPlayerIsX,
            xWon = gameStatus == Status.X_WON,
            oWon = gameStatus == Status.O_WON,
            tie = gameStatus == Status.TIE,
            inProgress = gameStatus == Status.IN_PROGRESS
        )
    }

    val board = Array(3) { intArrayOf(0, 0, 0) }

    var _currentPlayerIsX = true
    val currentPlayerIsX get() = _currentPlayerIsX

    fun placeMark(x: Int, y: Int): Boolean {
        if (inBounds(x, y)) {
            if (board[x][y] == 0) {
                board[x][y] = if (currentPlayerIsX) 1 else -1
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

    fun inBounds(x: Int, y: Int) = x in 0..2 && y in 0..2
}