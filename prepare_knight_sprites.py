from PIL import Image

# Crop Knight Idle sprite (first frame from sprite sheet)
knight_idle = Image.open(r"d:\QuizBattle\app\src\main\res\drawable\knight-character-sprites-pixel-art\Knight_1\Idle.png")
print(f"Knight Idle original: {knight_idle.size}")

# Each frame is approximately 29 pixels wide (290/10 frames)
frame_width = knight_idle.width // 10
frame_height = knight_idle.height

# Crop first frame
knight_idle_frame = knight_idle.crop((0, 0, frame_width, frame_height))
knight_idle_frame.save(r"d:\QuizBattle\app\src\main\res\drawable\player_avatar.png")
print(f"Knight Idle frame saved: {knight_idle_frame.size}")

# Crop Knight Hurt sprite for damage animation
knight_hurt = Image.open(r"d:\QuizBattle\app\src\main\res\drawable\knight-character-sprites-pixel-art\Knight_1\Hurt.png")
print(f"Knight Hurt original: {knight_hurt.size}")
hurt_frame_width = knight_hurt.width // 3  # Hurt animation usually has 3 frames
knight_hurt_frame = knight_hurt.crop((0, 0, hurt_frame_width, knight_hurt.height))
knight_hurt_frame.save(r"d:\QuizBattle\app\src\main\res\drawable\player_hurt.png")
print(f"Knight Hurt frame saved: {knight_hurt_frame.size}")

# Crop Knight Attack sprite for correct answer
knight_attack = Image.open(r"d:\QuizBattle\app\src\main\res\drawable\knight-character-sprites-pixel-art\Knight_1\Attack 1.png")
print(f"Knight Attack original: {knight_attack.size}")
attack_frame_width = knight_attack.width // 4  # Attack animation usually has 4 frames
knight_attack_frame = knight_attack.crop((0, 0, attack_frame_width, knight_attack.height))
knight_attack_frame.save(r"d:\QuizBattle\app\src\main\res\drawable\player_attack.png")
print(f"Knight Attack frame saved: {knight_attack_frame.size}")

# Crop Knight Defend sprite
knight_defend = Image.open(r"d:\QuizBattle\app\src\main\res\drawable\knight-character-sprites-pixel-art\Knight_1\Defend.png")
print(f"Knight Defend original: {knight_defend.size}")
defend_frame_width = knight_defend.width // 2
knight_defend_frame = knight_defend.crop((0, 0, defend_frame_width, knight_defend.height))
knight_defend_frame.save(r"d:\QuizBattle\app\src\main\res\drawable\player_defend.png")
print(f"Knight Defend frame saved: {knight_defend_frame.size}")

print("\nAll knight sprites prepared successfully!")
