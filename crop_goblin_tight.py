from PIL import Image
import os

# Path to goblin sprites
goblin_folder = r"D:\QuizBattle\app\src\main\res\drawable"

# Get all goblin sprite files
goblin_files = [f for f in os.listdir(goblin_folder) if f.startswith('goblin_') and f.endswith('.png')]

print(f"Found {len(goblin_files)} goblin sprite files")
print("Cropping ALL transparent pixels (including bottom)...\n")

# Process each sprite - crop to exact content bounds
for filename in goblin_files:
    filepath = os.path.join(goblin_folder, filename)
    
    try:
        # Open image
        img = Image.open(filepath)
        
        # Convert to RGBA if not already
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
        # Get bounding box of non-transparent pixels
        bbox = img.getbbox()
        
        if bbox:
            # Crop to exact content (NO padding at all)
            cropped = img.crop(bbox)
            
            # Save back
            cropped.save(filepath, 'PNG')
            
            print(f"✓ Cropped {filename}: {img.width}x{img.height} -> {cropped.width}x{cropped.height}")
        else:
            print(f"⚠ Skipped {filename}: No content found")
            
    except Exception as e:
        print(f"✗ Error processing {filename}: {e}")

print("\nDone! All goblin sprites now have NO transparent padding (tight crop like knight).")
