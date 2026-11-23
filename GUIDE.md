# Escape Manor — JavaFX 방탈출 게임 (팀 공통 틀 v0.1)

> ---
>
> 각 팀원은 자신이 담당한 두 개의 방과 퍼즐을 개발하고, 공통 시스템에 연결합니다.

## 1. 프로젝트 개요

- 과목: 지능형 UI/UX 프로그래밍 팀 프로젝트
- 언어 / UI: **Java / JavaFX** (현재 첨부 코드 기준)
- 빌드 도구: Maven (`pom.xml`)
- 장르: 2D 포인트 앤 클릭 방탈출 게임
- 공간: 침실, 서재, 주방, 지하 저장고, 식당, 현관 홀 (총 6개 방)
- 목표: 세 구역에서 열쇠를 1개씩, **총 3개** 획득해 **현관문으로 최종 탈출**
- 제한시간 기획: 60분의 **행동 기반 가상시간**. **현재 v0.1 코드에는 실시간 초 단위 타이머가 들어 있어 수정 예정입니다.**

### 팀원별 개발 담당

| 담당   | 구현할 방          | 최종 획득 열쇠 (`ItemId`) | 주요 Java 파일                                                     |
| ------ | ------------------ | ------------------------- | ------------------------------------------------------------------ |
| 팀원 A | 침실 + 서재        | `BEDROOM_STUDY_KEY`       | `rooms/bedroom/BedroomRoom.java`, `rooms/study/StudyRoom.java`     |
| 팀원 B | 주방 + 지하 저장고 | `KITCHEN_STORAGE_KEY`     | `rooms/kitchen/KitchenRoom.java`, `rooms/storage/StorageRoom.java` |
| 팀원 C | 식당 + 현관 홀     | `DINING_ENTRANCE_KEY`     | `rooms/dining/DiningRoom.java`, `rooms/entrance/EntranceRoom.java` |

> 역할 이름은 편의상 표기한 것입니다. 각자 담당 방의 클래스와 이미지부터 작업하세요.

## 2. 지금 구현된 기능 / 아직 할 일

### 구현된 공통 기능

- 메인 화면 → 시나리오 선택 → 게임 화면 연결
- 시나리오 선택 시 아이리스 전환 애니메이션
- 기본 게임 UI 레이아웃: 상단 상태바, 중앙 방 이미지, 우측 인벤토리, 하단 설명문
- 6개 방을 위한 별도 `RoomModule` 클래스와 등록 시스템
- 비율 좌표(0~1)로 클릭 영역(`hotspot`) 설정
- 임시 이전/다음 방 이동, 조사 결과 메시지 및 저널 기록
- 테스트 메뉴의 열쇠 지급 및 열쇠 3개 보유 시 현관 탈출 성공 처리
- 지정된 파일명으로 이미지를 추가하면 해당 방 배경을 읽어오는 구조

### 미완성 / 주의 사항

- **실제 퍼즐은 구현 전**: 현재 각 방의 조사 지점은 테스트용입니다.
- **실제 배경 6장 없음**: 파일이 없으면 임시 안내 문구가 표시됩니다.
- **방 이동은 임시 순환 이동**: 실제 저택 통로·잠금 조건은 미연결입니다.
- **타이머 불일치**: 현재는 현실시간으로 매초 감소하며, 기획한 행동 기반 차감으로 바꿔야 합니다.
- 현재 `ItemId`에는 **열쇠 3종만** 등록되어 있습니다. 퍼즐 아이템은 팀 협의 후 공통 enum을 추가해야 합니다.
- 저장/불러오기, 힌트 등은 미구현입니다.
- 시나리오 카드에 기존 독스프 이미지가 남아 있습니다. 최종 리소스 변경 예정입니다.
- 이미지를 넣은 뒤 **클릭 영역 좌표를 실제 오브젝트 위치에 맞춰 다시 조정**해야 합니다.

## 3. 필요한 프로그램

1. **JDK 21** (프로젝트 기준 Java 21로 컴파일하도록 설정되어 있음)
2. **Maven** (프로젝트의 라이브러리 및 실행 관리)
3. **Eclipse**(권장) 또는 Maven 프로젝트를 지원하는 Java IDE
4. **GitHub Desktop** (Git 명령어에 익숙하지 않을 때 협업용으로 권장)

`pom.xml`에 JavaFX 의존성이 선언되어 있어, **일반적으로 JavaFX SDK를 별도로 수동 설치하지 않고 Maven으로 받아 실행**합니다. 최초 의존성 다운로드에는 인터넷 연결이 필요합니다.

## 4. 프로젝트 열고 실행하기 (Eclipse / Windows)

1. GitHub에서 프로젝트를 **Clone**하거나 압축파일을 내려받아 압축을 풉니다.
2. Eclipse → **File > Import... > Maven > Existing Maven Projects**.
3. **Root Directory**에 `pom.xml`이 있는 `EscapeManor-JavaFX-Starter` 폴더를 선택합니다.
4. 프로젝트 우클릭 → **Maven > Update Project**.
5. 프로젝트 우클릭 → **Run As > Maven build...**.
6. **Goals**에 `clean javafx:run` 입력 → **Run**.
7. 메인 화면에서 게임 시작 → 시나리오 1을 선택합니다.

