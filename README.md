# Chatting-and-file-transfer
- 사용 Protocol : Simplest(여유 되는 경우 Stop and Wait로 변경)
- 전송 시나리오

## To-do List

### ChatFileDlg
- [ ] Layer 생성하고 연결하기 -> 어떻게 연결할 지 얘기 필요
- [x] 파일 전송 부분 GUI 작성
- [x] 파일 전송 관련 버튼 eventListener 작성
- [ ] ProgressBar 구현 : 파일 전송에 따라 어떻게 업데이트 할 지(쓰레드?)

### Ethernet Layer
- [ ] FileSend 함수 생성
- [ ] Send 함수 타입관련 수정
- [ ] Receive의 경우 type에 따라 case나눠서 상위 레이어로 전달.

### ChatAppLayer
- [ ] 패킷 type 부분 수정 필요
- [ ] 패킷 길이 변경(원래 단편화 기준은 10bytes -> Ethernet frame MTU = 1500bytes)
- [ ] Stop & Wait 프로토콜로 변경할 경우 ACK 관련 주석 제거