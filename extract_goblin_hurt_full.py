from PIL import Image
import os

# Source and destination
source_folder = r"D:\QuizBattle\app\src\main\res\drawable\Goblin\Atk2"
dest_folder = r"D:\QuizBattle\app\src\main\res\drawable"

# Get frames 0100-0110 from Atk2 for hurt animation
all_files = sorted([f for f in os.listdir(source_folder) if f.endswith('.png')])
hurt_files = [f for f in all_files if f.startswith('01') and int(f[0:4]) <= 110]

print(f"Extracting {len(hurt_files)} frames from Atk2 (0100-0110) for hurt animation...\n")

# Uniform canvas size
max_width = 201
max_height = 201

print(f"Using uniform canvas: {max_width}x{max_height}\n")

# Delete old hurt frames first
old_hurt = [f for f in os.listdir(dest_folder) if f.startswith('goblin_hurt_')]
for old_file in old_hurt:
    os.remove(os.path.join(dest_folder, old_file))
    print(f"Deleted old {old_file}")

print()

# Process each hurt frame
for i, filename in enumerate(hurt_files, start=1):
    source_path = os.path.join(source_folder, filename)
    dest_filename = f"goblin_hurt_{i:04d}.png"
    dest_path = os.path.join(dest_folder, dest_filename)
    
    try:
        # Open and process image
        img = Image.open(source_path)
        
        # Convert to RGBA
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
        # Crop to content
        bbox = img.getbbox()
        if bbox:
            cropped = img.crop(bbox)
            
            # Create uniform canvas
            canvas = Image.new('RGBA', (max_width, max_height), (0, 0, 0, 0))
            
            # Center horizontally, align to bottom
            x_offset = (max_width - cropped.width) // 2
            y_offset = max_height - cropped.height
            
            # Paste onto canvas
            canvas.paste(cropped, (x_offset, y_offset), cropped)
            
            # Save
            canvas.save(dest_path, 'PNG')
            
            print(f"✓ Created {dest_filename} from {filename}")
        else:
            print(f"⚠ Skipped {filename}: No content")
            
    except Exception as e:
        print(f"✗ Error processing {filename}: {e}")

print(f"\nDone! Created {len(hurt_files)} hurt animation frames (0100-0110).")
