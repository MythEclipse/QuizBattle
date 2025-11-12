from PIL import Image

# Crop Soldier sprite (first frame 100x100 from 600x100 sheet)
soldier = Image.open(r"d:\QuizBattle\app\src\main\res\drawable\Tiny RPG Character Asset Pack v1.03 -Free Soldier&Orc\Characters(100x100)\Soldier\Soldier\Soldier-Idle.png")
soldier_cropped = soldier.crop((0, 0, 100, 100))
soldier_cropped.save(r"d:\QuizBattle\app\src\main\res\drawable\player_avatar.png")
print(f"Soldier cropped: {soldier_cropped.size}")

# Crop Orc sprite (first frame 100x100 from sprite sheet)
orc = Image.open(r"d:\QuizBattle\app\src\main\res\drawable\Tiny RPG Character Asset Pack v1.03 -Free Soldier&Orc\Characters(100x100)\Orc\Orc\Orc-Idle.png")
orc_cropped = orc.crop((0, 0, 100, 100))
orc_cropped.save(r"d:\QuizBattle\app\src\main\res\drawable\bot_avatar.png")
print(f"Orc cropped: {orc_cropped.size}")

print("Sprites cropped successfully!")
