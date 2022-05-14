import java.util.ArrayList;
import java.util.StringTokenizer;

public class LayerManager { //계층간 연결을 위한 매니저 클래스
	
	private class _NODE{
		private String token;
		private _NODE next;
		public _NODE(String input){
			this.token = input;
			this.next = null;
		}
	}

	_NODE mp_sListHead;
	_NODE mp_sListTail;
	
	private int m_nTop;
	private int m_nLayerCount;

	private ArrayList<BaseLayer> mp_Stack = new ArrayList<BaseLayer>();
	private ArrayList<BaseLayer> mp_aLayers = new ArrayList<BaseLayer>();
	
	

	public LayerManager(){
		m_nLayerCount = 0;
		mp_sListHead = null;
		mp_sListTail = null;
		m_nTop = -1;
	}
	
	public void AddLayer(BaseLayer pLayer){
		mp_aLayers.add(m_nLayerCount++, pLayer);
		//m_nLayerCount++;
	}
	
	
	public BaseLayer GetLayer(int nindex){
		return mp_aLayers.get(nindex);
	}
	
	public BaseLayer GetLayer(String pName){
		for( int i=0; i < m_nLayerCount; i++){
			if(pName.compareTo(mp_aLayers.get(i).GetLayerName()) == 0)
				return mp_aLayers.get(i);
		}
		return null;
	}
	
	public void ConnectLayers(String pcList){
		MakeList(pcList);
		LinkLayer(mp_sListHead); 
	}

	private void MakeList(String pcList){ 
		StringTokenizer tokens = new StringTokenizer(pcList, " ");
		
		for(; tokens.hasMoreElements();){
			_NODE pNode = AllocNode(tokens.nextToken()); 
			AddNode(pNode);
			
		}	
	}

	private _NODE AllocNode(String pcName){ 
		//각 노드간의 연결
		_NODE node = new _NODE(pcName);		
		return node;				
	}
	
	private void AddNode(_NODE pNode){
		if(mp_sListHead == null){
			mp_sListHead = mp_sListTail = pNode;
		}else{
			mp_sListTail.next = pNode;
			mp_sListTail = pNode;
		}
	}

	private void Push (BaseLayer pLayer){
		mp_Stack.add(++m_nTop, pLayer);
		//mp_Stack.add(pLayer);
		//m_nTop++;
	}

	private BaseLayer Pop(){
		BaseLayer pLayer = mp_Stack.get(m_nTop);
		mp_Stack.remove(m_nTop);
		m_nTop--;
		
		return pLayer;
	}
	
	private BaseLayer Top(){
		return mp_Stack.get(m_nTop);
	}
	
	private void LinkLayer(_NODE pNode){ //계층간의 연결을 위한 함수
		BaseLayer pLayer = null;
		
		while(pNode != null){
			if( pLayer == null)
				pLayer = GetLayer (pNode.token);
			else{
				if(pNode.token.equals("("))
					Push (pLayer);
				else if(pNode.token.equals(")"))
					Pop();
				else{
					char cMode = pNode.token.charAt(0);
					String pcName = pNode.token.substring(1, pNode.token.length());
					
					pLayer = GetLayer (pcName);
					
					switch(cMode){
					case '*':
						Top().SetUpperUnderLayer( pLayer ); //쌍방향 연결
						break;
					case '+':
						Top().SetUpperLayer( pLayer ); //상위 레이어와 연결
						break;
					case '-':
						Top().SetUnderLayer( pLayer ); //하위 레이어와 연결
						break;
					}					
				}
			}
			
			pNode = pNode.next;
				
		}
	}
	
	
}

