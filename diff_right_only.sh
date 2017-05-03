comm -1 -3  <(cat heap.txt | perl -pe 's{.*","}{"}g' | sort) <(cat heap.txt | perl -pe 's{",".*}{"}g' | sort)
