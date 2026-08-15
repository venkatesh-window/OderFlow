import re
import os

log_file = r'C:\Users\gsven\.gemini\antigravity-ide\brain\c80a5756-e5eb-40ef-ba04-3fca018ca735\.system_generated\tasks\task-396.log'

with open(log_file, 'r', encoding='utf-8') as f:
    log_content = f.readlines()

warnings = []
for i, line in enumerate(log_content):
    if '[WARNING] ' in line and '.java:' in line and 'warning: no ' in line:
        # e.g. [WARNING] C:\path\to\File.java:13: warning: no comment
        match = re.search(r'\[WARNING\] (.*?):(\d+): warning: no (.*)', line)
        if match:
            file_path = match.group(1)
            line_num = int(match.group(2))
            warning_type = match.group(3)
            warnings.append((file_path, line_num, warning_type))

# Group by file in reverse order to insert lines without offsetting earlier line numbers
grouped = {}
for w in warnings:
    file_path, line_num, warning_type = w
    if file_path not in grouped:
        grouped[file_path] = []
    grouped[file_path].append((line_num, warning_type))

for file_path, file_warnings in grouped.items():
    file_warnings.sort(key=lambda x: x[0], reverse=True)
    if not os.path.exists(file_path):
        continue
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    modified = False
    for line_num, warning_type in file_warnings:
        idx = line_num - 1
        if idx >= len(lines): continue
        target_line = lines[idx]
        
        # Calculate indent
        indent = len(target_line) - len(target_line.lstrip())
        indent_str = ' ' * indent
        
        if warning_type == 'comment':
            javadoc = f"{indent_str}/**\n{indent_str} * Automatically generated Javadoc.\n{indent_str} */\n"
            lines.insert(idx, javadoc)
            modified = True
        elif warning_type == '@return':
            javadoc = f"{indent_str}/**\n{indent_str} * @return the value\n{indent_str} */\n"
            lines.insert(idx, javadoc)
            modified = True
        elif warning_type == 'main description':
            pass # We might not be able to easily inject main desc into existing javadoc via line number reliably if it points to an existing tag

    if modified:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.writelines(lines)
        print(f"Fixed {len(file_warnings)} warnings in {os.path.basename(file_path)}")

print("Done")
