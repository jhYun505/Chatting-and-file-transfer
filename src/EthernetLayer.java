
import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

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

	private class _ETHERNET_HEADER {
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;

		public _ETHERNET_HEADER() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	_ETHERNET_HEADER m_sHeader = new _ETHERNET_HEADER();

	public EthernetLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 5; i++) {
			m_sHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
		m_sHeader.enet_type[0] = (byte) 0x00;
		m_sHeader.enet_type[1] = (byte) 0x00;
		m_sHeader.enet_data = null;
	}

	public _ETHERNET_ADDR GetEnetDstAddress() {
		return m_sHeader.enet_dstaddr;
	}

	public _ETHERNET_ADDR GetEnetSrcAddress() {
		return m_sHeader.enet_srcaddr;
	}

	public void SetEnetType(byte[] input) {
		for (int i = 0; i < 2; i++) {
			m_sHeader.enet_type[i] = input[i];
		}
	}

	public void SetEnetDstAddress(byte[] input) {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = input[i];
		}
	}

	public void SetEnetSrcAddress(byte[] input) {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_srcaddr.addr[i] = input[i];
		}
	}

	public byte[] ObjToByte(_ETHERNET_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 14];

		buf[0] = Header.enet_dstaddr.addr[0];
		buf[1] = Header.enet_dstaddr.addr[1];
		buf[2] = Header.enet_dstaddr.addr[2];
		buf[3] = Header.enet_dstaddr.addr[3];
		buf[4] = Header.enet_dstaddr.addr[4];
		buf[5] = Header.enet_dstaddr.addr[5];

		buf[6] = Header.enet_srcaddr.addr[0];
		buf[7] = Header.enet_srcaddr.addr[1];
		buf[8] = Header.enet_srcaddr.addr[2];
		buf[9] = Header.enet_srcaddr.addr[3];
		buf[10] = Header.enet_srcaddr.addr[4];
		buf[11] = Header.enet_srcaddr.addr[5];

		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];

		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf;
	}
	
	/////////////////////////////////////////////
	//	To-Do								   //
	//	- file Send 함수 작성하기				   //
	//	- Send 함수 type 설정					   //
	//	- 채팅의 경우 0x2080, 파일의 경우 0x2090	   //
	/////////////////////////////////////////////
	public boolean fileSend(byte[] input, int length){
		//byte[] type = {0x20,0x90}; // 20 90에 맞추어서 적었습니다.
		byte[] type = {(byte)0x20,(byte)0x90};
		this.SetEnetType(type);
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 14);

		return false;
	}

	public boolean Send(byte[] input, int length) {
		//byte[] type = {0x02,0x08}; // 20 80에 맞추어서 적었습니다.
		byte[] type = {(byte)0x20,(byte)0x80};
		this.SetEnetType(type);
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 14);

		return false;
	}

	public byte[] RemoveEtherHeader(byte[] input, int length) {
		byte[] data = new byte[length - 14];
		for (int i = 0; i < length - 14; i++)
			data[i] = input[14 + i];
		return data;
	}

	public boolean IsItMyPacket(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (m_sHeader.enet_srcaddr.addr[i] == input[6 + i])
				continue;
			else
				return false;
		}
		return true;
	}

	public boolean IsItMine(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (m_sHeader.enet_srcaddr.addr[i] == input[i])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	public boolean IsItBroadcast(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (input[i] == -1) {
				continue;
			} else
				return false;
		}
		return true;
	}

	//////////////////////////////////////////////////
	//	To-Do List									//
	//	Receive의 경우 받은 파일을 어느 레이어로 올려보낼지		//
	//	1)채팅의 경우(0x2080) : ChatAppLayer로 보낸다		//
	//	2)파일의 경우(0x2090) : FileAppLayer로 보낸다		//
	//////////////////////////////////////////////////
	public boolean Receive(byte[] input) {
		byte[] data;
		boolean MyPacket, Mine, Broadcast;
		MyPacket = IsItMyPacket(input);
	
		if(MyPacket == true) {
			return false;
		} else {
			Broadcast = IsItBroadcast(input);
			if(Broadcast == false) {		
				Mine = IsItMine(input);
				if(Mine == false) {			
					return false;		
				}
			}
		}
		data = RemoveEtherHeader(input, input.length);
		//this.GetUpperLayer(0).Receive(data);
		
		//buf[12] = Header.enet_type[0]; 0x20
		//buf[13] = Header.enet_type[1]; 0x80
		if(input[12] == (byte) 0x20){// Type 첫번쨰가 0x20일시.
		if(input[13] == (byte)0x90)this.GetUpperLayer(1).Receive(data); // FileAppLayer로 전송
		else if (input[13] ==(byte) 0x80) this.GetUpperLayer(0).Receive(data); //  Chatapplayer로 전송
		}else return false; // 20이 아닐시 false 반환.
		
		return true;
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
