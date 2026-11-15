# TODOS

<!-- 완료된 항목 없음. 새 TODO는 아래에 추가 -->

- [x] iOS Pangle 미디에이션 어댑터 — 이 라이브러리가 아니라 Rosary 앱 SPM에 추가함 (juooy/Rosary PR #31, revision `e983219` = 8.4.0.7.0). verified: `xcodebuild` NovenaApp·RosaryApp 시뮬레이터 빌드 통과, 앱 번들에 PangleAdapter·PAGAdSDK 포함, 2026-11-15
- [x] GDPR 동의를 Pangle까지 전달 — **`setPAConsent`가 아니라 UMP를 켜는 문제다.** Google 문서상 Android 어댑터 7.9.0.9.0+, iOS 7.9.0.6.0+는 UMP 신호를 Pangle에 자동 전달한다. `setPAConsent`는 Android 문서에서 미국 주 규정 절에만 나온다. Rosary는 `NoOpConsentStrategy`로 UMP를 끄고 있어(양 flavor `MainActivity`) 현재 전달할 신호가 없다. AdMob 동의 양식을 만든 뒤 `UmpConsentStrategy`로 되돌린다 — Android: juooy/Rosary PR #30(병합)에서 `UmpConsentStrategy` 복귀. iOS: PR #31에서 UMP 추가. verified: Android `./gradlew test`(JDK 17) 통과, iOS 시뮬레이터에서 UMP 단계 뒤 ATT 창 표시, 2026-11-15. 남은 것: AdMob GDPR 메시지 생성(콘솔)
- [ ] iOS `Google-Mobile-Ads-SDK ~> 10.0` pod 정리 — CocoaPods Specs가 2026-12-02부터 읽기 전용이고 Google은 그 뒤 어댑터를 올리지 않는다. WhatsYourName iOS 경로에 영향
