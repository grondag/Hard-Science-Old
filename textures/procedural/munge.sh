rm $1_*.jpg >& /dev/null
convert "$1_*.png" -quality 100 -define colorspace:auto-grayscale=off $1_%d.jpg
#convert "$1_*.png" -quality 100 -resize 50% -define colorspace:auto-grayscale=off $1_b_%d.jpg
#convert "$1_*.png" -quality 100 -resize 25% -define colorspace:auto-grayscale=off $1_c_%d.jpg
#convert "$1_*.png" -quality 100 -resize 12.5% -define colorspace:auto-grayscale=off $1_d_%d.jpg
#convert "$1_*.png" -quality 100 -resize 6.25% -define colorspace:auto-grayscale=off $1_e_%d.jpg
/Applications/ImageOptim.app/Contents/MacOS/ImageOptim *.jpg
mv $1_*.jpg ../../../src/main/resources/assets/adversity/textures/blocks
