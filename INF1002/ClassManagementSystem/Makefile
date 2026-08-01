# Compiler and flags
CC := gcc
MINGW := x86_64-w64-mingw32-gcc
CFLAGS := -Iinclude -Wall -Wextra -g
LDFLAGS :=              # e.g. -lm if you need libm

# Directories
SRC_DIR := src
BUILD_DIR := build

# Sources and header files
CMD_SRCS := $(wildcard $(SRC_DIR)/commands/*.c)
SRCS := $(wildcard $(SRC_DIR)/*.c) $(CMD_SRCS)
HDRS := $(wildcard include/*.h include/commands/*.h)
LIB_SRCS := $(filter-out $(SRC_DIR)/main.c,$(SRCS))

# Tests
TEST_DIR := tests
TEST_SRCS := $(filter-out $(TEST_DIR)/test_utils.c,$(wildcard $(TEST_DIR)/*.c))
TEST_UTILS := $(TEST_DIR)/test_utils.c
TEST_BINS := $(patsubst $(TEST_DIR)/%.c,$(BUILD_DIR)/%,$(TEST_SRCS))

# Executable
TARGET := $(BUILD_DIR)/main

.PHONY: all run tests test test-all clean build-macos build-windows build-all

# Default target
all: $(TARGET)

# Single-step build: compile & link directly to build/main
$(TARGET): $(SRCS) $(HDRS) | $(BUILD_DIR)
	$(CC) $(CFLAGS) $(SRCS) -o $@ $(LDFLAGS)

# Cross-platform build targets
build-macos: $(BUILD_DIR)
	$(CC) $(CFLAGS) $(SRCS) -o $(BUILD_DIR)/main $(LDFLAGS)

build-windows: $(BUILD_DIR)
	$(MINGW) $(CFLAGS) $(SRCS) -o $(BUILD_DIR)/main.exe $(LDFLAGS)

build-all: build-macos build-windows

# Build all test harnesses
tests: $(TEST_BINS)

$(BUILD_DIR)/%: $(TEST_DIR)/%.c $(TEST_UTILS) $(LIB_SRCS) $(HDRS) | $(BUILD_DIR)
	$(CC) $(CFLAGS) $(LIB_SRCS) $(TEST_UTILS) $< -o $@ $(LDFLAGS)

# Run all tests
test: tests
	@echo "╔═══════════════════════════════════════════════════════════╗"
	@echo "║              Running All Test Suites                      ║"
	@echo "╚═══════════════════════════════════════════════════════════╝"
	@for test in $(TEST_BINS); do \
		if [ -f $$test ]; then \
			./$$test || exit 1; \
		fi; \
	done
	@echo "\n╔═══════════════════════════════════════════════════════════╗"
	@echo "║              All Tests Completed Successfully             ║"
	@echo "╚═══════════════════════════════════════════════════════════╝\n"

# Alias for running all tests
test-all: test

# Ensure build directory exists
$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

# Run the executable
run: $(TARGET)
	./$(TARGET) 

# Clean
clean:
	rm -rf $(BUILD_DIR)
