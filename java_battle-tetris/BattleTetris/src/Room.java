//1971261 KimJeongSeok
import java.awt.BorderLayout;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import java.util.Scanner;

import javax.swing.*;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import Tetris.Tetris;
import Tetris.Shape.Tetrominoe;
import Tetris.Shape;
import Tetris.Board;

public class Room extends JFrame{

	private Thread receiveThread=null;
	private JTextPane t_display;
	private DefaultStyledDocument document = new DefaultStyledDocument();
	private JScrollPane scroll;
	private JButton b_connect;
	private JButton b_disconnect;
	private JTextField t_input;
	private JButton b_send;
	private JButton b_ready;
	private String serverAddress;
	private int serverPort;
	private Socket socket;
	private JLabel id;
	private JLabel account;
	private JLabel port;
	private JTextField t_id;
	private JTextField t_account;
	private JTextField t_port;
	private String uid;
	private ObjectOutputStream out;
	private JButton b_select;
	private BufferedOutputStream bos;
	private Random random=new Random();
	private boolean isHost = false;
	List<String> players = new ArrayList<String>();
	private List<Boolean> pReady = new ArrayList<Boolean>();
	private static Tetris game;
	Timer timer;
	
	private void printDisplay(String a) {
//		t_display.append(a + "\n");
//		t_display.setCaretPosition(t_display.getDocument().getLength());
		int len=t_display.getDocument().getLength();
		
		try {
			document.insertString(len, a+"\n", null);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t_display.setCaretPosition(len);
	}
	private void printDisplay(ImageIcon icon) {
		int len=t_display.getDocument().getLength();
		t_display.setCaretPosition(len);
		
		if(icon.getIconWidth()>400) {
			Image img = icon.getImage();
			Image changeImg = img.getScaledInstance(400, -1, Image.SCALE_SMOOTH);
			icon = new ImageIcon(changeImg);
		}
		
		t_display.insertIcon(icon);
		printDisplay("");
		t_input.setText("");
	}
	

	public Room(String serverAddress2, int serverPort2) {
		// TODO Auto-generated constructor stub
		super("Battle Tetris");  //프레임의 타이틀 지정
        setSize(600,340);        //컨테이너 크기 지정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0,2));
		buildGUI();
		
		this.serverAddress = serverAddress2;
		this.serverPort = serverPort2;
		
		t_account.setText("localhost");
		t_port.setText("54321");
		
		setVisible(true);        //창을 보이게함
		//connectToServer();

	}



	private void buildGUI() {
		JPanel p_chat = new JPanel(new BorderLayout());
		
		p_chat.add(createDisplayPanel(),BorderLayout.CENTER);
		p_chat.add(createInputPanel(),BorderLayout.SOUTH);

		JPanel p_pink = new JPanel(new BorderLayout());
		JPanel p_player = new JPanel(new GridLayout(5,0));
		
		createPlayerPanel(p_player);
		
		
		JPanel p_login = new JPanel(new GridLayout(2,0));
		p_login.add(createInfoPanel());
		p_login.add(createControlPanel());
		p_pink.add(p_player,BorderLayout.CENTER);
		p_pink.add(p_login, BorderLayout.SOUTH);

		add(p_pink);
		add(p_chat);

	}
	private JTextField[] player1 = new JTextField[5];
	private JLabel plabel[] = new JLabel[5];
	private void createPlayerPanel(JPanel player) {
		for(int i=0;i<5;i++) {
			JPanel p = new JPanel(new BorderLayout());
			player1[i] = new JTextField(7);
			player1[i].setHorizontalAlignment(SwingConstants.CENTER);
			player1[i].setEditable(false);
			plabel[i] = new JLabel("  X  ");
			p.add(player1[i],BorderLayout.CENTER);
			p.add(plabel[i],BorderLayout.EAST);
			player.add(p);
		}
		
	}
	private JPanel createInfoPanel() {
		
		JPanel p = new JPanel(new GridLayout(0,2));
		JPanel tp = new JPanel(new GridLayout(0,2));
		id=new JLabel("  아이디 :");
		tp.add(id);
		t_id = new JTextField(7);
		tp.add(t_id);
		p.add(tp);
		
		b_connect = new JButton("접속하기");
		b_connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					if(b_connect.getText()=="접속 종료") {
						disconnect();
					}
					else {
						connectToServer();
					}
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					printDisplay("서버주소와 포트 번호를 확인하세요.");
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					printDisplay("서버를 찾을 수 없었습니다.");
					e1.printStackTrace();
				}
			}
		});
		p.add(b_connect);
		
		account = new JLabel("  IP주소 :");
		//p.add(account);
		t_account = new JTextField(13);
		//p.add(t_account);
		port = new JLabel("  포트 :");
		//p.add(port);
		t_port = new JTextField(5);
		//p.add(t_port);

		return p;
	}

	private JPanel createDisplayPanel() {

		JPanel p = new JPanel(new BorderLayout());
		t_display = new JTextPane(document);
		
		//p.add(t_display,BorderLayout.CENTER);
		scroll = new JScrollPane(t_display);
		scroll.setBounds(getBounds());
		p.add(scroll,BorderLayout.CENTER);

		t_display.setEnabled(true);

		t_display.setEditable(false);

		return p;

	}

	

	private JPanel createInputPanel() {
		JPanel p = new JPanel(new BorderLayout());
		t_input = new JTextField(30);
		t_input.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});
		b_send = new JButton("준비");
		b_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});
		
		p.add(t_input,BorderLayout.CENTER);
		t_input.setEnabled(false);
		b_send.setEnabled(false);
		//b_select.setEnabled(false);
		return p;

	}

	private JPanel createControlPanel() {

		JPanel p = new JPanel(new BorderLayout());
		b_ready = new JButton("준비 완료");


		b_ready.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendReady();
			}
		});
		p.add(b_ready);

		return p;

	}

	
	

	private void connectToServer() throws UnknownHostException, IOException{
			
		//Socket socket = null;

		try {
			serverAddress = t_account.getText();
            serverPort = Integer.parseInt(t_port.getText());

			socket = new Socket();
			SocketAddress sa = new InetSocketAddress(serverAddress,serverPort);
			socket.connect(sa,3000);
			//socket.setSoTimeout(3000);
			bos = new BufferedOutputStream(socket.getOutputStream());
			out = new ObjectOutputStream(bos);


			sendUserID();
			
			receiveThread = new Thread(new Runnable() {
				private ObjectInputStream in = null;
				private BufferedInputStream bis;
				private void receiveMessages() {
					try {

						ChatMsg message = (ChatMsg) in.readObject();
						if(message==null) {
							disconnect();
							printDisplay("서버 연결 끊김");
							return;
						}
						
						switch(message.mode) {
						case ChatMsg.MODE_LIST:
							players = new ArrayList<>(message.players);
							players.remove(players.indexOf(uid));
						case ChatMsg.MODE_READY:
							for(int i=0;i<5;i++) {
								player1[i].setText("");
								plabel[i].setText("  X  ");
							}
							printDisplay(message.message);
							for(int i=0;i<message.players.size();i++) {
								player1[i].setText(message.players.get(i));
								if(message.playerReady.get(i))
									plabel[i].setText("  O  ");
								else
									plabel[i].setText("  X  ");
								
							}
							plabel[0].setText("  H  ");
							if(!message.players.isEmpty() && message.players.get(0).equals(uid)) {
								isHost=true;
								b_ready.setText("게임 시작");
							}
							else {
								isHost=false;
								b_ready.setText("준비 완료");
							}
							pReady = message.playerReady;
							break;
						case ChatMsg.MODE_START:
							startTetrisGame(message.list);
							break;
						case ChatMsg.MODE_TX_STRING:
							printDisplay(message.userID+": "+message.message);
							break;
						case ChatMsg.MODE_BOARD_DATA:
							game.setBoard(message.board, players.indexOf(message.userID));
							break;
						case ChatMsg.MODE_GAME_OVER:
							timer.stop();
							game.showDialog(true);
							break;
						case ChatMsg.MODE_BLOCK_ATTACK:
							game.appendLine(message.attack);
							break;
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//System.out.println("서버 읽기 오류> "+e.getMessage());
						disconnect();
						//System.exit(-1);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				@Override
				public void run() {
					try {
						bis=new BufferedInputStream(
								socket.getInputStream());
						in= new ObjectInputStream(bis);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						printDisplay("입력 스트림이 없음");
					}
					// TODO Auto-generated method stub
					while(receiveThread == Thread.currentThread()) {
						receiveMessages();
					}
					
				}
				
			});
			receiveThread.start();
			
			//b_disconnect.setEnabled(true);

			b_connect.setText("접속 종료");

			t_input.setEnabled(true);

			b_send.setEnabled(true);
			//b_select.setEnabled(true);
		} catch (UnknownHostException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		} catch (IOException e) {

			// TODO Auto-generated catch block
			printDisplay("서버를 찾을 수 없었습니다.");
			e.printStackTrace();
		} 

	}

	private void disconnect() {
		send(new ChatMsg(uid, ChatMsg.MODE_LOGOUT));
		try {
			receiveThread = null;
			//b_disconnect.setEnabled(false);
			b_connect.setText("접속하기");
			t_input.setEnabled(false);
			b_send.setEnabled(false);
			//b_select.setEnabled(false);
			for(int i=0;i<5;i++) {
				player1[i].setText("");
				plabel[i].setText("  X  ");
			}
			socket.close();
		} catch (IOException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}

		

	}
	private void send(ChatMsg msg) {
		try {
			out.writeObject(msg);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void sendMessage() {
		if(t_input.getText()=="") return;
		String message=null;
		message = t_input.getText();
		send(new ChatMsg(uid, ChatMsg.MODE_TX_STRING,message));
		printDisplay("메시지 보냄 : "+message);
		t_input.setText("");

	}
	
	private void sendReady() {
		if(isHost) {
			boolean allready=true;
			//host(1P) 제외 전부 준비 완료라면
			for(int i=1;i<pReady.size();i++) {
				if(pReady.get(i)==false)
					allready=false;
			}
			if(allready && pReady.size()>=2) {
				printDisplay("모두 준비 완료. 게임을 시작합니다");
				sendStart();
				
			}
			else {
				printDisplay("준비하지 않은 사람이 있거나, 인원이 부족합니다");
				return;
			}
		}
		else {
			send(new ChatMsg(uid, ChatMsg.MODE_READY,""));
		}

	}
	private void sendStart() {
		send(new ChatMsg(uid,ChatMsg.MODE_START));
	}
	private void sendUserID(){
		uid=t_id.getText();
		send(new ChatMsg(uid, ChatMsg.MODE_LOGIN));
	}

	private void sendBoard(Shape.Tetrominoe[] board) {
		ChatMsg msg = new ChatMsg(uid, ChatMsg.MODE_BOARD_DATA);
		msg.board = new Shape.Tetrominoe[220];
		for (int i=0;i<board.length;i++) {
			msg.board[i] = board[i];
		}
		
		send(msg);
	}

	public static void main(String[] args) {

		// TODO Auto-generated method mstub

		String serverAddress = "localhost";

		int serverPort = 54321;
		
		
		new Room(serverAddress, serverPort);
		
	}

	
	public void startTetrisGame(Tetrominoe[] list) {
        EventQueue.invokeLater(() -> {
            game = new Tetris(list);
            int x = getX();
            int y = getY();
            game.setLocation(x, y);
            game.setVisible(true);

			timer = new Timer(Board.PERIOD_INTERVAL, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sendBoard(game.getBoard());
					if (game.attack > 0) {
						ChatMsg msg = new ChatMsg(uid, ChatMsg.MODE_BLOCK_ATTACK);
						msg.attack = game.attack;
						send(msg);
						game.attack = 0;
					}
					if(game.isOver()) {
						send(new ChatMsg(uid,ChatMsg.MODE_PLAYER_OUT));
						game.showDialog(false);
						timer.stop();
					}
				}
			});
			timer.start();
        });
    }
}