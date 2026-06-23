# juooy-ads — Library Reference

KMP (Android + iOS) AdMob 래퍼. `commonMain`에서 공통 인터페이스로 광고를 제어하고, 플랫폼별 구현체가 실제 SDK를 호출한다.

---

## 핵심 타입

```kotlin
// 공통
expect class PlatformContext       // Android = Activity, iOS = UIViewController
expect class PlatformNativeAd {    // Android wraps NativeAd, iOS wraps GADNativeAd
    fun destroy()
}

data class AdConfig(
    val appId: String,
    val bannerAdUnitId: String,
    val interstitialAdUnitId: String,
    val rewardedAdUnitId: String,
    val nativeAdUnitId: String,
    val exitAdUnitId: String,
    val openAdUnitId: String = "",
    val isTestMode: Boolean = false
)

data class AdError(val code: Int, val message: String, val type: AdType)
enum class AdType { BANNER, INTERSTITIAL, REWARDED, NATIVE, APP_OPEN }

interface AdEventListener {
    fun onAdLoaded(type: AdType) {}
    fun onAdFailed(error: AdError) {}
    fun onUserEarnedReward(amount: Int, rewardType: String) {}
    fun onAdRevenuePaid(value: Double, currencyCode: String) {}
}
```

---

## AdManager 인터페이스 (commonMain)

```kotlin
interface AdManager {
    val config: AdConfig?
    fun isInitialized(): Boolean

    // 1단계: 항상 먼저 호출
    fun initialize(config: AdConfig, listener: AdEventListener? = null)

    // 2단계: 플랫폼 컨텍스트로 SDK 초기화 (동의 요청 포함)
    fun initializeWithPlatformContext(context: PlatformContext, onReady: (() -> Unit)? = null)

    fun showInterstitial(context: PlatformContext, onDismissed: (() -> Unit)? = null): Boolean
    fun showRewarded(
        context: PlatformContext,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onDismissed: (() -> Unit)? = null
    ): Boolean

    fun loadNativeAd(
        context: PlatformContext,
        adUnitId: String,
        onLoaded: (PlatformNativeAd) -> Unit,
        onFailed: ((AdError) -> Unit)? = null
    )
}

// 전역 접근
object AdManagerProvider {
    fun set(manager: AdManager)
    fun get(): AdManager           // 미설정 시 throw
    fun getOrNull(): AdManager?
}
```

---

## Android 설정

### Application.onCreate()

```kotlin
val adManager = AndroidAdManager()
adManager.initialize(
    AdConfig(
        appId = "ca-app-pub-xxx~yyy",
        bannerAdUnitId = "ca-app-pub-xxx/yyy",
        interstitialAdUnitId = "ca-app-pub-xxx/yyy",
        rewardedAdUnitId = "ca-app-pub-xxx/yyy",
        nativeAdUnitId = "ca-app-pub-xxx/yyy",
        exitAdUnitId = "ca-app-pub-xxx/yyy",
        openAdUnitId = "ca-app-pub-xxx/yyy",  // App Open 사용 시
        isTestMode = BuildConfig.DEBUG
    )
)
AdManagerProvider.set(adManager)
```

### Activity에서 SDK 초기화 (동의 포함)

```kotlin
// ConsentStrategy 기본값 = UmpConsentStrategy (GDPR UMP)
adManager.initializeWithActivity(
    activity = this,
    consentStrategy = UmpConsentStrategy(),  // 테스트: NoOpConsentStrategy()
    onReady = { /* 광고 노출 가능 */ }
)
// 또는 공통 인터페이스로:
adManager.initializeWithPlatformContext(context = this, onReady = { })
```

### 배너 (Compose)

```kotlin
BannerAdView(
    adUnitId = "ca-app-pub-xxx/yyy",
    adSize = AdSize.BANNER,           // 기본값
    modifier = Modifier.fillMaxWidth(),
    onAdLoaded = { },
    onAdFailed = { error -> }
)
```

### 전면 / 보상형

```kotlin
// 전면 — preloadInterstitial()은 initializeWithActivity 내부에서 자동 호출
adManager.showInterstitial(activity) { /* onDismissed */ }

// 보상형
adManager.showRewarded(
    context = activity,
    onUserEarnedReward = { amount, type -> },
    onDismissed = { }
)
```

### App Open (Android)

```kotlin
// AppOpenAdManager는 AndroidAdManager에만 있음
val appOpenManager = AppOpenAdManager(adManager)
appOpenManager.currentActivity = this

// ProcessLifecycleOwner에 등록 → 포그라운드 진입 시 자동 노출
ProcessLifecycleOwner.get().lifecycle.addObserver(appOpenManager)

// 특정 화면에서 억제 (예: 결제 화면)
appOpenManager.isAdSuppressed = true
```

### 네이티브 광고

