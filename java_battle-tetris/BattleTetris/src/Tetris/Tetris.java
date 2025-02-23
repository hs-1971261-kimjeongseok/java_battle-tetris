package Tetris;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import Tetris.Shape.Tetrominoe;

/*
Java Tetris game clone

Author: Jan Bodnar
Website: https://zetcode.com
 */
public class Tetris extends JFrame {
    Board board;
    Board nextBlockBoard;
    Board[] otherBoard = new Board[4];
    public int attack = 0;
    JLabel label;


    public Tetris(Tetrominoe[] list) {
        initUI(list);
        
    }

    public void setLabelText(int i) {
        label.setText(i + " 줄 후 공격");
    }

    private void initUI(Tetrominoe[] list) {
        this.setLayout(new BorderLayout());
        JPanel myPanel = new JPanel(new BorderLayout());
        JPanel otherPanel = new JPanel(new GridLayout(2, 2));

        board = new Board(this);
        board.setList(list);
        board.start();
        myPanel.add(board, BorderLayout.CENTER);

        nextBlockBoard = new Board(this, 5, 22);
        nextBlockBoard.set();
        nextBlockBoard.setBorder(new TitledBorder(new LineBorder(Color.black, 1), "next"));

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(75, 400));

        infoPanel.add(nextBlockBoard, BorderLayout.CENTER);
        JPanel linePanel = new JPanel(new BorderLayout());
        label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        linePanel.add(label);
        linePanel.setPreferredSize(new Dimension(75, 50));
        linePanel.setBorder(new LineBorder(Color.black,1));
        infoPanel.add(linePanel, BorderLayout.SOUTH);
        myPanel.add(infoPanel, BorderLayout.EAST);
        myPanel.setBorder(new TitledBorder(new LineBorder(Color.black,5),"테트리스"));

        for (int i = 0; i < 4; i++) {
            otherBoard[i] = new Board(this);
            otherBoard[i].set();
            otherBoard[i].setBorder(new TitledBorder(new LineBorder(Color.black,1),"상대 "+(i+1)));
            otherPanel.add(otherBoard[i]);
        }
        
        otherPanel.setPreferredSize(new Dimension(190,400));
        //otherPanel.add();
        this.add(myPanel,BorderLayout.CENTER);
        this.add(otherPanel,BorderLayout.EAST);
        
        setTitle("Tetris");
        setSize(465, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
    }

    public void setBoard(Shape.Tetrominoe[] board, int index) {
        otherBoard[index].setBoard(board);
    }

    public void setNextBlocks(Shape.Tetrominoe block1, Shape.Tetrominoe block2, Shape.Tetrominoe block3) {
        Tetrominoe[] b = new Tetrominoe[5 * 22];
        Shape shape1 = new Shape();
        Shape shape2 = new Shape();
        Shape shape3 = new Shape();
        shape1.setShape(block1);
        shape2.setShape(block2);
        shape3.setShape(block3);

        for(int i=0;i<b.length;i++) {
            b[i] = Shape.Tetrominoe.NoShape;
        }

        for (int i = 0; i < 4; i++) {

            int x1 = 2 + shape1.x(i);
            int y1 = 15 - shape1.y(i);
            int x2 = 2 + shape2.x(i);
            int y2 = 10 - shape2.y(i);
            int x3 = 2 + shape3.x(i);
            int y3 = 5 - shape3.y(i);

            b[(y1 * 5) + x1] = shape1.getShape();
            b[(y2 * 5) + x2] = shape2.getShape();
            b[(y3 * 5) + x3] = shape3.getShape();
        }

        nextBlockBoard.setBoard(b);
    }

    public Shape.Tetrominoe[] getBoard() {
        return board.getBoard();
    }
    public boolean isOver() {
    	return board.gameOver;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Shape.Tetrominoe[] list = new Shape.Tetrominoe[1000];
            var r = new Random();
            for (int i = 0; i < 1000; i++) {
                int x = Math.abs(r.nextInt()) % 7 + 1;
                Shape.Tetrominoe[] values = Shape.Tetrominoe.values();
                list[i] = values[x];
            }
            var game = new Tetris(list);
            game.setVisible(true);

        });
    }
    
    //실행되면 일정 시간 뒤에 테트리스 게임 끄는 함수
    public void closeGame(int delay) {
    	
    	Timer timer = new Timer(delay, e -> {
    		this.dispose();
        });
        timer.setRepeats(false); // 타이머 반복 실행을 비활성화합니다.
        timer.start();
    }
    
    public void showDialog(boolean win) {
    	board.timer.stop();
    	if(win)
    		JOptionPane.showMessageDialog(this, "You Win!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
    	else
    		JOptionPane.showMessageDialog(this, "You lose..", "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    public void appendLine(int attack) {
        board.appendLine(attack);
    }
}
