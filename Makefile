.PHONY: clean build all

VERSION := $(shell ./version.sh simple)

all: build

clean:
	@echo "Cleaning..."
	@./gradlew clean

test:
	@echo "Running tests of version ${VERSION}..."
	@./gradlew test

build:
	@echo "Building version ${VERSION}..."
	@./gradlew build

publish:
	@echo "Publishing version ${VERSION} to Sonatype OSS repository..."
	@./gradlew --no-configuration-cache publishToSonatype closeAndReleaseSonatypeStagingRepository
