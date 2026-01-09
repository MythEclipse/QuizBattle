#!/bin/bash

# Source image
SOURCE_IMAGE="/home/asephs/.gemini/antigravity/brain/de717da1-5774-41de-a20d-3d9f03938311/quiz_battle_icon_1767963195182.png"
RES_DIR="app/src/main/res"

# Standard Icon (Square/Adaptive)
echo "Generating standard icons..."
magick "$SOURCE_IMAGE" -resize 48x48 "$RES_DIR/mipmap-mdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 72x72 "$RES_DIR/mipmap-hdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 96x96 "$RES_DIR/mipmap-xhdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 144x144 "$RES_DIR/mipmap-xxhdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 192x192 "$RES_DIR/mipmap-xxxhdpi/ic_launcher.png"

# Round Icon
echo "Generating round icons..."
# Create a round mask and apply it
magick "$SOURCE_IMAGE" \( +clone -alpha transparent -draw "circle %[fx:w/2],%[fx:h/2] %[fx:w/2],0" \) -compose DstIn -composite -resize 48x48 "$RES_DIR/mipmap-mdpi/ic_launcher_round.png"
magick "$SOURCE_IMAGE" \( +clone -alpha transparent -draw "circle %[fx:w/2],%[fx:h/2] %[fx:w/2],0" \) -compose DstIn -composite -resize 72x72 "$RES_DIR/mipmap-hdpi/ic_launcher_round.png"
magick "$SOURCE_IMAGE" \( +clone -alpha transparent -draw "circle %[fx:w/2],%[fx:h/2] %[fx:w/2],0" \) -compose DstIn -composite -resize 96x96 "$RES_DIR/mipmap-xhdpi/ic_launcher_round.png"
magick "$SOURCE_IMAGE" \( +clone -alpha transparent -draw "circle %[fx:w/2],%[fx:h/2] %[fx:w/2],0" \) -compose DstIn -composite -resize 144x144 "$RES_DIR/mipmap-xxhdpi/ic_launcher_round.png"
magick "$SOURCE_IMAGE" \( +clone -alpha transparent -draw "circle %[fx:w/2],%[fx:h/2] %[fx:w/2],0" \) -compose DstIn -composite -resize 192x192 "$RES_DIR/mipmap-xxxhdpi/ic_launcher_round.png"

# Foreground for adaptive icon (optional, using full image for now)
echo "Generating adaptive foregrounds..."
magick "$SOURCE_IMAGE" -resize 108x108 "$RES_DIR/mipmap-mdpi/ic_launcher_foreground.png"
magick "$SOURCE_IMAGE" -resize 162x162 "$RES_DIR/mipmap-hdpi/ic_launcher_foreground.png"
magick "$SOURCE_IMAGE" -resize 216x216 "$RES_DIR/mipmap-xhdpi/ic_launcher_foreground.png"
magick "$SOURCE_IMAGE" -resize 324x324 "$RES_DIR/mipmap-xxhdpi/ic_launcher_foreground.png"
magick "$SOURCE_IMAGE" -resize 432x432 "$RES_DIR/mipmap-xxxhdpi/ic_launcher_foreground.png"

# Remove interfering adaptive icon XMLs (Force use of PNGs)
echo "Removing conflicting adaptive icon XMLs..."
rm -rf "$RES_DIR/mipmap-anydpi-v26"
rm -rf "$RES_DIR/mipmap-anydpi"

echo "Icons updated successfully!"
