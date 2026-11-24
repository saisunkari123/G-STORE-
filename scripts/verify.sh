#!/usr/bin/env bash
# ==============================================================================
# G-Store Android Automatic Verification Script
# ==============================================================================
# Automates the verification workflow:
# 1. Build the Android debug APK (:app:assembleDebug)
# 2. Run unit tests (:app:testDebugUnitTest)
# 3. Formatted status reporting and error diagnostics
# ==============================================================================

set -euo pipefail

# Determine project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${PROJECT_ROOT}"

# Color formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Default options
RUN_BUILD=true
RUN_TESTS=true
TEST_FILTER=""

print_usage() {
    echo -e "${BOLD}Usage:${NC} $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -t, --test <filter>    Run specific test class or method (e.g., OrderCalculationTest)"
    echo "  -b, --build-only       Run only Android build (assembleDebug)"
    echo "      --test-only        Run only unit tests (testDebugUnitTest)"
    echo "  -h, --help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                   # Run full build and all unit tests"
    echo "  $0 --test OrderCalculationTest       # Run build and specific test"
    echo "  $0 --test-only -t DeliveryLocationTest # Run only specific unit test"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        -t|--test)
            TEST_FILTER="$2"
            shift 2
            ;;
        -b|--build-only)
            RUN_BUILD=true
            RUN_TESTS=false
            shift
            ;;
        --test-only)
            RUN_BUILD=false
            RUN_TESTS=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            print_usage
            exit 1
            ;;
    esac
done

echo -e "${CYAN}${BOLD}======================================================${NC}"
echo -e "${CYAN}${BOLD}       G-Store Android Verification Pipeline          ${NC}"
echo -e "${CYAN}${BOLD}======================================================${NC}"
echo -e "${BLUE}Project root:${NC} ${PROJECT_ROOT}"

# Check for gradlew
if [[ ! -f "${PROJECT_ROOT}/gradlew" ]]; then
    echo -e "${RED}Error: gradlew not found in ${PROJECT_ROOT}${NC}"
    exit 1
fi

chmod +x "${PROJECT_ROOT}/gradlew"

# Ensure valid JAVA_HOME is configured
if [[ -z "${JAVA_HOME:-}" ]] || [[ ! -d "${JAVA_HOME}" ]]; then
    if [[ -d "/Applications/Unity/Hub/Editor/6000.3.22f1/PlaybackEngines/AndroidPlayer/OpenJDK" ]]; then
        export JAVA_HOME="/Applications/Unity/Hub/Editor/6000.3.22f1/PlaybackEngines/AndroidPlayer/OpenJDK"
    fi
fi

START_TIME=$(date +%s)

# Step 1: Build Android Project
if [[ "${RUN_BUILD}" == true ]]; then
    echo -e "\n${YELLOW}${BOLD}[1/2] Building Android Debug APK (:app:assembleDebug)...${NC}"
    if ./gradlew assembleDebug; then
        echo -e "${GREEN}✔ Android Build Succeeded.${NC}"
    else
        echo -e "\n${RED}${BOLD}✖ Android Build FAILED.${NC}"
        echo -e "${YELLOW}Please inspect compiler, KSP, and resource errors above and fix them.${NC}"
        exit 1
    fi
else
    echo -e "\n${BLUE}[1/2] Skipping build step (--test-only selected)${NC}"
fi

# Step 2: Run Unit Tests
if [[ "${RUN_TESTS}" == true ]]; then
    if [[ -n "${TEST_FILTER}" ]]; then
        echo -e "\n${YELLOW}${BOLD}[2/2] Running Unit Tests matching '${TEST_FILTER}'...${NC}"
        TEST_CMD=("./gradlew" "testDebugUnitTest" "--tests" "*${TEST_FILTER}*")
    else
        echo -e "\n${YELLOW}${BOLD}[2/2] Running All Unit Tests (:app:testDebugUnitTest)...${NC}"
        TEST_CMD=("./gradlew" "testDebugUnitTest")
    fi

    if "${TEST_CMD[@]}"; then
        echo -e "${GREEN}✔ Unit Tests PASSED.${NC}"
    else
        echo -e "\n${RED}${BOLD}✖ Unit Tests FAILED.${NC}"
        echo -e "${YELLOW}Please inspect the test failure stack trace above and resolve the root cause.${NC}"
        exit 1
    fi
else
    echo -e "\n${BLUE}[2/2] Skipping test step (--build-only selected)${NC}"
fi

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo -e "\n${GREEN}${BOLD}======================================================${NC}"
echo -e "${GREEN}${BOLD}  ✔ ALL VERIFICATION CHECKS PASSED (in ${DURATION}s)  ${NC}"
echo -e "${GREEN}${BOLD}======================================================${NC}"
exit 0
