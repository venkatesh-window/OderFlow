import os
import glob

def clean_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    new_lines = []
    i = 0
    modified = False
    while i < len(lines):
        line = lines[i]
        if '/**' in line and i + 2 < len(lines):
            if '* Automatically generated Javadoc.' in lines[i+1] or '* @return the value' in lines[i+1]:
                # Skip the 3 lines of the generated block
                i += 3
                modified = True
                continue
        new_lines.append(line)
        i += 1
        
    if modified:
        with open(path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print(f"Cleaned {path}")

for root, _, files in os.walk(r'c:\Users\gsven\OrderFlow\src\main\java'):
    for f in files:
        if f.endswith('.java'):
            clean_file(os.path.join(root, f))