```kotlin
adManager.loadNativeAd(
    context = activity,
    adUnitId = "ca-app-pub-xxx/yyy",
    onLoaded = { platformNativeAd ->
        val nativeAd = platformNativeAd.nativeAd  // com.google.android.gms.ads.nativead.NativeAd
        // NativeAdView에 바인딩 후 표시
    },
    onFailed = { error -> }
)
```

---

## iOS 설정

### AppDelegate (또는 SwiftUI @main)

```swift
// Kotlin에서 초기화
let adManager = IosAdManager()
adManager.initialize(
    config: AdConfig(
        appId: "ca-app-pub-xxx~yyy",
        bannerAdUnitId: "ca-app-pub-xxx/yyy",
        interstitialAdUnitId: "ca-app-pub-xxx/yyy",
        rewardedAdUnitId: "ca-app-pub-xxx/yyy",
        nativeAdUnitId: "ca-app-pub-xxx/yyy",
        exitAdUnitId: "ca-app-pub-xxx/yyy",
        openAdUnitId: "ca-app-pub-xxx/yyy",
        isTestMode: true
    ),
    listener: nil
)
AdManagerProvider.shared.set(manager: adManager)
```

### SDK 초기화 (ATT 동의 포함)

```swift
// consentStrategy 기본값 = AttIosConsentStrategy (ATT 팝업)
adManager.initializeWithViewController(
    viewController: self,
    consentStrategy: AttIosConsentStrategy(),  // 테스트: NoOpIosConsentStrategy()
    onReady: { /* 광고 노출 가능 */ }
)
// 또는 공통 인터페이스로:
adManager.initializeWithPlatformContext(context: self, onReady: { })
```

### 배너 (UIKit)

```swift
let bannerManager = IosBannerAdManager(adManager: adManager)
let bannerView = bannerManager.loadBannerAd(
    viewController: self,
    adUnitId: "ca-app-pub-xxx/yyy",
    onLoaded: { },
    onFailed: { error in }
)
view.addSubview(bannerView)
// SwiftUI: UIViewRepresentable로 wrapping
```

### 전면 / 보상형

```swift
// 전면 — preloadInterstitial()은 initializeWithViewController 내부에서 자동 호출
adManager.showInterstitial(context: self, onDismissed: nil)

// 보상형
adManager.showRewarded(
    context: self,
    onUserEarnedReward: { amount, type in },
    onDismissed: nil
)
```

### App Open (iOS)

```swift
// Android의 자동 생명주기 훅 없음 — 수동으로 호출해야 함
let appOpenManager = IosAppOpenAdManager(adManager: adManager)

// SceneDelegate 또는 AppDelegate에서:
func sceneDidBecomeActive(_ scene: UIScene) {
    appOpenManager.showIfAvailable(viewController: rootVC, onDismissed: nil)
}

// 억제
appOpenManager.isAdSuppressed = true
```

### 네이티브 광고

```swift
adManager.loadNativeAd(
    context: self,
    adUnitId: "ca-app-pub-xxx/yyy",
    onLoaded: { platformNativeAd in
        let gadNativeAd = platformNativeAd.gadNativeAd  // GADNativeAd
        // GADNativeAdView에 바인딩 후 표시
    },
    onFailed: { error in }
)
```

---

## 공통 코드에서 사용 (commonMain)

```kotlin
// 초기화 이후 어디서든
val manager = AdManagerProvider.get()

manager.showInterstitial(context) { /* onDismissed */ }

manager.loadNativeAd(
    context = context,
    adUnitId = manager.config?.nativeAdUnitId ?: "",
    onLoaded = { ad ->
        // PlatformNativeAd.destroy()는 공통 코드에서 호출 가능
        // 실제 뷰 렌더링은 platformMain에서 처리 (ad.nativeAd / ad.gadNativeAd)
        ad.destroy()
    }
)
```

---

## ConsentStrategy 교체

| 플랫폼 | 프로덕션 | 테스트 |
|--------|---------|--------|
| Android | `UmpConsentStrategy()` | `NoOpConsentStrategy()` |
| iOS | `AttIosConsentStrategy()` | `NoOpIosConsentStrategy()` |

커스텀 전략은 각 인터페이스를 구현:
- Android: `ConsentStrategy.requestConsent(activity, onSuccess, onFailure)`
- iOS: `IosConsentStrategy.requestConsent(onComplete)`

---

## 주의 사항

- `initialize(config)` → `initializeWithPlatformContext(context)` 순서 필수
- Android `showInterstitial`/`showRewarded`: `activity.isFinishing || activity.isDestroyed` 내부에서 자동 체크
- iOS App Open: `applicationDidBecomeActive` 등 생명주기 훅에서 직접 `showIfAvailable()` 호출
- `AdManager` 신규 메서드에는 모두 default 구현 있음 — 소비안 breaking change 없음
- `AdType.NATIVE`, `AdType.APP_OPEN` 추가 시 `when` exhaustive 체크 주의
