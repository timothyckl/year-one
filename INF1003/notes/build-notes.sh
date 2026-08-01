#!/bin/bash
cd tex
for f in tutorial-*.tex; do
  tectonic "$f"
done
mv tutorial-*.pdf ../pdf/