터미널로 실행한다면 프로젝트 폴더(`pom.xml`이 있는 위치)에서:

```bash
mvn clean javafx:run
```

- 실제 실행 시작점: `src/main/java/com/escapemanor/MainApp.java`
- **팀원이 별도의 `MainApp` 또는 `Application` 클래스를 새로 만들 필요는 없습니다.**

## 5. 팀원이 수정할 곳

```text
EscapeManor-JavaFX-Starter/
├─ README.md                          # 프로젝트 및 협업 설명 (이 파일)
├─ pom.xml                            # Maven / JavaFX 설정 (공통)
├─ .gitignore                         # Git에 넣지 않을 파일 설정
├─ docs/
│  └─ TEAM_INTEGRATION_GUIDE.md      # 통합 담당자를 위한 설명
└─ src/main/
   ├─ java/com/escapemanor/
   │  ├─ MainApp.java                 # 프로그램 실행 시작점 (공통)
   │  ├─ core/                        # 상태·아이템·방 연결·탈출 (공통)
   │  │  ├─ GameManager.java
   │  │  ├─ GameState.java
   │  │  ├─ GameContext.java
   │  │  ├─ RoomId.java
   │  │  ├─ ItemId.java
   │  │  ├─ RoomModule.java
   │  │  └─ RoomManager.java
   │  ├─ ui/                          # 공통 게임 화면·메뉴 (공통)
   │  └─ rooms/
   │     ├─ bedroom/BedroomRoom.java  # 침실 담당
   │     ├─ study/StudyRoom.java      # 서재 담당
   │     ├─ kitchen/KitchenRoom.java  # 주방 담당
   │     ├─ storage/StorageRoom.java  # 저장고 담당
   │     ├─ dining/DiningRoom.java    # 식당 담당
   │     └─ entrance/EntranceRoom.java # 현관 담당
   └─ resources/
      ├─ images/rooms/               # 방 배경 이미지
      ├─ assets/                     # 기존 메인/시나리오 이미지
      └─ css/                        # UI 스타일
```

**작업 규칙**

- 자신이 맡은 `rooms/` 폴더의 Java 클래스와 이미지 파일을 우선 수정합니다.
- `core/`, `ui/`, `MainApp.java`, `pom.xml`은 **공통 파일**입니다. 수정이 필요하면 팀 채팅에서 먼저 공유하고 통합 담당자와 조율합니다.
- 메인 화면·시나리오 선택·게임 UI의 **기존 화면 배치와 위치를 임의로 바꾸지 않습니다.**
- 팀원별 타이머·인벤토리·메인 실행 클래스를 새로 만들지 않습니다. 공통 시스템을 사용합니다.
- **현관문은 최종 탈출문**이며 일반 방 이동에 사용하지 않습니다.

## 6. 방 배경 이미지 넣는 방법

`src/main/resources/images/rooms/`에 **정확한 이름**으로 PNG 파일을 저장합니다.

| 방          | 파일명         |
| ----------- | -------------- |
| 침실        | `bedroom.png`  |
| 서재        | `study.png`    |
| 주방        | `kitchen.png`  |
| 지하 저장고 | `storage.png`  |
| 식당        | `dining.png`   |
| 현관        | `entrance.png` |

권장: 가로 16:9 (예: 1600×900). **게임 UI(버튼, 인벤토리, 시간 등)를 이미지에 그리지 마세요.**
현재 `RoomCanvas`는 이미지를 영역 크기에 맞춰 그리므로, 비율과 클릭 위치를 실제 결과 화면에서 확인해야 합니다.

## 7. 퍼즐·클릭 영역 구현 예시

식당 파일 `rooms/dining/DiningRoom.java`는 아래와 같은 구조를 사용합니다.

```java
@Override
public Parent createView(GameContext context) {
    RoomCanvas room = new RoomCanvas(id().imagePath(), "식당");
    room.addHotspot(0.30, 0.49, 0.42, 0.34, "식탁 조사",
        () -> context.addClue("dining.table.look", "식탁에 오래된 흔적이 있다."));
    return room;
}
```

- `addHotspot(x, y, width, height, 설명, 클릭했을_때_동작)`
- 좌표는 **0~1 비율**입니다. 예: `x=0.30`은 화면 가로 30% 지점.
- `context.say("문구")`: 설명 출력 + 기록
- `context.addClue("고유ID", "문구")`: 새로운 단서 1회 등록
- `context.setFlag("고유ID")` / `context.flag("고유ID")`: 퍼즐 완료 여부 기억
- `context.grantItem(ItemId.BEDROOM_STUDY_KEY)`: 열쇠 획득(담당 키로 변경)
- `context.hasItem(ItemId.BEDROOM_STUDY_KEY)`: 해당 열쇠 보유 여부 확인
- `context.goTo(RoomId.STUDY)`: 방 이동 (실제 이동 조건은 직접 설계)
- `context.attemptEscape()`: 현관에서 열쇠 3개 보유 여부 확인

