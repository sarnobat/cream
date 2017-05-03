# (instead of diff --line-format or --left-column which I haven't figured out how to get working)
comm -2 -3  <(cat heap.txt | perl -pe 's{.*","}{"}g' | sort) <(cat heap.txt | perl -pe 's{",".*}{"}g' | sort)
