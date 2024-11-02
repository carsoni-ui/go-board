import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

public class GoBoard {
    // Constants for board size and players
    private static final int BOARD_SIZE = 9;
    private static final char EMPTY = '+';
    private static final char BLACK = 'B';
    private static final char WHITE = 'W';

    private char[][] board;

    public GoBoard() {
        board = new char[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    // Initializes the board with EMPTY cells
    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    // Displays the board with row and column numbers
    public void displayBoard() {
        System.out.print("  ");
        for (int i = 1; i <= BOARD_SIZE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // Checks if the position is within the board boundaries
    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    // Returns the opponent of the current player
    private char getOpponent(char player) {
        return (player == BLACK) ? WHITE : BLACK;
    }

    // Returns a list of adjacent positions to (x, y)
    private int[][] getAdjacentPositions(int x, int y) {
        int[][] directions = { {-1,0}, {1,0}, {0,-1}, {0,1} };
        Set<String> adjSet = new HashSet<>();
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (isWithinBounds(nx, ny)) {
                adjSet.add(nx + "," + ny);
            }
        }
        int size = adjSet.size();
        int[][] adj = new int[size][2];
        int idx = 0;
        for (String pos : adjSet) {
            String[] parts = pos.split(",");
            adj[idx][0] = Integer.parseInt(parts[0]);
            adj[idx][1] = Integer.parseInt(parts[1]);
            idx++;
        }
        return adj;
    }

    // Recursively finds all connected stones of the same color
    private Set<String> getGroup(int x, int y, Set<String> visited) {
        Set<String> group = new HashSet<>();
        char color = board[x][y];
        String key = x + "," + y;
        group.add(key);
        visited.add(key);

        for (int[] pos : getAdjacentPositions(x, y)) {
            int nx = pos[0];
            int ny = pos[1];
            String adjKey = nx + "," + ny;
            if (board[nx][ny] == color && !visited.contains(adjKey)) {
                group.addAll(getGroup(nx, ny, visited));
            }
        }
        return group;
    }

    // Checks if the group has any liberties
    private boolean hasLiberties(Set<String> group) {
        for (String pos : group) {
            String[] parts = pos.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            // Check all adjacent positions for liberties
            for (int[] adj : getAdjacentPositions(x, y)) {
                if (board[adj[0]][adj[1]] == EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    // Captures opponent stones if they have no liberties after a move
    private boolean captureStones(char player, int x, int y) {
        char opponent = getOpponent(player);
        Set<String> stonesToCapture = new HashSet<>();

        // Check all adjacent opponent groups
        for (int[] pos : getAdjacentPositions(x, y)) {
            int nx = pos[0];
            int ny = pos[1];
            if (board[nx][ny] == opponent) {
                String key = nx + "," + ny;
                if (!stonesToCapture.contains(key)) {
                    Set<String> group = getGroup(nx, ny, new HashSet<>());
                    if (!hasLiberties(group)) {
                        stonesToCapture.addAll(group);
                    }
                }
            }
        }

        // Remove captured stones
        for (String pos : stonesToCapture) {
            String[] parts = pos.split(",");
            int cx = Integer.parseInt(parts[0]);
            int cy = Integer.parseInt(parts[1]);
            board[cx][cy] = EMPTY;
            System.out.println("Captured " + opponent + " stone at (" + (cx +1) + ", " + (cy +1) + ")");
        }

        // Check if the player's own stone has no liberties (suicide)
        Set<String> ownGroup = getGroup(x, y, new HashSet<>());
        if (!hasLiberties(ownGroup)) {
            System.out.println("Move at (" + (x +1) + ", " + (y +1) + ") is suicide. Move rejected.");
            board[x][y] = EMPTY;
            return false;
        }

        return true;
    }

    // Attempts to place a stone on the board
    public boolean placeStone(char player, int x, int y) {
        if (!isWithinBounds(x, y)) {
            System.out.println("Position out of bounds. Try again.");
            return false;
        }
        if (board[x][y] != EMPTY) {
            System.out.println("Position already occupied. Try again.");
            return false;
        }
        board[x][y] = player;
        if (!captureStones(player, x, y)) {
            return false;
        }
        return true;
    }

    // Calculates the score for both players
    public int[] calculateScore() {
        int blackScore = 0;
        int whiteScore = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == BLACK) {
                    blackScore++;
                } else if (board[i][j] == WHITE) {
                    whiteScore++;
                }
            }
        }
        return new int[] { blackScore, whiteScore };
    }

    // Main game loop
    public void playGame() {
        Scanner scanner = new Scanner(System.in);
        char currentPlayer = BLACK;

        while (true) {
            displayBoard();
            System.out.println("Current Player: " + (currentPlayer == BLACK ? "Black" : "White"));
            System.out.print("Enter your move as 'row col' or 'pass' to end the game: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("pass")) {
                break;
            }

            String[] tokens = input.split("\\s+");
            if (tokens.length != 2) {
                System.out.println("Invalid input. Please enter row and column numbers separated by a space.");
                continue;
            }

            int x, y;
            try {
                x = Integer.parseInt(tokens[0]) - 1;
                y = Integer.parseInt(tokens[1]) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter numeric row and column numbers.");
                continue;
            }

            if (placeStone(currentPlayer, x, y)) {
                currentPlayer = getOpponent(currentPlayer);
            } else {
                System.out.println("Move invalid. Try again.");
            }
        }

        scanner.close();
        displayBoard();
        int[] scores = calculateScore();
        System.out.println("Final Score:");
        System.out.println("Black: " + scores[0]);
        System.out.println("White: " + scores[1]);
        if (scores[0] > scores[1]) {
            System.out.println("Black wins!");
        } else if (scores[1] > scores[0]) {
            System.out.println("White wins!");
        } else {
            System.out.println("It's a tie!");
        }
    }

    // Entry point
    public static void main(String[] args) {
        GoBoard game = new GoBoard();
        game.playGame();
    }
}