`ItemId`에 없는 새 퍼즐 아이템을 추가해야 한다면 **팀원끼리 먼저 ID 이름을 합의**하고 공통 파일 변경을 요청하세요. 현재 `GameContext`에는 행동별 가상시간 차감 API가 없으므로, 시간을 직접 깎는 코드는 임의 구현하지 않고 공통 시스템 업데이트 이후 연결합니다.

## 8. 현재 버전 테스트 방법

1. 게임을 실행합니다. 초기 방은 침실입니다.
2. 화면의 **이전/다음 화살표**로 6개 방이 전환되는지 확인합니다. (현재는 임시 테스트 전용)
3. 방의 조사 지점을 클릭하여 하단 설명문과 저널에 기록되는지 확인합니다.
4. 상단 **⚙ 설정 / 통합 테스트 → 열쇠 지급 테스트**로 열쇠 3개를 각각 지급합니다.
5. **방 이동 테스트 → 현관** 이동 → 가운데 탈출문 영역 클릭.
6. 3개가 있으면 탈출 성공, 부족하면 현재 열쇠 개수가 표시됩니다.

현재 실제 방 이미지가 없고 클릭 영역도 임시 좌표이므로, 조사 지점이 눈에 보이지 않을 수 있습니다.

## 9. GitHub 협업 방법 (처음 하는 팀원용)

### 최초 프로젝트를 올리는 사람

1. 프로젝트 폴더에 이 `README.md`, `pom.xml`, `src/`가 있는지 확인합니다.
2. **GitHub Desktop**에 로그인합니다.
3. **File → Add Local Repository...**에서 이 프로젝트 폴더를 선택합니다.
4. 아직 Git 저장소가 아니라고 나오면 **Create a Repository Here**로 초기화합니다. 이미 `.gitignore`, `README.md`가 있으므로 중복 생성하지 마세요.
5. **Changes**에서 파일들을 확인합니다. 아래 `target/` 파일이 Git에 포함되지 않아야 정상입니다.
6. 변경 내용을 **Commit to main**(예: `Initial JavaFX project structure`)으로 기록합니다.
7. **Publish repository**를 클릭합니다. 초반에는 **Keep this code private** 체크를 권장합니다.
8. GitHub 사이트의 저장소 **Settings → Collaborators**에서 팀원 두 명을 초대하고 초대 링크/권한을 확인합니다.

### 나머지 팀원

1. GitHub Desktop → **File → Clone repository...** → 초대받은 저장소 선택 → 원하는 위치에 Clone.
2. **Current branch → New branch**로 자신만의 브랜치를 만듭니다. 예: `feature/bedroom-study`, `feature/kitchen-storage`, `feature/dining-entrance`.
3. 자신 담당 파일 수정 → **Commit to 브랜치명** → **Publish branch**(처음) 또는 **Push origin**(이후).
4. 작업 단위가 완성되면 GitHub에서 **Pull Request**를 만들고 통합 담당자가 확인한 뒤 `main`에 병합합니다.
5. 다른 사람의 변경을 받으려면 **Fetch origin / Pull origin**으로 최신 상태를 받습니다.

**용어 정리:** `Commit` = 변경 기록(내 컴퓨터), `Push` = GitHub에 올리기, `Pull` = GitHub의 변경 내려받기, `Branch` = 각자 작업 줄기, `Pull Request(PR)` = 내 작업을 `main`에 합쳐 달라는 요청.

### 절대 주의

- `main`은 정상 실행 확인된 코드만 넣습니다.
- `target/`, `.class`, 개인 IDE 설정 등 빌드 결과물은 업로드하지 않습니다 (`.gitignore` 참고).
- 다른 사람이 수정한 내용을 확인하지 않고 덮어쓰지 않습니다.
- 계정 비밀번호, 토큰, API 키는 Git에 올리지 않습니다.
- 방 배경 이미지 등 큰 파일이 많아지면 파일 크기와 저장소 용량을 함께 관리합니다.

## 10. 다음 작업 우선순위

1. 공통 타이머를 **행동 기반 60분**으로 변경하고 방 모듈용 시간 차감 API 제공
2. 6개 방 배경 이미지 제작 및 클릭 좌표 조정
3. 방별 퍼즐과 일반 아이템(`ItemId`) 구현
4. 실제 방 연결 구조 및 통행 조건 구현
5. 열쇠 3개 통합, 실패/성공 처리 테스트
6. 테스트 메뉴 정리 및 최종 사용성 점검

기술적인 파일 통합 예시는 [`docs/TEAM_INTEGRATION_GUIDE.md`](docs/TEAM_INTEGRATION_GUIDE.md)를 참고하세요.
