import os
import sys

def sort_imports(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    start_idx = -1
    end_idx = -1
    
    for i, line in enumerate(lines):
        if line.startswith('import '):
            if start_idx == -1:
                start_idx = i
            end_idx = i
            
    if start_idx == -1: return
    
    imports = []
    java_imports = []
    alias_imports = []
    
    for line in lines[start_idx:end_idx+1]:
        if not line.startswith('import '): continue
        if ' as ' in line:
            alias_imports.append(line)
        elif line.startswith('import java.') or line.startswith('import javax.') or line.startswith('import kotlin.'):
            java_imports.append(line)
        else:
            imports.append(line)
            
    imports.sort()
    java_imports.sort()
    alias_imports.sort()
    
    sorted_lines = imports + java_imports + alias_imports
    
    new_lines = lines[:start_idx] + sorted_lines + lines[end_idx+1:]
    
    with open(filepath, 'w') as f:
        f.writelines(new_lines)

files = [
    "app/src/main/java/com/theupnextapp/ui/settings/SettingsScreen.kt",
    "app/src/main/java/com/theupnextapp/ui/showDetail/ShowDetailScreen.kt",
    "app/src/main/java/com/theupnextapp/ui/search/SearchScreen.kt",
    "app/src/main/java/com/theupnextapp/ui/showSeasonEpisodes/ShowSeasonEpisodesScreen.kt",
    "app/src/main/java/com/theupnextapp/ui/personDetail/PersonDetailScreen.kt"
]

for f in files:
    sort_imports(f)
    print("Sorted " + f)
