cat heap.txt | groovy csvHeap2listReversed.groovy | sh ~/bin/httpify.sh | sh ~/bin/htmlify.sh | tee out.html
