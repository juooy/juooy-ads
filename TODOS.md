# TODOS

<!-- 완료된 항목 없음. 새 TODO는 아래에 추가 -->

- [ ] iOS Pangle 미디에이션 어댑터 추가 — **2027년 초 iOS 출시 전에 진행** (사용자 확인, 2026-11-08). 지금은 App Store 공개 목록에 노베나·묵주기도 iOS가 없다 (iTunes lookup `kr.co.juooy.novena.Rosary`·`kr.co.juooy.rosary.Rosary`, kr·us 모두 0건, 2026-11-08)
  - 위치 주의: Rosary iOS는 이 라이브러리를 쓰지 않고 앱에서 SPM `swift-package-manager-google-mobile-ads`(11.13.0)를 직접 링크한다. 그래서 어댑터는 이 리포 podspec이 아니라 **앱 쪽 SPM**에 넣어야 한다. 이 리포 pod 경로를 쓰는 iOS 앱은 WhatsYourName이다
- [ ] `PangleMediationAdapter.setPAConsent` 연결 여부 결정 — 필요한 입력: AdMob 국가별 보고서의 EU·EEA·영국 노출 비중 (기존 CSV 2종에는 국가 측정기준이 없다)
