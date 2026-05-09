# TODOS

## loadNativeAd 공통 인터페이스화

**What:** `expect class PlatformNativeAd` 래퍼 도입으로 `loadNativeAd`를 `AdManager` 공통 인터페이스에 올리기

**Why:** 현재 `loadNativeAd` 콜백 타입이 `NativeAd`(Android) / `GADNativeAd`(iOS)로 플랫폼 종속이라 공통 인터페이스에 올리지 못함. 소비안이 공통 코드에서 네이티브 광고를 다루려면 다운캐스팅 필요.

**Pros:** AdManager 인터페이스가 완전해짐. 소비안 공통 로직에서 네이티브 광고 핸들링 가능.

**Cons:** `expect class PlatformNativeAd` 도입으로 복잡도 증가. 네이티브 광고 기능이 풍부해질수록 래퍼가 무거워짐.

**Context:** `PlatformContext expect/actual` 도입 이후 자연스러운 다음 단계. `PlatformNativeAd`는 loadNativeAd 콜백 타입에만 사용하면 됨. IosAdManager에 `activeNativeLoaders` 메모리 누수 패치(onLoadFinished 추가)가 먼저 완료되어야 함.

**Depends on:** `expect/actual PlatformContext` PR 머지 후 진행
