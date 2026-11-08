# TODOS

<!-- 완료된 항목 없음. 새 TODO는 아래에 추가 -->

- [ ] iOS Pangle 미디에이션 어댑터 추가 — **2027년 초 iOS 출시 전에 진행** (사용자 확인, 2026-11-08). 지금은 App Store 공개 목록에 노베나·묵주기도 iOS가 없다 (iTunes lookup `kr.co.juooy.novena.Rosary`·`kr.co.juooy.rosary.Rosary`, kr·us 모두 0건, 2026-11-08)
  - 위치 주의: Rosary iOS는 이 라이브러리를 쓰지 않고 앱에서 SPM `swift-package-manager-google-mobile-ads`(11.13.0)를 직접 링크한다. 그래서 어댑터는 이 리포 podspec이 아니라 **앱 쪽 SPM**에 넣어야 한다. 이 리포 pod 경로를 쓰는 iOS 앱은 WhatsYourName이다
- [ ] GDPR 동의를 Pangle까지 전달 — **`setPAConsent`가 아니라 UMP를 켜는 문제다.** Google 문서상 Android 어댑터 7.9.0.9.0+, iOS 7.9.0.6.0+는 UMP 신호를 Pangle에 자동 전달한다. `setPAConsent`는 Android 문서에서 미국 주 규정 절에만 나온다. Rosary는 `NoOpConsentStrategy`로 UMP를 끄고 있어(양 flavor `MainActivity`) 현재 전달할 신호가 없다. AdMob 동의 양식을 만든 뒤 `UmpConsentStrategy`로 되돌린다
- [ ] iOS `Google-Mobile-Ads-SDK ~> 10.0` pod 정리 — CocoaPods Specs가 2026-12-02부터 읽기 전용이고 Google은 그 뒤 어댑터를 올리지 않는다. WhatsYourName iOS 경로에 영향
