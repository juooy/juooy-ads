# Changelog

All notable changes to this project will be documented in this file.

## [1.2.1.0] - 2026-10-17

### Changed
- Kotlin Gradle plugins 2.2.10 → 2.4.21. Kotlin 2.2.10's cinterop (clang 19) cannot read the Xcode 27 SDK headers (`could not build module '_DarwinFoundation1'`), so the iOS targets did not compile
- Output stays readable by consumers on Kotlin 2.2.x: `languageVersion`/`apiVersion` = 2.2, `coreLibrariesVersion` = 2.2.10. Verified with CatholicPrayForTheDead, WhatsYourName and JuooyLibrary (2.2.0–2.2.10)

### Fixed
- iOS build on Xcode 27: transitive pods of Google-Mobile-Ads-SDK declare iOS 11.0/12.0 deployment targets, which Xcode 27 rejects (min 15.0). `podInstallSyntheticIos` now raises them to 15.0 in the generated Pods project

## [1.2.0.0] - 2026-10-17

### Added
- `NativeAdViewBinder` (Android): `inflate(parent)` + `bind(adView, ad)` render a `PlatformNativeAd` into the bundled `juooy_ads_native_medium` layout (`NativeAdView` with badge, icon, headline, body, `MediaView`, CTA). Replaces per-app copies and the old `nativetemplates` `TemplateView`
- `IosNativeAdViewFactory` (iOS): `createNativeAdView(ad, badgeText)` returns a bound `GADNativeAdView` built in code with Dynamic Type fonts
- Android resources (`juooy_ads_*` prefix): layout, badge string (en/ko), badge colors
- `AdEventListener.onAdShown` / `onAdClicked` / `onAdFailedToShow` (default no-op) for interstitial and rewarded, both platforms

### Fixed
- `initializeWithActivity` / `initializeWithViewController`: a second caller while init was still in flight (e.g. base Activity + subclass) silently lost its `onReady`. Callbacks are now queued and fire once when init settles (success, consent failure or timeout)
- `initializeWithActivity` / `initializeWithViewController` called before `initialize(AdConfig)` no longer blocks every later call
- `preloadInterstitial` / `preloadRewarded` skip blank ad unit IDs instead of issuing a failing load
- `UmpConsentStrategy`: when the consent info update itself fails (no privacy message configured in AdMob, offline) and status is still `UNKNOWN`, the SDK now initializes. Before, interstitial/rewarded never preloaded in that state. A determined status that disallows ads still blocks
- Interstitial: a failed preload (e.g. offline at launch) was never retried. `showInterstitial` now starts a reload when nothing is cached (after init), and a show failure clears the stale ad and reloads. Concurrent preloads are deduplicated
- iOS `AttIosConsentStrategy`: ATT completion runs on a background queue, so SDK start and its timeout `NSTimer` ran off-main (the timeout never fired). `onComplete` now hops to the main queue

## [1.1.0.2] - 2026-09-28

### Fixed
- iOS full-screen and native ad delegates: manager now holds strong references — GAD delegate properties are `weak`, so without this the delegate deallocates before callbacks fire (silent ad-event loss)
- `IosBannerAdManager`: banner delegates retained in a map; consumers should call `removeBannerAd(view)` when the banner is torn down
- `NativeAdLoaderDelegate`: only remove loader from `activeNativeLoaders` on `adLoaderDidFinishLoading` (previously removed twice on success/fail path)
- `PlatformNativeAd.destroy()` on iOS: SDK 10.x `GADNativeAd` has no `destroy()` — cleanup left to ARC (previous release incorrectly claimed to call it)

### Changed
- iOS state flags no longer marked `@Volatile` — Kotlin/Native new memory model + iOS main-thread invariant makes it unnecessary

## [1.1.0.1] - 2026-06-27

### Fixed
- `ads.podspec`: use `__dir__`-relative path for framework existence check so `pod install` works correctly from any working directory (e.g. `iosApp/`)

## [1.1.0.0] - 2026-06-24

### Added
- `PlatformContext` expect/actual: `Activity` on Android, `UIViewController` on iOS — replaces untyped `Any` parameter in all AdManager methods
- `PlatformNativeAd` expect/actual: wraps `NativeAd` (Android) and `GADNativeAd` (iOS) behind a unified `destroy()` interface
- `IosAppOpenAdManager` — App Open Ad management for iOS with manual `showIfAvailable(vc)` API and injectable time for expiry testing
- `IosBannerAdManager` — GADBannerView factory for iOS; returns a ready-to-embed UIView
- `IosConsentStrategy` interface with `AttIosConsentStrategy` (ATT prompt) and `NoOpIosConsentStrategy` (for testing)
- `AdType.APP_OPEN` enum value for App Open Ad event tracking
- `AppOpenAdManager.onAdShown` callback (Android) — fires after the ad is confirmed displayed
- Gradle wrapper and `libs.versions.toml` version catalog — consumers can now build without a pre-installed Gradle

### Changed
- `AdManager` interface: `showInterstitial`, `showRewarded`, `loadNativeAd`, `initializeWithPlatformContext` now accept `PlatformContext` instead of `Any`
- All methods have default implementations on the interface to avoid breaking change for consumers who implement `AdManager` directly
- `IosAdManager.initializeWithViewController` now accepts an injectable `IosConsentStrategy` (default: `AttIosConsentStrategy`)
- `AppOpenAdManager.currentActivity` now uses a `WeakReference` to prevent Activity leaks
- `AppOpenAdManager.isAdAvailable()` accepts injectable `now` parameter for deterministic unit testing
- iOS `showInterstitial`/`showRewarded` guard against concurrent presentation with `isShowingFullScreen` flag

### Fixed
- `PlatformNativeAd.destroy()` on iOS now correctly calls `GADNativeAd.destroy()` (was a silent no-op, causing memory leaks)
- `IosAppOpenAdManager` now reloads after presentation failure (Android parity)
- `IosBannerAdManager.onFailed` callback now delivers `AdError` instead of platform-specific `NSError`
- `AndroidAdManager.showInterstitial`/`showRewarded` check `activity.isFinishing || activity.isDestroyed` before showing
- `AppOpenAdManager.showAdIfAvailable` checks `activity.isFinishing || activity.isDestroyed` before showing
- Build is now compatible with AGP 9.x — upgrade your Android Gradle Plugin without breakage
- Deprecated `initializeWithContext(Any)` bridge now logs a warning on type mismatch instead of silently returning (was a silent swallow, making integration bugs hard to diagnose)
