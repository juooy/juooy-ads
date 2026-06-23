# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0.0] - 2026-06-24

### Added
- `PlatformContext` expect/actual: `Activity` on Android, `UIViewController` on iOS — replaces untyped `Any` parameter in all AdManager methods
- `PlatformNativeAd` expect/actual: wraps `NativeAd` (Android) and `GADNativeAd` (iOS) behind a unified `destroy()` interface
- `IosAppOpenAdManager` — App Open Ad management for iOS with manual `showIfAvailable(vc)` API and injectable time for expiry testing
- `IosBannerAdManager` — GADBannerView factory for iOS; returns a ready-to-embed UIView
- `IosConsentStrategy` interface with `AttIosConsentStrategy` (ATT prompt) and `NoOpIosConsentStrategy` (for testing)
- `AdType.APP_OPEN` enum value for App Open Ad event tracking
- `AppOpenAdManager.onAdShown` callback (Android) — fires after the ad is confirmed displayed
- Gradle wrapper and `libs.versions.toml` version catalog

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
- Build configuration updated for AGP 9.x compatibility
- Deprecated `initializeWithContext(Any)` bridge logs a warning on type mismatch instead of silently returning
