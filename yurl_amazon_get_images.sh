cat - | perl -pe 's{.*<img}{<img}g' | perl -pe 's{</a>.*}{}g' | perl -pe 's{.*src=.}{}g' | perl -pe 's{.jpg.*}{.jpg}g' | grep -v pagepeeker | grep -v 'data:image' | perl -pe 's{^(.*)$}{"$1"}g' | perl -pe 's{" width=".*}{}g' 
