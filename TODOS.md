# TODOS

<!-- 완료된 항목 없음. 새 TODO는 아래에 추가 -->

- [x] iOS Pangle 미디에이션 어댑터 — 이 라이브러리가 아니라 Rosary 앱 SPM에 추가함 (juooy/Rosary PR #31, revision `e983219` = 8.4.0.7.0). verified: `xcodebuild` NovenaApp·RosaryApp 시뮬레이터 빌드 통과, 앱 번들에 PangleAdapter·PAGAdSDK 포함, 2026-11-15
- [x] GDPR 동의를 Pangle까지 전달 — **`setPAConsent`가 아니라 UMP를 켜는 문제다.** Google 문서상 Android 어댑터 7.9.0.9.0+, iOS 7.9.0.6.0+는 UMP 신호를 Pangle에 자동 전달한다. `setPAConsent`는 Android 문서에서 미국 주 규정 절에만 나온다. Rosary는 `NoOpConsentStrategy`로 UMP를 끄고 있어(양 flavor `MainActivity`) 현재 전달할 신호가 없다. AdMob 동의 양식을 만든 뒤 `UmpConsentStrategy`로 되돌린다 — Android: juooy/Rosary PR #30(병합)에서 `UmpConsentStrategy` 복귀. iOS: PR #31에서 UMP 추가. verified: Android `./gradlew test`(JDK 17) 통과, iOS 시뮬레이터에서 UMP 단계 뒤 ATT 창 표시, 2026-11-15. 남은 것: AdMob GDPR 메시지 생성(콘솔)
- [ ] iOS `Google-Mobile-Ads-SDK ~> 10.0` pod 정리 — CocoaPods Specs가 2026-12-02부터 읽기 전용이고 Google은 그 뒤 어댑터를 올리지 않는다. WhatsYourName iOS 경로에 영향

## 콘솔 작업 — Pangle·UMP 출시 후속 (사용자 담당, 2026-11-15 기록)

코드는 끝났고 콘솔에서만 할 수 있는 일이다. Pangle 어댑터는 이 라이브러리를 거쳐 **소비 앱 전부**(Rosary·CatholicPrayForTheDead·Microphone·WhatsYourName·MagicTakeOut·Whisky)에 들어갔으므로, 데이터 보안 항목은 앱마다 확인한다.

- [ ] **AdMob GDPR 메시지 만들기** — AdMob → 개인정보 보호 및 메시지 → 유럽 규정. 앱별(Android·iOS 각각). 만들기 전까지 UMP 상태가 UNKNOWN이라 동의 창이 뜨지 않는다(광고는 나감). 우선 Rosary(novena `kr.co.juooy.rosary`, rosary `kr.co.juooy.rosary2`, iOS 2종)
  - 선택: IDFA 안내 메시지 — iOS에서 ATT 권한 창 앞에 설명 화면을 보여 준다
  - 확인: 실기기에서 UMP 디버그 지역을 EEA로 두고 동의 창·설정의 「광고 개인정보 설정」 버튼 표시
- [ ] **Pangle KR 매핑** — Pangle 콘솔에서 앱·네이티브 placement(In-App Bidding) 생성 → AdMob 미디에이션 그룹 입찰에 **Pangle KR** 추가·매핑(App ID, Placement ID). ROW 매핑만으로는 한국 사용자 요청이 가지 않는다. 같은 그룹에서 일치 0%인 Meta·Pangle 폭포식 항목 제거. 우선 노베나 Android, iOS는 출시 전
  - 확인: Ad Inspector 단일 소스 테스트 「Pangle KR SDK (Bidding)」, 4주 뒤 입찰 요청·참여율·낙찰률
- [ ] **Pangle DSP 파트너 추가** — AdMob → 개인정보 보호 및 메시지 → 유럽 규정·미국 주 규정의 광고 파트너 목록에 「Pangle DSP」 체크 (계정 관리 권한 필요)
- [ ] **Play 데이터 보안 양식** — Pangle SDK(+ TikTok Business SDK)가 수집하는 항목을 앱별로 반영: Rosary 2종, CatholicPrayForTheDead, MIC, 그 외 출시·출시 예정 앱. 개인정보처리방침에 제3자(Pangle/ByteDance) 추가
  - ⚠️ **Whisky(`com.uwual.cask68`, 사용자 소유·팀 공동 개발)**: 광고를 쓰지 않는데 JuooyLibrary(`implementation 'kr.co.juooy:ads'`) 경유로 AdMob·Pangle SDK가 포함된다. 증상과 선택지는 Whisky `TODOS.md` 「쓰지 않는 광고 SDK가 앱에 포함됨」 (팀 결정 대기)
- [ ] **App Store 개인정보 라벨** — Rosary iOS 출시 제출 때 Pangle 수집 항목(Pangle 문서 「App privacy details on the App Store」) 반영. WhatsYourName iOS는 Pangle 미포함

