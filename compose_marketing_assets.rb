require 'fileutils'

def parse_strings(filepath)
  # Basic parsing for .strings file (format: "key" = "value";)
  return {} unless File.exist?(filepath)
  content = File.read(filepath, encoding: 'UTF-8').encode('UTF-8', invalid: :replace, undef: :replace, replace: '?')
  hash = {}
  content.each_line do |line|
    if line =~ /"([^"]+)"\s*=\s*"(.*)";/m
      key = $1
      val = $2.gsub('\n', "\n")
      hash[key] = val
    end
  end
  hash
end

base_dir = "fastlane/metadata/android/en-US"
title_strings = parse_strings("#{base_dir}/title.strings")
keyword_strings = parse_strings("#{base_dir}/keyword.strings")
background = "#{base_dir}/background.png"
title_font = "fastlane/fonts/Inter-Bold.ttf"
keyword_font = "fastlane/fonts/Inter-SemiBold.ttf"

def composite_image(screenshot, output, title, keyword, bg, title_font, kw_font, is_tablet)
  bg_width = is_tablet ? 2732 : 1600
  bg_height = is_tablet ? 2048 : 3200
  
  # The phone screenshot is ~1284x2778. Tablet is ~2732x2048.
  # We scale the screenshot down slightly so it fits beautifully on the background.
  scale_percent = is_tablet ? "85%" : "85%"
  offset_y = is_tablet ? "+0+50" : "+0+150"
  
  # ImageMagick command
  cmd = [
    "magick", bg,
    "-resize", "#{bg_width}x#{bg_height}\\!",
    "\\(", screenshot, "-resize", scale_percent, "\\)",
    "-gravity", "center",
    "-geometry", offset_y,
    "-composite",
    "-font", title_font, "-fill", "white", "-pointsize", is_tablet ? "100" : "120",
    "-gravity", "North", "-annotate", "+0+150", "\"#{title}\"",
    "-font", kw_font, "-fill", "white", "-pointsize", is_tablet ? "40" : "50",
    "-gravity", "South", "-annotate", "+0+150", "\"#{keyword}\"",
    output
  ]
  
  system(cmd.join(" "))
end

['phoneScreenshots', 'tenInchScreenshots'].each do |folder|
  is_tablet = folder == 'tenInchScreenshots'
  dir = "#{base_dir}/images/#{folder}"
  next unless Dir.exist?(dir)
  
  Dir.glob("#{dir}/*.png").each do |img|
    next if img.include?("_framed")
    
    filename = File.basename(img, ".png")
    
    # Try to find a matching key
    key = title_strings.keys.find { |k| filename.include?(k) }
    
    title = key ? title_strings[key] : "Upnext"
    keyword = key ? keyword_strings[key] : ""
    
    output = "#{dir}/#{filename}_framed.png"
    puts "Compositing #{filename}..."
    composite_image(img, output, title, keyword, background, title_font, keyword_font, is_tablet)
  end
end
puts "Done!"
