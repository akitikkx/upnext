from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os
import glob

base_dir = "fastlane/metadata/android/en-US"
background_path = os.path.join(base_dir, "background.png")
title_font_path = "/System/Library/Fonts/SFCompact.ttf"
keyword_font_path = "/System/Library/Fonts/SFCompact.ttf"

TITLES = {
    "01_dashboard": "Your Personalized Dashboard",
    "02_show_detail": "Track Your Favorites",
    "03_dashboard_recent": "Resume Watching",
    "04_explore": "Discover New Shows",
    "05_schedule": "Never Miss An Episode",
    "06_search": "Find Exactly What You Want"
}

DISCLAIMER = "TV SHOW METADATA AND ARTWORK PROVIDED BY TVMAZE.COM UNDER CC BY-SA 4.0\n* FREE TRAKT.TV ACCOUNT REQUIRED FOR TRACKING AND PERSONALIZATION FEATURES."

def add_corners(im, rad):
    circle = Image.new('L', (rad * 2, rad * 2), 0)
    draw = ImageDraw.Draw(circle)
    draw.ellipse((0, 0, rad * 2 - 1, rad * 2 - 1), fill=255)
    alpha = Image.new('L', im.size, 255)
    w, h = im.size
    alpha.paste(circle.crop((0, 0, rad, rad)), (0, 0))
    alpha.paste(circle.crop((0, rad, rad, rad * 2)), (0, h - rad))
    alpha.paste(circle.crop((rad, 0, rad * 2, rad)), (w - rad, 0))
    alpha.paste(circle.crop((rad, rad, rad * 2, rad * 2)), (w - rad, h - rad))
    im.putalpha(alpha)
    return im

def get_fitted_font(text, max_width, start_size, font_path):
    size = start_size
    while size > 20:
        try:
            font = ImageFont.truetype(font_path, size)
        except:
            return ImageFont.load_default()
        
        # Use getsize if available, else getbbox
        try:
            w, h = font.getsize(text)
        except AttributeError:
            bbox = font.getbbox(text)
            w = bbox[2] - bbox[0]
            
        if w < max_width:
            return font
        size -= 4
    return ImageFont.truetype(font_path, size)

def compose_image(img_path, output_path, title, keyword, is_tablet):
    print(f"Processing {os.path.basename(img_path)}...")
    bg_width = 2732 if is_tablet else 1600
    bg_height = 2048 if is_tablet else 3200
    
    bg = Image.open(background_path).convert("RGBA")
    try:
        resample = Image.Resampling.LANCZOS
    except AttributeError:
        resample = Image.LANCZOS
        
    bg = bg.resize((bg_width, bg_height), resample)
    
    screenshot = Image.open(img_path).convert("RGBA")
    
    # Scale screenshot to fit well within background
    target_height = int(bg_height * 0.70)
    aspect_ratio = screenshot.width / screenshot.height
    target_width = int(target_height * aspect_ratio)
    screenshot = screenshot.resize((target_width, target_height), resample)
    
    # Add rounded corners to look like a modern device
    rad = 60 if is_tablet else 80
    screenshot = add_corners(screenshot, rad=rad)
    
    # Draw drop shadow
    shadow = Image.new('RGBA', bg.size, (0,0,0,0))
    shadow_draw = ImageDraw.Draw(shadow)
    shadow_offset = 30
    shadow_x = (bg_width - target_width) // 2
    shadow_y = (bg_height - target_height) // 2 + (40 if is_tablet else 120)
    
    shadow_draw.rectangle(
        [shadow_x, shadow_y, shadow_x + target_width, shadow_y + target_height], 
        fill=(0,0,0,150)
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(50))
    bg = Image.alpha_composite(bg, shadow)
    
    # Paste screenshot
    bg.paste(screenshot, (shadow_x, shadow_y), screenshot)
    
    # Draw text
    draw = ImageDraw.Draw(bg)
    
    # Title at top (dynamically scale font to fit)
    max_title_width = bg_width - 160
    title_font = get_fitted_font(title, max_title_width, 160 if is_tablet else 180, title_font_path)
    
    try:
        title_w, _ = title_font.getsize(title)
    except AttributeError:
        bbox = draw.textbbox((0, 0), title, font=title_font)
        title_w = bbox[2] - bbox[0]
        
    title_x = (bg_width - title_w) // 2
    title_y = 120 if is_tablet else 250
    draw.text((title_x, title_y), title, fill=(255, 255, 255), font=title_font)
    
    # Keyword at bottom
    kw_y = shadow_y + target_height + (60 if is_tablet else 140)
    lines = keyword.split('\n')
    kw_font = get_fitted_font(lines[0], bg_width - 100, 45 if is_tablet else 55, keyword_font_path)
    
    for line in lines:
        try:
            kw_w, kw_h = kw_font.getsize(line)
        except AttributeError:
            bbox = draw.textbbox((0, 0), line, font=kw_font)
            kw_w = bbox[2] - bbox[0]
            kw_h = bbox[3] - bbox[1]
            
        kw_x = (bg_width - kw_w) // 2
        draw.text((kw_x, kw_y), line, fill=(200, 200, 200), font=kw_font)
        kw_y += kw_h + 20
        
    bg = bg.convert("RGB")
    bg.save(output_path, quality=95)

for folder in ['phoneScreenshots', 'tenInchScreenshots']:
    is_tablet = (folder == 'tenInchScreenshots')
    dir_path = os.path.join(base_dir, "images", folder)
    if not os.path.exists(dir_path): continue
    
    for img_path in glob.glob(os.path.join(dir_path, "*.png")):
        if "_framed" in img_path or "_trimmed" in img_path: continue
        if "background" in img_path: continue
        if "01_dashboard.png" in os.path.basename(img_path): continue
        
        filename = os.path.basename(img_path)
        
        title = "Upnext"
        for k, v in TITLES.items():
            if k in filename:
                title = v
                break
                
        output_path = os.path.join(dir_path, f"{os.path.splitext(filename)[0]}_framed.png")
        compose_image(img_path, output_path, title, DISCLAIMER, is_tablet)

print("Composition complete!")
