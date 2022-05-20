import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	_ETHERNET_Frame m_sHeader;
	
	public EthernetLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}
	
	public void ResetHeader() {
		m_sHeader = new _ETHERNET_Frame();
	}
	
    private class _ETHERNET_ADDR {
        private byte[] addr = new byte[6];

        public _ETHERNET_ADDR() {
            this.addr[0] = (byte) 0x00;
            this.addr[1] = (byte) 0x00;
            this.addr[2] = (byte) 0x00;
            this.addr[3] = (byte) 0x00;
            this.addr[4] = (byte) 0x00;
            this.addr[5] = (byte) 0x00;

        }
    }
    
    private class _ETHERNET_Frame {
        _ETHERNET_ADDR enet_dstaddr;
        _ETHERNET_ADDR enet_srcaddr;
        byte[] enet_type;
        byte[] enet_data;

        public _ETHERNET_Frame() {
            this.enet_dstaddr = new _ETHERNET_ADDR();
            this.enet_srcaddr = new _ETHERNET_ADDR();
            this.enet_type = new byte[2];
            this.enet_data = null;
        }
    }
    
    public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) {//data에 헤더 붙여주기
		byte[] buf = new byte[length + 14];
		for(int i = 0; i < 6; i++) {
			buf[i] = Header.enet_dstaddr.addr[i];
			buf[i+6] = Header.enet_srcaddr.addr[i];
		}			
		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];
		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf;
	}

	// 파일 보내는 메서드
	public boolean fileSend(byte[] input, int length) {
		if (input == null && length == 0) // ack를 위한 데이터를 받은 경우
			m_sHeader.enet_type = intToByte2(0x2091);
		else if (isBroadcast(m_sHeader.enet_dstaddr.addr)) // broadcast의 경우
			m_sHeader.enet_type = intToByte2(0xff);
		else // normal 파일인 경우
			m_sHeader.enet_type = intToByte2(0x2090);

		//헤더를 붙여 하위 계층으로 보내는 과정 작성
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, bytes.length);
		return true;
	}
	
	// 채팅을 보내는 메서드
	public boolean chatSend(byte[] input, int length) {
		if (input == null && length == 0) // ack를 위한 데이터를 받은 경우
			m_sHeader.enet_type = intToByte2(0x2081);
		else if (isBroadcast(m_sHeader.enet_dstaddr.addr)) // broadcast의 경우
			m_sHeader.enet_type = intToByte2(0xff);
		else // normal 데이터인 경우
			m_sHeader.enet_type = intToByte2(0x2080);
		
		// 헤더를 붙여 하위계층으로 보내는 과정 작성
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, bytes.length);
		return true;
	}
	
	public boolean Send(byte[] input, int length) {
		chatSend(input, length);
		return true;
	}

	public byte[] RemoveEthernetHeader(byte[] input, int length) {
		byte[] cpyInput = new byte[length - 14];
		System.arraycopy(input, 14, cpyInput, 0, length - 14);
		input = cpyInput;
		return input;
	}
	
	public synchronized boolean Receive(byte[] input) {
		byte[] data;
		byte[] temp_src = m_sHeader.enet_srcaddr.addr;
		int temp_type = byte2ToInt(input[12], input[13]); 
		
		/* ( ~ *Ethernet ( *ChatApp --- 0번 ( *GUI ) *FileApp  --- 1번 ( *GUI )) */
		ChatAppLayer chatAppLayer = (ChatAppLayer) this.GetUpperLayer(0);
        FileAppLayer fileAppLayer = (FileAppLayer) this.GetUpperLayer(1);
        
		//////////////////////////////////////////////////
		//	header에 들어있는 type에 따라 판단한다.				//
		//												//
		//  ACK - 각각의 상위 레이어로 전달						//
		//   1) 0x209? - FileAppLayer로 전달하는 ACK		//
		//	 2) 0x208? - ChatAppLayer로 전달하는 ACK		//
		//	정상적인 데이터 전송 - 헤더를 제거하고 상위 레이어로 전달		//
		// 	 1) 0x2090 - FileAppLayer로 전달				//
		//   2) 0x2080 - ChatAppLayer로 전달				//
		//	각각에 대해 내가 보낸건지, BroadCast인지, 나에게 온건지	//
		//////////////////////////////////////////////////
		
		
        if(!isMyPacket(input) && (isBroadcast(input) || chkAddr(input))) {
			if (temp_type == 0x2081) {
				chatAppLayer.Receive(null);	// chatApp으로 ACK 전달
				/* ACK는 내용이 null */
			}
			else if(temp_type == 0x2091) {
				fileAppLayer.Receive(null);	// fileApp으로 ACK 전달
				/* ACK는 내용이 null */
			}
			else if (temp_type == 0x2080) {
				// Chatting message이므로 ChatApp으로 헤더 제거하여 전달
				data = RemoveEthernetHeader(input, input.length);
				chatAppLayer.Receive(data);	// ChatApp으로 헤더 제거한 데이터 전달
				return true;
			}
			else if (temp_type == 0x2090) {
				// File 전송이므로 ChatApp으로 헤더 제거하여 전달
				data = RemoveEthernetHeader(input, input.length);
				fileAppLayer.Receive(data);	// FileApp으로 헤더 제거한 데이터 전달
				return true;
			}
		}
		return false;
	}

    private byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[0] |= (byte) ((value & 0xFF00) >> 8);
        temp[1] |= (byte) (value & 0xFF);

        return temp;
    }

    private int byte2ToInt(byte value1, byte value2) {
        return (int)(((value1 & 0xff) << 8) | (value2 & 0xff));
    }
	
    private boolean isBroadcast(byte[] bytes) {
		for(int i = 0; i< 6; i++)
			if (bytes[i] != (byte) 0xff)
				return false;
		return (bytes[12] == (byte) 0xff && bytes[13] == (byte) 0xff);
	}

	private boolean isMyPacket(byte[] input){
		for(int i = 0; i < 6; i++)
			if(m_sHeader.enet_srcaddr.addr[i] != input[6 + i])
				return false;
		return true;
	}

	private boolean chkAddr(byte[] input) {
		byte[] temp = m_sHeader.enet_srcaddr.addr;
		for(int i = 0; i< 6; i++)
			if(m_sHeader.enet_srcaddr.addr[i] != input[i])
				return false;
		return true;
	}
	
	public void SetEnetSrcAddress(byte[] srcAddress) {
		// TODO Auto-generated method stub
		m_sHeader.enet_srcaddr.addr = srcAddress;
	}

	public void SetEnetDstAddress(byte[] dstAddress) {
		// TODO Auto-generated method stub
		m_sHeader.enet_dstaddr.addr = dstAddress;
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
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}
}