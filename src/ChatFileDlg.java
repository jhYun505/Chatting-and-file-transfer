import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;





import org.jnetpcap.PcapIf;

public class ChatFileDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextField ChattingWrite;

	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcMacAddress;
	JTextArea dstMacAddress;

	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton Chat_send_Button;

	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;
	
	String Text;
	
	////////////////////////////////////
	//	To-Do : 파일 전송 패널 관련된 요소 선언	  //
	////////////////////////////////////
	File file;
	JButton File_Send_Button;
	JButton File_Upload_Button;
	private JTextField file_upload_text;
	JProgressBar progressBar;

	public static void main(String[] args) {
		
		//////////////////////////////////////////
		//	TO-DO : 레이어 생성하고 연결 시키기				//
		//	- 고민해야 할 부분 : ChatAppLayer와			//
		//				 FileAppLayer 어떻게 연결?	//
		//////////////////////////////////////////

		m_LayerMgr.AddLayer(new NILayer("NI"));	//NILayer 
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));		//EthernetLayer
		m_LayerMgr.AddLayer(new ChatAppLayer("ChatApp"));		//ChatAppLayer
		m_LayerMgr.AddLayer(new FileAppLayer("FileApp"));	//FileAppLayer
		m_LayerMgr.AddLayer(new ChatFileDlg("GUI"));		//GUI
		
		/* 각각의 Layer를 연결한다. */
		m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ChatApp ( *GUI ) *FileApp ( *GUI ) ))");
		
	}

	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			/* 주소 설정 버튼이 눌렸을 때 */
			if (e.getSource() == Setting_Button) {
				
				/* Reset 버튼이 눌린거라면 */
				if(Setting_Button.getText() == "Reset") {
					srcMacAddress.setText("");
					dstMacAddress.setText("");
					Setting_Button.setText("Setting");
					srcMacAddress.setEnabled(true);
					dstMacAddress.setEnabled(true);
					
				}
				/* 주소 설정 과정 */
				else if(Setting_Button.getText() == "Setting"){
					byte[] srcAddress = new byte[6];	
					byte[] dstAddress = new byte[6];	
					
					String src = srcMacAddress.getText();
					String dst = dstMacAddress.getText();
					
					String[] byte_src = src.split("-");		
					for(int i = 0; i < byte_src.length ; i++) {
						srcAddress[i] = (byte) Integer.parseInt(byte_src[i], 16); 	
					}
					String[] byte_dst = dst.split("-");
					for(int i = 0; i < byte_dst.length ; i++) {
						dstAddress[i] = (byte) Integer.parseInt(byte_dst[i], 16);
					}
					
					((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(srcAddress);
					((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetDstAddress(dstAddress);
					
					((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(adapterNumber);
					
					
					
					/* GUI Setting Change */
					Setting_Button.setText("Reset");
					dstMacAddress.setEnabled(false);
					srcMacAddress.setEnabled(false);
				}
				
			}
			
			/* 채팅 전송 버튼이 눌렸을 때 */
			if (e.getSource() == Chat_send_Button) {
				/* 주소가 설정(고정)되어있는 상태*/
				if (Setting_Button.getText() == "Reset") {
				//	for(int i = 0 ; i < 10; i++) {
						String input = ChattingWrite.getText();
						ChattingArea.append("[SEND] : " + input + "\n");
						 
						byte[] bytes = input.getBytes();
						((ChatAppLayer)m_LayerMgr.GetLayer("ChatApp")).Send(bytes, bytes.length);
						
						ChattingWrite.setText(""); 
						
				} else {
					JOptionPane.showMessageDialog(null, "주소 설정 오류");
				}
			}
			
			/* 파일 업로드 버튼을 눌렀을 때 */
			if (e.getSource() == File_Upload_Button) {
				/*파일 선택을 위해 javax.swing의 JFileChooser를 import한다. */
				JFileChooser select_file = new JFileChooser();
				int result = select_file.showOpenDialog(null);
				if(result == JFileChooser.APPROVE_OPTION) {
					file = select_file.getSelectedFile();
					file_upload_text.setText(file.getPath());
					file_upload_text.setEnabled(false);
					File_Send_Button.setEnabled(true);
					progressBar.setValue(0);
				}
				
			}
			
			
			/* 파일 전송 버튼을 눌렀을 때 : */
			if (e.getSource() == File_Send_Button) {
				/* 주소가 설정(고정)되어있는 상태*/
				if(Setting_Button.getText() == "Reset") {
					((FileAppLayer) m_LayerMgr.GetLayer("FileApp")).setAndStartSendFile();
				} else {
					JOptionPane.showMessageDialog(null, "주소 설정 오류");
				}
			}
			
			
		}
	}

	/* Dialog 생성자 - GUI 나타내야 한다. */
	public ChatFileDlg(String pName) {
		pLayerName = pName;

		setTitle("Packet_Send_Test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "채팅",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ChattingArea = new JTextArea();
		ChattingArea.setEditable(false);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area
		

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(380, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);

		lblsrc = new JLabel("Source Mac Address");
		lblsrc.setBounds(10, 75, 170, 20);
		settingPanel.add(lblsrc);

		srcMacAddress = new JTextArea();
		srcMacAddress.setBounds(2, 2, 170, 20);
		sourceAddressPanel.add(srcMacAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("Destination Mac Address");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstMacAddress = new JTextArea();
		dstMacAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstMacAddress);// dst address

		JLabel NICLabel = new JLabel("NIC 선택");
		NICLabel.setBounds(10, 20, 170, 20);
		settingPanel.add(NICLabel);

		NICComboBox = new JComboBox();
		NICComboBox.setBounds(10, 49, 170, 20);
		settingPanel.add(NICComboBox);

		for (int i = 0; ((NILayer) m_LayerMgr.GetLayer("NI")).getAdapterList().size() > i; i++) {
			PcapIf pcapIf = ((NILayer) m_LayerMgr.GetLayer("NI")).GetAdapterObject(i);
			NICComboBox.addItem(pcapIf.getName());
		}

		NICComboBox.addActionListener(new ActionListener() { // 이벤트리스너

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JComboBox jcombo = (JComboBox) e.getSource();
				adapterNumber = jcombo.getSelectedIndex();
				System.out.println("Index: " + adapterNumber);
				try {
					srcMacAddress.setText("");
					srcMacAddress.append(get_MacAddress(((NILayer) m_LayerMgr.GetLayer("NI"))
							.GetAdapterObject(adapterNumber).getHardwareAddress()));

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		try {// 초기mac주소
			srcMacAddress.append(get_MacAddress(
					((NILayer) m_LayerMgr.GetLayer("NI")).GetAdapterObject(adapterNumber).getHardwareAddress()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		;

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		Chat_send_Button = new JButton("Send");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button

		
		
		/////////////////////////////////////////////
		//	TO-DO : 파일 전송 GUI 구현				   //
		//  1) 파일 전송 박스							   //
		//	2) 파일 주소 및 이름 나오는 부분	: filePathText	   //
		//	3) 파일 업로드 버튼 : File_Upload_Button	   //
		//	4) 파일 전송 버튼	: File_Send_Button		   //
		//	5) ProgressBar	: progressBar		   //
		//	6) 파일 찾는 창 : fileSelectPanel
		/////////////////////////////////////////////
		
		//panel
		JPanel filePanel = new JPanel();
		filePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "파일전송",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		filePanel.setBounds(10, 295, 630, 90);
		contentPane.add(filePanel);
		filePanel.setLayout(null);
		
		//파일 이름 & path
		file_upload_text = new JTextField();
		file_upload_text.setEditable(false);
		file_upload_text.setBounds(2, 2, 480, 20);
		filePanel.add(file_upload_text);
		
		
		//진행 바
		progressBar = new JProgressBar(0, 100);
		progressBar.setBounds(10, 50, 480, 20);
		progressBar.setStringPainted(true);
		filePanel.add(progressBar);
		
		setVisible(true);

	}

	public String get_MacAddress(byte[] byte_MacAddress) {

		String MacAddress = "";
		for (int i = 0; i < 6; i++) {
			MacAddress += String.format("%02X%s", byte_MacAddress[i], (i < MacAddress.length() - 1) ? "" : "");
			if (i != 5) {
				MacAddress += "-";
			}
		}

		System.out.println("현재 선택된 주소" + MacAddress);
		return MacAddress;
	}

	public boolean Receive(byte[] input) {
		if (input != null) {
			byte[] data = input;
			Text = new String(data);
			System.out.println("Recive 발생!");
			ChattingArea.append("[RECV] : " + Text + "\n");
			return false;
		}
		return false;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
}
