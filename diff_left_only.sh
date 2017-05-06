#!/bin/bash
# (instead of diff --line-format or --left-column which I haven't figured out how to get working)
comm -2 -3  <(cat "$1" | perl -pe 's{.*","}{"}g' | sort) <(cat "$1" | perl -pe 's{",".*}{"}g' | sort)
