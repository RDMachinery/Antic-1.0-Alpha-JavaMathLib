#!/usr/bin/env zsh
# =============================================================================
# build.sh — Compiles Java source files and packages them into a JAR file.
#
# Usage:
#   ./build.sh [options]
#
# Options:
#   -s <dir>    Source root directory       (default: src)
#   -c <dir>    Classes output directory    (default: classes)
#   -o <file>   Output JAR filename         (default: output.jar)
#   -m <class>  Main-Class for manifest     (optional)
#   -h          Show this help message
#
# Examples:
#   ./build.sh
#   ./build.sh -s src -c classes -o mathlib.jar
#   ./build.sh -s src -c classes -o mathlib.jar -m com.mario.mathlib.Main
# =============================================================================

set -euo pipefail

# -----------------------------------------------------------------------------
# Defaults
# -----------------------------------------------------------------------------
SRC_DIR="src"
CLASSES_DIR="classes"
JAR_FILE="antic-1.0.jar"
MAIN_CLASS=""
MANIFEST_FILE="manifest.tmp.mf"

# -----------------------------------------------------------------------------
# Colours for output
# -----------------------------------------------------------------------------
RED="\033[0;31m"
GREEN="\033[0;32m"
YELLOW="\033[0;33m"
CYAN="\033[0;36m"
RESET="\033[0m"

# -----------------------------------------------------------------------------
# Logging helpers
# -----------------------------------------------------------------------------
info()    { print -P "%F{cyan}[INFO]%f  $*" }
success() { print -P "%F{green}[OK]%f    $*" }
warn()    { print -P "%F{yellow}[WARN]%f  $*" }
error()   { print -P "%F{red}[ERROR]%f $*" >&2 }

# -----------------------------------------------------------------------------
# Usage
# -----------------------------------------------------------------------------
usage() {
    print ""
    print "Usage: $0 [options]"
    print ""
    print "  -s <dir>    Source root directory       (default: src)"
    print "  -c <dir>    Classes output directory    (default: classes)"
    print "  -o <file>   Output JAR filename         (default: output.jar)"
    print "  -m <class>  Main-Class for manifest     (optional)"
    print "  -h          Show this help message"
    print ""
    print "Examples:"
    print "  $0"
    print "  $0 -s src -c classes -o mathlib.jar"
    print "  $0 -s src -c classes -o mathlib.jar -m com.mario.mathlib.Main"
    print ""
}

# -----------------------------------------------------------------------------
# Parse arguments
# -----------------------------------------------------------------------------
while getopts ":s:c:o:m:h" opt; do
    case $opt in
        s) SRC_DIR="$OPTARG" ;;
        c) CLASSES_DIR="$OPTARG" ;;
        o) JAR_FILE="$OPTARG" ;;
        m) MAIN_CLASS="$OPTARG" ;;
        h) usage; exit 0 ;;
        :) error "Option -$OPTARG requires an argument."; usage; exit 1 ;;
        \?) error "Unknown option: -$OPTARG"; usage; exit 1 ;;
    esac
done

# -----------------------------------------------------------------------------
# Validate environment
# -----------------------------------------------------------------------------
print ""
info "Checking environment..."

if ! command -v javac &>/dev/null; then
    error "javac not found. Please install a JDK and ensure it is on your PATH."
    exit 1
fi

if ! command -v jar &>/dev/null; then
    error "jar not found. Please install a JDK and ensure it is on your PATH."
    exit 1
fi

JAVA_VERSION=$(javac -version 2>&1)
success "Found $JAVA_VERSION"

# -----------------------------------------------------------------------------
# Validate source directory
# -----------------------------------------------------------------------------
if [[ ! -d "$SRC_DIR" ]]; then
    error "Source directory '$SRC_DIR' does not exist."
    exit 1
fi

# Collect all .java files under the source tree
JAVA_FILES=("${(@f)$(find "$SRC_DIR" -name "*.java" 2>/dev/null)}")

if [[ ${#JAVA_FILES[@]} -eq 0 ]]; then
    error "No .java files found under '$SRC_DIR'."
    exit 1
fi

info "Found ${#JAVA_FILES[@]} source file(s) under '$SRC_DIR'."

# -----------------------------------------------------------------------------
# Prepare classes directory
# -----------------------------------------------------------------------------
if [[ -d "$CLASSES_DIR" ]]; then
    warn "Classes directory '$CLASSES_DIR' already exists — cleaning it."
    rm -rf "$CLASSES_DIR"
fi

mkdir -p "$CLASSES_DIR"
success "Created classes directory: $CLASSES_DIR"

# -----------------------------------------------------------------------------
# Compile
# -----------------------------------------------------------------------------
print ""
info "Compiling sources..."

if javac -d "$CLASSES_DIR" "${JAVA_FILES[@]}"; then
    success "Compilation successful."
else
    error "Compilation failed. See errors above."
    exit 1
fi

# Verify something was actually produced
CLASS_COUNT=$(find "$CLASSES_DIR" -name "*.class" | wc -l | tr -d ' ')
info "Produced $CLASS_COUNT .class file(s)."

# -----------------------------------------------------------------------------
# Build manifest (if a Main-Class was specified)
# -----------------------------------------------------------------------------
if [[ -n "$MAIN_CLASS" ]]; then
    info "Writing manifest with Main-Class: $MAIN_CLASS"
    {
        print "Manifest-Version: 1.0"
        print "Main-Class: $MAIN_CLASS"
        print ""          # manifest requires a trailing newline
    } > "$MANIFEST_FILE"
fi

# -----------------------------------------------------------------------------
# Package into JAR
# -----------------------------------------------------------------------------
print ""
info "Packaging into $JAR_FILE ..."

if [[ -n "$MAIN_CLASS" ]]; then
    jar cvfm "$JAR_FILE" "$MANIFEST_FILE" -C "$CLASSES_DIR" .
    rm -f "$MANIFEST_FILE"
else
    jar cvf "$JAR_FILE" -C "$CLASSES_DIR" .
fi

if [[ $? -eq 0 ]]; then
    success "JAR created: $JAR_FILE"
else
    error "JAR creation failed."
    exit 1
fi

# -----------------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------------
JAR_SIZE=$(du -sh "$JAR_FILE" | cut -f1)
print ""
print "${CYAN}=============================================${RESET}"
print "${GREEN}  Build complete!${RESET}"
print "${CYAN}=============================================${RESET}"
print "  Source dir  : $SRC_DIR"
print "  Classes dir : $CLASSES_DIR"
print "  JAR file    : $JAR_FILE ($JAR_SIZE)"
[[ -n "$MAIN_CLASS" ]] && print "  Main-Class  : $MAIN_CLASS"
print ""
info "JAR contents:"
jar tf "$JAR_FILE"
print ""
