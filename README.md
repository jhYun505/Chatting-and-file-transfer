# Chatting-and-file-transfer
- 사용 Protocol : Stop and Wait
  - 데이터를 보내고 나서 수신자로부터 ACK가 올 때까지 기다린다.
  - FLOWCHART
    ![image](https://user-images.githubusercontent.com/81208791/169484859-b112d8d8-97e3-4a39-ac08-57291580fafe.png)



- 전송 시나리오
  ```
  - 두 대의 PC에서 각각 프로그램을 실행
  - 두 대의 PC는 네트워크로 연결
  - PC 1의 프로그램
    - 1-1. 전송할 파일 선택, 1-2. 전송할 텍스트 입력
    - 2. 전송 버튼을 클릭
    - 3. 레이어 아키텍쳐에 의해서 Encapsulated Packet이 만들어짐 (Ethernet frame)
    - 4. NILayer의 Pcap 라이브러리로 작성된 코드에 의해 packet은 네트워크로 전송됨
  - PC 2의 프로그램에서 PC1으로부터 Ethernet frame을 수신
    - 1. 하위 레이어부터 수신된 프로토콜이 PC 1로부터 보내진 패킷인지 검사
        아니라면, discard(버림)
    - 2. 맞으면, 레이어 아키텍쳐에 의해서 Demultiplexing을 통해 header를 제외한 data 부분을 상위 레이어로 전달
        - Header는 매 레이어마다 receive 함수에서 frame에 대해 나에게 온 것이 맞는 검사할 때 쓰임
    - 3. 최상위 레이어 (Dlg)에 전달 될 때 까지 1번과 2번 과정을 반복함
    - 4. 전달된 결과 확인
       - 파일인 경우, 채팅&파일전송 프로그램이 위치한 곳에 저장됨 (기본 저장 경로 사용 시)
       - 텍스트인 경우, 채팅&파일전송 프로그램 화면에서 채팅창(CListbox) 에 표기 됨
          + 표기 방법은 IPC에서 채팅 표기 방법과 동일
  ```
- 실습 환경
  - 두 대의 PC(가상 머신)에서 각각 프로그램을 실행
  - 두 대의 PC는 랜선(가상 이더넷 인터페이스)으로 연결
- 요구사항
  - Basic Design
    - One to one 방식
    - 시스템에는 하나의 프로세스만 동작하며, 통신할 대상은 다른 시스템의 프로세스로서 1:1통신
  - Chatting과 File 전송을 동시에 가능
    - Thread 구현
  - Chatting Message와 File Size는 제한 없이 전송 가능
    - Fragmentation 가능(Chatting, File 둘다)
  - 실제 Network Protocol (Ethernet Protocol)을 이용하여 Ethernet Frame을 송수신
    - Packet driver 이용하여 정보를 얻어 WinPcap(JnetPcap)을 이용하여 송수신
  - Ethernet Frame을 수신
    - NILayer에서 수신만을 하기 위한 Thread 구현


## To-do List

### ChatFileDlg
- [x] Layer 생성하고 연결하기 -> 모두 양방향 연결. `m_LayerMgr.ConnectLayers("NI ( *Ethernet ( *ChatApp ( *GUI ) *FileApp ( +GUI ) ))");`
- [x] 파일 전송 부분 GUI 작성
- [x] 파일 전송 관련 버튼 eventListener 작성
- [x] ProgressBar 구현 : 파일 전송에 따라 어떻게 업데이트 할 지(쓰레드?)

### Ethernet Layer
- [x] FileSend 함수 생성
- [x] Send 함수 타입관련 수정
- [x] Receive의 경우 type에 따라 case나눠서 상위 레이어로 전달.

### ChatAppLayer
- [x] 패킷 type 부분 수정 필요
- [x] 패킷 길이 변경(원래 단편화 기준은 10bytes -> Ethernet frame MTU = 1500bytes)
- [x] Stop & Wait 프로토콜로 변경할 경우 ACK 관련 주석 제거
