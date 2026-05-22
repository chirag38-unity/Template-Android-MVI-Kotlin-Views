# Architecture

This document describes the architecture of the Template Android MVI Multi-module project.

## Module Structure

```
app/                          # Application entry point, Hilt setup, navigation wiring
core/
  common/                     # Shared utilities: dispatchers, result types, extensions, DI helpers
  ui/                         # Base UI classes, MVI contracts, view utilities
  network/                    # Ktor HTTP client, connectivity, retry, auth
  database/                   # Room database, DAOs, migrations, sync helpers
  domain/                     # Use case base classes (pure Kotlin)
  navigation/                 # Navigation contracts and implementations
  cache/
    api/                      # Cache abstraction interfaces
    impl/                     # LRU + DataStore cache implementations
  testing/                    # Test helpers, rules, fakes, fixtures
feature-{name}/
  api/                        # Feature contract (entry point, public models)
  impl/                       # Feature implementation (UI, ViewModel, DI)
```

## Dependency Rules

Allowed dependency directions (→ = can depend on):

```
app → feature-*/impl → feature-*/api
app → core/*
feature-*/impl → core/common, core/ui, core/network, core/database, core/domain, core/navigation
feature-*/api  → core/common, core/domain
core/ui        → core/common
core/network   → core/common
core/database  → core/common
core/navigation → (no core deps; uses Android framework only)
core/testing   → core/common, core/navigation
core/cache/impl → core/cache/api, core/common
```

**Forbidden**: feature modules MUST NOT depend on other feature modules directly. Use the `api` module + navigation commands instead.

## MVI Flow

```
User Interaction
      │
      ▼
   Fragment
  (View Layer)
      │ sendIntent(intent)
      ▼
  ViewModel
  (BaseViewModel<State, Intent, Effect>)
      │
      ├── handleIntent(intent)
      │       │
      │       ├── updateState { ... }   ──► StateFlow<State> ──► Fragment.observeState()
      │       │
      │       └── sendEffect(effect)   ──► Channel<Effect>  ──► Fragment.collectEffects()
      │
      └── UseCases / Repository
              │
              ├── Room (local DB)
              └── Ktor (remote API)
```

## How to Add a New Feature

1. **Create API module** at `feature-{name}/api/build.gradle.kts` using plugin `myapp.feature.api`.
   - Define `{Name}FeatureEntry : FeatureEntry` with the route key.
   - Define public domain models.

2. **Create Impl module** at `feature-{name}/impl/build.gradle.kts` using plugin `myapp.feature.impl`.
   - Implement the feature entry (`{Name}EntryImpl`).
   - Create Fragment, ViewModel, Adapter (conventional MVI pattern).
   - Add Hilt module binding the entry impl.

3. **Wire in app module**:
   - Add both `:feature-{name}:api` and `:feature-{name}:impl` to `app/build.gradle.kts`.
   - Provide the feature entry in `NavigationModule` or a dedicated DI module.
   - Add the tab/route in the navigation setup.

4. **Write tests** in `feature-{name}/impl/src/test/` extending `ViewModelTest`.

## How to Add a New Core Module

1. Create directory: `core/{name}/`
2. Add `build.gradle.kts` using the appropriate plugin:
   - Pure Kotlin logic → `myapp.kotlin.library`
   - Android library → `myapp.android.library` or `myapp.core.module`
3. Register in `settings.gradle.kts`:
   ```kotlin
   include(":core:{name}")
   ```
4. Add Hilt modules if needed (`@Module @InstallIn(SingletonComponent::class)`).
5. Add to dependent modules' `build.gradle.kts` dependencies.

## Testing Strategy

| Layer | Tool | Pattern |
|-------|------|---------|
| ViewModel | JUnit4 + Turbine + MockK | Extend `ViewModelTest`; use `MainDispatcherRule` |
| Use Case | JUnit4 + MockK | Pure unit tests; mock repositories |
| Repository | JUnit4 + MockK + Room in-memory | Test local/remote sync logic |
| Fragment | Espresso / Fragment testing | UI interaction tests |
| Network | Ktor MockEngine | `createMockHttpClient()` from `core/testing` |
| Database | Room in-memory DB | Use `Room.inMemoryDatabaseBuilder()` |

### ViewModel Test Example

```kotlin
class MyViewModelTest : ViewModelTest() {

    private val repository = mockk<MyRepository>()
    private lateinit var viewModel: MyViewModel

    @Before
    fun setup() {
        viewModel = MyViewModel(repository)
    }

    @Test
    fun `loading state emits correctly`() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)
        }
    }
}
```

## Key Conventions

- **Constructor injection only** — never use `object` singletons unless DI-managed.
- **KSP** for annotation processing (not KAPT).
- **JVM toolchain 21** in all modules.
- **No Compose** — Android Views only.
- **AppResult vs Result**: Use `AppResult<T>` in new domain code (has `Empty` state); `Result<T>` is the legacy type with `Loading` state.
- **DomainException**: Map all errors through `ExceptionMapper.map()` at repository boundaries.
