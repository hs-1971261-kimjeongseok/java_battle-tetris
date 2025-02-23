//1971261 KimJeongSeok

import Tetris.Shape;

import java.awt.BorderLayout;

import java.awt.Container;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JTextArea;

import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;


public class TetrisServer extends JFrame {
	private int port; 
    private ServerSocket serverSocket; 
    private JTextPane t_display;
	private DefaultStyledDocument document = new DefaultStyledDocument();
	private JScrollPane scroll;
    private Thread acceptThread = null; 
    private Vector<ClientHandler> users = new Vector<>();  // ClientHandler를 저장할 벡터
	private JButton b_connect;
	private JButton b_disconnect;
	private JButton b_exit;
	BufferedWriter out;
	private int clients = 0;
	
	private List<String> Players = new ArrayList<String>();
	private List<Boolean> PlayerReady = new ArrayList<Boolean>();
	private List<Shape.Tetrominoe> blocks = new ArrayList<Shape.Tetrominoe>();
	
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
	}
	private void buildGUI() {

		add(createDisplayPanel(),BorderLayout.CENTER);

		JPanel p_pink = new JPanel(new GridLayout(1,0));

		//p_pink.add(createInputPanel());

		p_pink.add(createControlPanel());

		add(p_pink, BorderLayout.SOUTH);

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
	
	private JPanel createControlPanel() {

		JPanel p = new JPanel(new GridLayout(1,0));
		b_connect = new JButton("서버시작");
		b_disconnect = new JButton("서버종료");
		b_exit = new JButton("종료");

		
		b_connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//startServer();
				
				acceptThread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						startServer();
					}
					
				});
				acceptThread.start();
				
				//printDisplay("서버가 시작되었습니다.");
				b_connect.setEnabled(false);
				b_disconnect.setEnabled(true);
				
				//b_exit.setEnabled(false);
			}
		});

		b_disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();
				b_connect.setEnabled(true);
				b_disconnect.setEnabled(false);
				b_exit.setEnabled(true);
			}
		});

		b_exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		

		p.add(b_connect);
		p.add(b_disconnect);
		p.add(b_exit);

		b_disconnect.setEnabled(false);
		
		return p;
	}
	private void disconnect() {
		try {
			serverSocket.close();
			printDisplay("서버 닫음");
			acceptThread = null;
			users.clear();
		} catch (IOException e) {
			System.err.println("서버소켓 닫기 오류 > "+e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private class ClientHandler extends Thread {
		private Socket clientSocket;
		private BufferedOutputStream bos;
		public BufferedInputStream bis;
		private String uid;
	    private ObjectOutputStream out;
		public ClientHandler(Socket clientSocket) {
			this.clientSocket=clientSocket;
		}
		private void receiveMessages(Socket cs) {
			byte[] receive = new byte[2048];
			int len;
			try {
				String message;
				bis=new BufferedInputStream(clientSocket.getInputStream());
				ObjectInputStream in = new ObjectInputStream(bis);
				bos=new BufferedOutputStream(clientSocket.getOutputStream());
	            out=new ObjectOutputStream(bos);
	            
	            
				ChatMsg msg;
				while ((msg =(ChatMsg)in.readObject())!=null){
					if(msg.mode==ChatMsg.MODE_LOGIN) {
						uid=msg.userID;
						msg.message = "새 참가자: "+uid;
						synchronized(this) {
						    Players.add(uid);
						    PlayerReady.add(false);
						    ChatMsg msg1 = new ChatMsg(uid, ChatMsg.MODE_LIST, "새 참가자: "+uid);
						    updatePlayers(msg1);
						}
				
						printDisplay(msg.message);
						printDisplay("참가자 수 : "+(++clients));
						continue;
					}
					else if(msg.mode==ChatMsg.MODE_LOGOUT) break;
					else if(msg.mode==ChatMsg.MODE_READY) {
						int index = Players.indexOf(msg.userID);
						if(index != -1) {
						    PlayerReady.set(index, !PlayerReady.get(index));
						}
						synchronized(this) {
							ChatMsg msg1 = new ChatMsg(uid, ChatMsg.MODE_READY, "준비 상태 변경: "+uid);
							printDisplay(msg1.message);
							updatePlayers(msg1);
						}
					}
					else if(msg.mode==ChatMsg.MODE_START) {
						printDisplay("모두 준비 완료. 게임을 시작합니다");
						Shape.Tetrominoe[] list = new Shape.Tetrominoe[1000];
						var r = new Random();
						for (int i = 0; i < 1000; i++) {
							int x = Math.abs(r.nextInt()) % 7 + 1;
							Shape.Tetrominoe[] values = Shape.Tetrominoe.values();
							list[i] = values[x];
						}
						msg.list = list;
						broadcasting(msg);
					}
					else if(msg.mode==ChatMsg.MODE_TX_STRING) {
						message=uid+": "+msg.message;
						printDisplay(message);
						broadcasting(msg);
					}
					else if(msg.mode==ChatMsg.MODE_BOARD_DATA) {
						printDisplay(msg.userID + "board");
						broadcastOthers(msg);
					}
					else if(msg.mode==ChatMsg.MODE_PLAYER_OUT) {
						printDisplay(msg.userID + " Out");
						int index = Players.indexOf(msg.userID);
						if(index != -1) {
							Players.remove(index);
						}
						if(Players.size()==1) {
							msg.mode=ChatMsg.MODE_GAME_OVER;
							broadcastTo(msg,Players.get(0));
						}
					}
					else if (msg.mode== ChatMsg.MODE_BLOCK_ATTACK) {
						printDisplay(msg.userID + " Attack " + msg.attack);
						broadcastOthers(msg);
					}

				}
				int index = Players.indexOf(msg.userID);
				if(index != -1) {
					Players.remove(index);
				    PlayerReady.remove(index);
				}
				synchronized(this) {
					ChatMsg msg1 = new ChatMsg(uid, ChatMsg.MODE_LIST, "로그아웃: "+uid);
					updatePlayers(msg1);
				}
				users.removeElement(this);
				printDisplay("클라이언트 " +uid+" 가 연결을 종료했습니다.");
				printDisplay("참가자 수 : "+(--clients));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				users.removeElement(this);
				printDisplay(uid+" 연결 끊김> "+e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					cs.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					printDisplay("닫기 오류> "+e.getMessage());
					System.exit(-1);
				}
			}
		}
		private void updatePlayers(ChatMsg message) {
			
				message.players = new ArrayList<>(Players);
				message.playerReady = new ArrayList<>(PlayerReady);
				broadcasting(message);
		}
		private void sendMessage(String msg){
            send(new ChatMsg(uid,ChatMsg.MODE_TX_STRING,msg));
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
        void broadcasting(ChatMsg message){
            for(ClientHandler user : users)
            	user.send(message);
        }

		void broadcastOthers(ChatMsg message) {
			synchronized(ClientHandler.class) {
				for (ClientHandler user : users) {
					if (message.userID != user.uid)
						user.send(message);
				}
			}
		}
		void broadcastTo(ChatMsg message, String uid){
            for(ClientHandler user : users)
            	if(uid==user.uid)
            		user.send(message);
        }
		@Override
		public void run() {
			receiveMessages(clientSocket);
		}
	}
	private void startServer() {
		Socket clientSocket = null;
		this.serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			printDisplay("서버가 시작되었습니다. 로컬 주소: " + serverSocket.getInetAddress().getLocalHost());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ClientHandler clientHandler;
		while (acceptThread==Thread.currentThread()) {
			try {
				clientSocket = serverSocket.accept();
				printDisplay("클라이언트가 연결되었습니다.");
				clientHandler = new ClientHandler(clientSocket);
				users.add(clientHandler);
				clientHandler.start();
				
			} catch (SocketException e) {
				printDisplay("서버 소켓 종료");
			}
			catch (IOException e) {
	            e.printStackTrace();
	            
			}
			
		}
	}
	
	
	
	
	public TetrisServer(int serverPort) {
		// TODO Auto-generated constructor stub
		super("BattleTetrisServer");  //프레임의 타이틀 지정
        setSize(400,300);        //컨테이너 크기 지정

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		buildGUI();
		this.port = serverPort;
		setVisible(true);        //창을 보이게함

		//connectToServer();

	}

	public static void main(String[] args) {

		int port = 54321;

		TetrisServer server = new TetrisServer(port);
		//server.startServer();

	}
}
