# Makefile for Timeliner Android App
# This provides convenient shortcuts for common Gradle tasks

.PHONY: clean assembleDebug assembleRelease test lint build help

# Default target - show help
help:
	@echo "Timeliner Android App - Available make targets:"
	@echo ""
	@echo "  make clean           - Clean build artifacts"
	@echo "  make assembleDebug   - Build debug APK"
	@echo "  make assembleRelease - Build release APK (unsigned)"
	@echo "  make test            - Run unit tests"
	@echo "  make lint            - Run Android lint checks"
	@echo "  make build           - Build everything and run tests"
	@echo ""
	@echo "Output locations:"
	@echo "  Debug APK:   app/build/outputs/apk/debug/app-debug.apk"
	@echo "  Release APK: app/build/outputs/apk/release/app-release-unsigned.apk"

# Clean build artifacts
clean:
	./gradlew clean

# Build debug APK
assembleDebug:
	./gradlew :app:assembleDebug

# Build release APK (unsigned)
assembleRelease:
	./gradlew :app:assembleRelease

# Run unit tests
test:
	./gradlew test

# Run Android lint
lint:
	./gradlew lint

# Build everything and run tests
build:
	./gradlew build
