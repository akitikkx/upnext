# CI/CD Github Actions Optimization

The current `pull_request.yml` spins up 7 disparate GitHub Action virtual environments (Runners) to execute Gradle tasks in parallel (`test`, `build`, `ktlint`, `lint`, `detekt`, `build-release`, `ui-tests`). 

Because they run in isolated environments, **every single job must download dependencies, spin up the Gradle Daemon, setup the Android SDK, and re-compile the entire application codebase.** This exponentially inflates GitHub minute consumption (e.g. `testDebugUnitTest`, `assembleDebug` and `lintDebug` all independently compile `compileDebugKotlin`, compounding 3x the computational cost).

## Proposed Changes

### [MODIFY] `.github/workflows/pull_request.yml`
- **Job Consolidation:** Merge the isolated jobs `test`, `build`, `ktlint`, `lint`, `detekt`, and `build-release` into a single unified `verify` job.
- **Dependency Execution Graph:** We will run `./gradlew ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug assembleRelease --build-cache --continue`. Gradle's internal dependency graph will guarantee that `compileDebugKotlin` only executes **once**, dramatically speeding up the workflow. The `--continue` flag ensures all static checks run, even if one fails initially, preventing hidden failures.
- **Superior Caching:** Swap out the legacy `actions/cache` and custom `chmod +x ./gradlew` steps for the officially maintained `gradle/actions/setup-gradle@v3` Action. This automatically caches Daemon state, build cache, and dependencies optimally.
- **Isolated UI Tests:** The InstrumentedTests (`ui-tests`) utilizing the Pixel emulator will remain as their own dedicated isolated job, maintaining their massive timeout allowance and KVM hardware acceleration rules securely undisturbed.

## User Review Required

> [!IMPORTANT]
> Because the formatting, linting, and unit-test checks are combined, they will no longer appear as 6 separate individual `Checkmarks` under the GitHub `Merge` dialog. They will simply be one major check known as `Verify Code and Build`. 
> 
> Are you okay making this tradeoff for the significant decrease in GitHub Minutes consumption?

## Verification Plan
1. Push the consolidated YAML formatting check directly to a new tracking branch.
2. Monitor Github Actions to affirm minute expenditures have drastically dropped.
