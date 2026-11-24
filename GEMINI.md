# G-Store Workspace Rules & Verification Protocol

## Mandatory Automatic Verification Workflow

Whenever you modify or create code files (`.kt`, `.java`, `.xml`, `.gradle.kts`, `gradle.properties`, etc.):

### 1. Execute Verification
Always run the verification suite before concluding your turn:
- **Comprehensive Verification:** Run `./scripts/verify.sh` (or `./gradlew assembleDebug testDebugUnitTest`).
- **Targeted Verification (during active iteration):** Run `./scripts/verify.sh --test <TestClassName>` or `./gradlew testDebugUnitTest --tests "*<TestClassName>*"`.

### 2. The Build → Test → Diagnose → Fix Loop
1. **Build the Android project** (`./gradlew assembleDebug`).
2. **Run relevant unit tests** (`./gradlew testDebugUnitTest`).
3. **If build or tests fail:** Carefully inspect compiler warnings, KSP generator outputs, stack traces, and test assertions.
4. **Fix the underlying issue:** Resolve the root cause in application logic, data layer, UI components, or configuration.
5. **Build and test again.**
6. **Continue this loop until both the build and tests pass 100% cleanly.**

### 3. Strict Rules & Constraints
- **Never stop after a failure:** Do not terminate the session or return control to the user while builds or tests are broken.
- **No Error Suppression:** Never bypass errors or make tests pass by:
  - Adding empty `try { ... } catch (e: Exception) {}` blocks to swallow errors.
  - Adding `@Ignore` or commenting out failing test methods/assertions.
  - Hardcoding dummy values or disabling validation logic.
  - Modifying tests to assert incorrect behavior rather than fixing the code under test.
- **Documentation & Reporting:** In your final response, explicitly provide:
  1. A structured summary of all files created or modified.
  2. Clear confirmation of whether the final build (`assembleDebug`) and tests (`testDebugUnitTest`) passed.
