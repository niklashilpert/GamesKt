package game.chess

import java.awt.Point

enum class ChessPieceType {
    PAWN,
    ROOK,
    KNIGHT,
    BISHOP,
    QUEEN,
    KING,
}

class ChessPiece(val type: ChessPieceType, val isWhite: Boolean) {
    val isBlack get() = !isWhite
}

class ChessBoard {
    private val currentPlayerIsWhite = true
    val whiteTurn get() = currentPlayerIsWhite
    val blackTurn get() = !currentPlayerIsWhite

    // Castling flags
    private var kingMoved = false
    private var leftRookMoved = false
    private var rightRookMoved = false

    // En passant
    private var spaceForEnPassant: Point? = null

    private val board = Array<Array<ChessPiece?>>(8) { arrayOfNulls(8) }
    init {
        board[7][0] = ChessPiece(ChessPieceType.ROOK, true)
        board[7][1] = ChessPiece(ChessPieceType.KNIGHT, true)
        board[7][2] = ChessPiece(ChessPieceType.BISHOP, true)
        board[7][3] = ChessPiece(ChessPieceType.QUEEN, true)
        board[7][4] = ChessPiece(ChessPieceType.KING, true)
        board[7][5] = ChessPiece(ChessPieceType.BISHOP, true)
        board[7][6] = ChessPiece(ChessPieceType.KNIGHT, true)
        board[7][7] = ChessPiece(ChessPieceType.ROOK, true)

        board[6][0] = ChessPiece(ChessPieceType.PAWN, true)
        board[6][1] = ChessPiece(ChessPieceType.PAWN, true)
        board[6][2] = ChessPiece(ChessPieceType.PAWN, true)
        board[6][3] = ChessPiece(ChessPieceType.PAWN, true)
        board[6][4] = ChessPiece(ChessPieceType.PAWN, true)
        board[6][5] = ChessPiece(ChessPieceType.PAWN, true)
        board[6][6] = ChessPiece(ChessPieceType.PAWN, true)
        board[6][7] = ChessPiece(ChessPieceType.PAWN, true)

        board[0][0] = ChessPiece(ChessPieceType.ROOK, false)
        board[0][1] = ChessPiece(ChessPieceType.KNIGHT, false)
        board[0][2] = ChessPiece(ChessPieceType.BISHOP, false)
        board[0][3] = ChessPiece(ChessPieceType.QUEEN, false)
        board[0][4] = ChessPiece(ChessPieceType.KING, false)
        board[0][5] = ChessPiece(ChessPieceType.BISHOP, false)
        board[0][6] = ChessPiece(ChessPieceType.KNIGHT, false)
        board[0][7] = ChessPiece(ChessPieceType.ROOK, false)

        board[1][0] = ChessPiece(ChessPieceType.PAWN, false)
        board[1][1] = ChessPiece(ChessPieceType.PAWN, false)
        board[1][2] = ChessPiece(ChessPieceType.PAWN, false)
        board[1][3] = ChessPiece(ChessPieceType.PAWN, false)
        board[1][4] = ChessPiece(ChessPieceType.PAWN, false)
        board[1][5] = ChessPiece(ChessPieceType.PAWN, false)
        board[1][6] = ChessPiece(ChessPieceType.PAWN, false)
        board[1][7] = ChessPiece(ChessPieceType.PAWN, false)

    }

    fun move(origin: Point, target: Point): Boolean {
        val movementOptions = getMovementOptions(origin.x, origin.y)
        if (target in movementOptions) {
            this[target] = this[origin]
            this[origin] = null
            return true
        }
        return false
    }

    fun isPieceWhite(point: Point): Boolean {
        return this[point]?.isWhite == true
    }
    fun isPieceBlack(point: Point): Boolean {
        return this[point]?.isBlack == true
    }

    fun getMovementOptions(x: Int, y: Int): Set<Point> {
        return when (this[x, y]?.type) {
            ChessPieceType.PAWN -> getPawnMovement(x, y)
            ChessPieceType.ROOK -> getRookMovement(x, y)
            ChessPieceType.KNIGHT -> getKnightMovement(x, y)
            ChessPieceType.BISHOP -> getBishopMovement(x, y)
            ChessPieceType.QUEEN -> getQueenMovement(x, y)
            ChessPieceType.KING -> getKingMovement(x, y)
            else -> setOf()
        }

    }

    private fun getPawnMovement(x: Int, y: Int): Set<Point> {
        val piece = this[x, y]
        val options: MutableSet<Point> = mutableSetOf()
        if (piece != null) {
            val oneForward = if (piece.isWhite) Point(x, y-1) else Point(x, y+1)
            val twoForward = if (piece.isWhite) Point(x, y-2) else Point(x, y+2)
            val leftDiagonal = if (piece.isWhite) Point(x-1, y-1) else Point(x-1, y+1)
            val rightDiagonal = if (piece.isWhite) Point(x+1, y-1) else Point(x+1, y+1)

            // Forward movement
            if (this[oneForward] == null) {
                options.add(oneForward)
                if (y == 6 && this[twoForward] == null) {
                    options.add(twoForward)
                }
            }

            // Diagonal Attacking
            val leftDiagonalPiece = this[leftDiagonal]
            if (leftDiagonalPiece?.isWhite != piece.isWhite || leftDiagonal == spaceForEnPassant) {
                options.add(leftDiagonal)
            }

            val rightDiagonalPiece = this[rightDiagonal]
            if (rightDiagonalPiece?.isWhite != piece.isWhite || rightDiagonal == spaceForEnPassant) {
                options.add(rightDiagonal)
            }
        }
        return options.filter { p -> inBounds(p) }.toSet()

    }
    private fun getRookMovement(x: Int, y: Int): Set<Point> {
        return getMovementOptionsInDirection(x, y, 0, -1) +
                getMovementOptionsInDirection(x, y, 0, 1) +
                getMovementOptionsInDirection(x, y, -1, 0) +
                getMovementOptionsInDirection(x, y, 1, 0)
    }

