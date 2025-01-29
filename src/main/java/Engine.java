
import java.util.Random;
import javax.swing.ImageIcon;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author droii
 */
public class Engine {

    private final ImageIcon ICON_O = new ImageIcon(getClass().getClassLoader().getResource("./icon/o.png"));
    private final ImageIcon ICON_X = new ImageIcon(getClass().getClassLoader().getResource("./icon/x.png"));
    public final Random random = new Random();
    private final int XROWS = 3;
    private final int YCOLS = 3;

    private char player = ' ';
    private char[][] board;
    private GFG.Move CPU;
    private boolean enableCPU;

    public Engine() {
    }

    public void gameStart() {
        char[][] grid = new char[XROWS][YCOLS];

        for (int i = 0; i < XROWS; i++) {
            for (int j = 0; j < YCOLS; j++) {
                grid[i][j] = '_';
            }
        }

        setBoard(grid);

        if (!isEnableCPU()) {
            switchPlayer();
            return;
        }

        setPlayer('o');
    }

    public void displayBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public boolean play(int x, int y) {
        if (x < 0 || x >= board.length || y < 0 || y >= board[x].length) { //x >= 0 && x < board.length && y >= 0 && y < board[x].length
            return false;
        }

        if (board[x][y] != '_') {
            return false;
        }

        if (hasWon('x') || hasWon('o')) {
            //Game is won, no more plays will be done
            return false;
        }

        board[x][y] = getPlayer();

        if (!isEnableCPU()) {
            switchPlayer();
            return true;
        }

        cpuPlay();

        return true;
    }

    public void cpuPlay() {
        CPU = GFG.findBestMove(getBoard());

        if (!allPositionsFilled(getBoard())) {
            board[CPU.row][CPU.col] = 'x';
            //System.out.println(CPU.row + " - " + CPU.col);
        }
    }

    public boolean allPositionsFilled(char[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == '_') {
                    return false; // Found an empty position
                }
            }
        }
        return true; // All positions are filled
    }

    public void switchPlayer() {
        if (getPlayer() != ' ') {
            setPlayer((getPlayer() == 'x') ? 'o' : 'x');
            return;
        }

        if (random.nextBoolean()) {
            setPlayer('x');
            return;
        }

        setPlayer('o');
    }

    public ImageIcon getPlayerIcon() {
        if (isEnableCPU()) {
            return getICON_O();
        }

        return (getPlayer() == 'x') ? getICON_O() : getICON_X();
    }

    public boolean hasWon(char player) {
        char[][] table;
        table = getBoard();

        // Check rows
        for (int i = 0; i < 3; i++) {
            if (table[i][0] == player && table[i][1] == player && table[i][2] == player) {
                setPlayer(player);
                return true;
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (table[0][i] == player && table[1][i] == player && table[2][i] == player) {
                setPlayer(player);
                return true;
            }
        }

        // Check main diagonal
        if (table[0][0] == player && table[1][1] == player && table[2][2] == player) {
            setPlayer(player);
            return true;
        }

        // Check anti-diagonal
        if (table[0][2] == player && table[1][1] == player && table[2][0] == player) {
            setPlayer(player);
            return true;
        }

        return false;
    }

    public char getPlayer() {
        return player;
    }

    public void setPlayer(char player) {
        this.player = player;
    }

    public ImageIcon getICON_O() {
        return ICON_O;
    }

    public ImageIcon getICON_X() {
        return ICON_X;
    }

    public char[][] getBoard() {
        return board;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public boolean isEnableCPU() {
        return enableCPU;
    }

    public void setEnableCPU(boolean enableCPU) {
        this.enableCPU = enableCPU;
    }

}
