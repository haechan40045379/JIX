# 팀원 소스 최종 통합 안내

## 현재 프로젝트의 상태
- **JavaFX 전용** 게임 플레이 UI. Swing JFrame / JFXPanel 사용하지 않음.
- `MainApp`은 단 하나의 실행 진입점.
- 공통 `GameManager`가 보유 열쇠·게임 제한시간·방 이동·탈출 판정을 관리.
- `RoomId`의 6개 ID와 `ItemId`의 열쇠 3개 ID는 팀원 전체에 적용.
- 기존 메인 화면/시나리오 선택 이미지와 배치/아이리스 전환을 재사용.
- 식당·현관의 실제 퍼즐, 팀원 4개 방의 퍼즐은 **아직 완성되지 않음**.

## 실제 팀원 파일 받았을 때
1. 팀원의 소스를 `rooms/<방명>/` 아래 새 클래스로 구성. **MainApp.java 복사 금지**.
2. 각 방은 `RoomModule`을 구현하고 `id()` / `createView(GameContext)`를 제공.
3. 이미지 및 효과 파일은 `src/main/resources/images/rooms` 등에 넣음.
4. 팀원이 자체 구현한 `JFrame`, `Application`, 타이머, 인벤토리는 공통 구조에 연결하도록 변환.
5. `RoomManager.createDefault()`의 `register(...)`를 실제 모듈로 변경.
6. 보유 아이템에 직접 접근하는 대신 `context.grantItem(ItemId....)` 호출.
7. 현관문에서는 `context.attemptEscape()` 호출. 3개 키 판정은 공통 코드가 수행.

## 화면 좌표 예시
```java
RoomCanvas room = new RoomCanvas(id().imagePath(), "식당");
room.addHotspot(0.30, 0.49, 0.42, 0.34, "식탁 조사",
    () -> context.say("식탁 위에서 낡은 문양을 발견했다."));
return room;
```
x,y,w,h는 이미지 기준 0~1 비율. 추후 UI 비율을 바꿔도 좌표가 함께 대응.

## 개발 테스트
- 상단 ⚙ 버튼 → `방 이동 테스트` 또는 `열쇠 지급 테스트`.
- 열쇠 3개를 지급 후 현관으로 이동하여 가운데 문을 클릭하면 클리어.
- 양옆 화살표는 임시 순서(침실→서재→주방→저장고→식당→현관) 이동.
  **실제 저택 지도/출입 조건을 확정하면 교체해야 함**.
- 테스트용 열쇠 지급 기능은 최종 배포 시 제거.
- UI 참고 이미지와 다른 부분은 실제 배경·아이콘·디자인 확정 후 CSS/이미지 교체.

## Git 권장 원칙
- `main`은 정상 실행되는 버전만 유지.
- 파일 하나에 세 명이 동시에 수정하지 말고 각자 rooms 디렉터리를 유지.
- 변경 사항은 팀원별 브랜치에서 작업 후 통합 담당자가 병합.