    private fun getKnightMovement(x: Int, y: Int): Set<Point> {
        val point = Point(x, y)
        val piece = this[point]
        val options: MutableSet<Point> = mutableSetOf()
        if (piece != null) {
            val topLeft = Point(x-1, y-2)
            val topRight= Point(x+1, y-2)
            val bottomLeft = Point(x-1, y+2)
            val bottomRight = Point(x+1, y+2)
            val leftTop = Point(x-2, y-1)
            val leftBottom = Point(x-2, y+1)
            val rightTop = Point(x+2, y-1)
            val rightBottom = Point(x+2, y+1)

            if (this[topLeft]?.isWhite != piece.isWhite) options.add(topLeft)
            if (this[topRight]?.isWhite != piece.isWhite) options.add(topRight)
            if (this[bottomLeft]?.isWhite != piece.isWhite) options.add(bottomLeft)
            if (this[bottomRight]?.isWhite != piece.isWhite) options.add(bottomRight)
            if (this[leftTop]?.isWhite != piece.isWhite) options.add(leftTop)
            if (this[leftBottom]?.isWhite != piece.isWhite) options.add(leftBottom)
            if (this[rightTop]?.isWhite != piece.isWhite) options.add(rightTop)
            if (this[rightBottom]?.isWhite != piece.isWhite) options.add(rightBottom)
        }
        return options.filter { p -> inBounds(p) }.toSet()
    }
    private fun getBishopMovement(x: Int, y: Int): Set<Point> {
        return getMovementOptionsInDirection(x, y, -1, -1) +
                getMovementOptionsInDirection(x, y, -1, 1) +
                getMovementOptionsInDirection(x, y, 1, -1) +
                getMovementOptionsInDirection(x, y, 1, 1)
    }
    private fun getQueenMovement(x: Int, y: Int): Set<Point> {
        return getBishopMovement(x, y) + getRookMovement(x, y)
    }
    private fun getKingMovement(x: Int, y: Int): Set<Point> {
        val piece = this[x, y]
        var options: MutableSet<Point> = mutableSetOf()

        if (piece != null) {
            options.add(Point(x-1, y-1))
            options.add(Point(x, y-1))
            options.add(Point(x+1, y-1))
            options.add(Point(x-1, y))
            options.add(Point(x+1, y))
            options.add(Point(x-1, y+1))
            options.add(Point(x, y+1))
            options.add(Point(x+1, y+1))

            val threatenedFields = getThreatenedFields(piece.isWhite)
            options = options.filter { p -> inBounds(p) && !threatenedFields.contains(p) }.toMutableSet()

            if (!kingMoved && !leftRookMoved) {
                var canLeftCastle = true
                for (i in x-1 downTo 0) {
                    if (threatenedFields.contains(Point(i, y)) || this[i, y] != null) {
                        canLeftCastle = false
                        break
                    }
                }
                if (canLeftCastle) {
                    options.add(Point(x-2, y))
                }
            }

            if (!kingMoved && !rightRookMoved) {
                var canRightCastle = true
                for (i in x+1.. 7) {
                    if (threatenedFields.contains(Point(i, y)) || this[i, y] != null) {
                        canRightCastle = false
                        break
                    }
                }
                if (canRightCastle) {
                    options.add(Point(x+2, y))
                }
            }

        }
        return options
    }

    private fun getThreatenedFields(isPlayerWhite: Boolean): Set<Point> {
        val threatenedFields: MutableSet<Point> = mutableSetOf()
        for (y in board.indices) {
            for (x in board[y].indices) {
                val piece = this[x, y]
                if (piece != null && piece.isWhite != isPlayerWhite) {
                    threatenedFields.addAll(getMovementOptions(x, y))
                }
            }
        }
        return threatenedFields
    }

    private fun getMovementOptionsInDirection(x: Int, y: Int, xStep: Int, yStep: Int): Set<Point> {
        val piece = this[x, y]
        val options: MutableSet<Point> = mutableSetOf()
        if (piece != null) {
            var pointX = x + xStep
            var pointY = y + yStep
            while(inBounds(pointX, pointY) && this[pointX, pointY] == null) {
                options.add(Point(pointX, pointY))
                pointX += xStep
                pointY += yStep
            }
            if (this[pointX, pointY]?.isWhite != piece.isWhite) {
                options.add(Point(pointX, pointY))
            }
        }
        return options
    }


    operator fun get(x: Int, y: Int) = this[Point(x, y)]
    operator fun get(point: Point): ChessPiece? {
        return if (inBounds(point)) board[point.y][point.x] else null
    }
    private operator fun set(point: Point, chessPiece: ChessPiece?): Boolean {
        return if (inBounds(point)) { board[point.y][point.x] = chessPiece; true } else { false }
    }

    private fun inBounds(point: Point) = inBounds(point.x, point.y)
    private fun inBounds(x: Int, y: Int): Boolean {
        return x in 0..7 && y in 0..7
    }
}