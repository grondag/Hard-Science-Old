rm "$1_anim.png" >& /dev/null
rm "$1_anim_low.png" >& /dev/null
magick "$1_*.png" -append "$1_anim.png"
convert "$1_anim.png"  -resize 50%  "$1_anim_low.png"
mv "$1_anim.png" ../../../src/main/resources/assets/adversity/textures/blocks
mv "$1_anim_low.png" ../../../src/main/resources/assets/adversity/textures/blocks
cp $1*0000.png "../../../src/main/resources/assets/adversity/textures/blocks/$1_static.png"
