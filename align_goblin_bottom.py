from PIL import Image
import os

# Path to goblin sprites
goblin_folder = r"D:\QuizBattle\app\src\main\res\drawable"

# Get all goblin sprite files
goblin_files = [f for f in os.listdir(goblin_folder) if f.startswith('goblin_') and f.endswith('.png')]

print(f"Found {len(goblin_files)} goblin sprite files")

# First pass: find the maximum dimensions
max_width = 0
max_height = 0

for filename in goblin_files:
    filepath = os.path.join(goblin_folder, filename)
    img = Image.open(filepath)
    max_width = max(max_width, img.width)
    max_height = max(max_height, img.height)

print(f"\nMax dimensions found: {max_width}x{max_height}")

# Use a standard canvas size
canvas_size = 227
print(f"Using canvas size: {canvas_size}x{canvas_size}")

# Second pass: resize all sprites to uniform canvas with bottom-aligned content (NO padding)
for filename in goblin_files:
    filepath = os.path.join(goblin_folder, filename)
    
    try:
        # Open original image
        img = Image.open(filepath)
        original_size = img.size
        
        # Create new transparent canvas
        canvas = Image.new('RGBA', (canvas_size, canvas_size), (0, 0, 0, 0))
        
        # Calculate position to center horizontally and align to BOTTOM (no padding)
        x_offset = (canvas_size - img.width) // 2
        y_offset = canvas_size - img.height  # Align to absolute bottom
        
        # Paste original image onto canvas
        canvas.paste(img, (x_offset, y_offset), img)
        
        # Save back
        canvas.save(filepath, 'PNG')
        
        print(f"✓ Aligned {filename}: {original_size[0]}x{original_size[1]} -> {canvas_size}x{canvas_size} (bottom aligned)")
        
    except Exception as e:
        print(f"✗ Error processing {filename}: {e}")

print(f"\nDone! All goblin sprites are now {canvas_size}x{canvas_size} with bottom-aligned content (no padding).")
