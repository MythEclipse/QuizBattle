from PIL import Image
import os

# Path to goblin sprites
goblin_folder = r"D:\QuizBattle\app\src\main\res\drawable"

# Get all goblin sprite files
goblin_files = [f for f in os.listdir(goblin_folder) if f.startswith('goblin_') and f.endswith('.png')]

print(f"Found {len(goblin_files)} goblin sprite files")

for filename in goblin_files:
    filepath = os.path.join(goblin_folder, filename)
    
    try:
        # Open image
        img = Image.open(filepath)
        
        # Convert to RGBA if not already
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
        # Get image data
        pixels = img.load()
        width, height = img.size
        
        # Find the actual content bounds (crop transparent areas)
        min_x, min_y = width, height
        max_x, max_y = 0, 0
        
        for y in range(height):
            for x in range(width):
                r, g, b, a = pixels[x, y]
                if a > 0:  # Non-transparent pixel
                    min_x = min(min_x, x)
                    min_y = min(min_y, y)
                    max_x = max(max_x, x)
                    max_y = max(max_y, y)
        
        # If we found content, crop to it
        if max_x > min_x and max_y > min_y:
            # Add small padding if needed
            padding = 2
            min_x = max(0, min_x - padding)
            min_y = max(0, min_y - padding)
            max_x = min(width - 1, max_x + padding)
            max_y = min(height - 1, max_y + padding)
            
            # Crop the image
            cropped = img.crop((min_x, min_y, max_x + 1, max_y + 1))
            
            # Save back to original file
            cropped.save(filepath, 'PNG')
            
            print(f"✓ Cropped {filename}: {width}x{height} -> {cropped.width}x{cropped.height}")
        else:
            print(f"⚠ Skipped {filename}: No content found")
            
    except Exception as e:
        print(f"✗ Error processing {filename}: {e}")

print("\nDone! All goblin sprites have been cropped.")
