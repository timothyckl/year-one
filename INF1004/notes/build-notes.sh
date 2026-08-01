#!/bin/bash

# build all lecture notes
if [ -d "tex/notes" ] && [ -n "$(ls -A tex/notes/*.tex 2>/dev/null)" ]; then
  echo "Building lecture notes..."
  cd tex/notes
  for f in lecture-*.tex; do
    [ -e "$f" ] || continue
    echo "  Compiling $f..."
    tectonic "$f"
  done
  mv lecture-*.pdf ../../pdf/notes/ 2>/dev/null
  cd ../..
fi

# build all tutorial notes
if [ -d "tex/tutorials" ] && [ -n "$(ls -A tex/tutorials/*.tex 2>/dev/null)" ]; then
  echo "Building tutorial notes..."
  cd tex/tutorials
  for f in tutorial-*.tex; do
    [ -e "$f" ] || continue
    echo "  Compiling $f..."
    tectonic "$f"
  done
  mv tutorial-*.pdf ../../pdf/tutorials/ 2>/dev/null
  cd ../..
fi

echo "Build complete!"
