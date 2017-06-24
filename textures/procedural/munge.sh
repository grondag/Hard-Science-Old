convert "$1_*.png" -quality 100 $1_a_%d.jpg
convert "$1_*.png" -quality 100 -resize 50%  $1_b%d.jpg
convert "$1_*.png" -quality 100 -resize 25%  $1_c%d.jpg
convert "$1_*.png" -quality 100 -resize 12.5% $1_d%d.jpg
convert "$1_*.png" -quality 100 -resize 6.25% $1_e%d.jpg
/Applications/ImageOptim.app/Contents/MacOS/ImageOptim *.jpg
cp "$1_*.jpg" ../../../src/main/resources/assets/adversity/textures/blocks

