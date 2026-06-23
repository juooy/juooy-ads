# 프로젝트 개요

## 3-Agent 팀 구조
1. 아키텍트(Architect) : 전체 시스템을 이해하고 상세한 작업 계획(브리프)만 세웁니다.
2. 빌더(Builder) : 아키텍트의 브리프에 있는 내용만 정확히 구현하며, 전체 코드를 뒤지지 않습니다.
3. 리뷰어(Reviewer) : 완성된 코드의 변경된 부분만 검증하고 피드백을 줍니다.

## Skill routing

When the user's request matches an available skill, ALWAYS invoke it using the Skill
tool as your FIRST action. Do NOT answer directly, do NOT use other tools first.
The skill has specialized workflows that produce better results than ad-hoc answers.

Key routing rules:
- Product ideas, "is this worth building", brainstorming → invoke office-hours
- Bugs, errors, "why is this broken", 500 errors → invoke investigate
- Ship, deploy, push, create PR → invoke ship
- QA, test the site, find bugs → invoke qa
- Code review, check my diff → invoke review
- Update docs after shipping → invoke document-release
- Weekly retro → invoke retro
- Design system, brand → invoke design-consultation
- Visual audit, design polish → invoke design-review
- Architecture review → invoke plan-eng-review
- Save progress, checkpoint, resume → invoke checkpoint
- Code quality, health check → invoke health

## KMP 플랫폼 지원 원칙

### PlatformContext expect/actual 패턴
- `commonMain/PlatformContext.kt`: `expect class PlatformContext`
- `androidMain/PlatformContext.kt`: `actual typealias PlatformContext = android.app.Activity`
- `iosMain/PlatformContext.kt`: `actual typealias PlatformContext = platform.UIKit.UIViewController`
- AdManager 인터페이스에 플랫폼 컨텍스트가 필요한 메서드는 반드시 `PlatformContext`를 사용할 것. `Any` 캐스팅 금지.

### 인터페이스 변경 규칙
- `AdManager` 인터페이스에 새 메서드 추가 시 `default` 구현(= false / = Unit 등) 필수 — 소비안 구현체 breaking change 방지
- `AdType` 열거형 값 추가 시 릴리즈 노트에 명시 (`when` 표현식 exhaustive 체크 영향)

### 플랫폼 대칭 원칙
- androidMain에 기능 추가 시 iosMain에도 대응 구현 필수 (역도 마찬가지)
- iOS BannerAdManager: UIView 팩토리 함수만 제공, embed는 소비안 담당
- iOS AppOpenAdManager: 자동 생명주기 훅 없음 — `showIfAvailable(vc)` 수동 호출 API만 제공, 소비안이 `applicationDidBecomeActive` 등에서 직접 호출

### ConsentStrategy 주입
- Android: `initializeWithActivity(activity, consentStrategy = UmpConsentStrategy())` — 교체 가능
- iOS: `initializeWithViewController(vc, consentStrategy = AttIosConsentStrategy())` — 교체 가능
- 테스트 환경: `NoOpConsentStrategy` / `NoOpIosConsentStrategy` 사용

### 광고 표시 안전 체크
- Android `showInterstitial`/`showRewarded` 호출 전 `activity.isFinishing || activity.isDestroyed` 체크 필수
- iOS `showInterstitial`/`showRewarded` 전 UIViewController가 유효한지 확인
