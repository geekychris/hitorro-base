#!/usr/bin/env python3
"""
Script to fix duplicate MIT License headers in Java files.
Ensures only ONE license header appears at the very beginning.
"""

import re
from pathlib import Path

LICENSE_HEADER = """/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
"""

def fix_file(filepath):
    """Remove all license headers and add exactly one at the top."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Count how many times our copyright appears
        count = content.count("Copyright (c) 2006")
        
        if count <= 1:
            return False  # Nothing to fix
        
        # Remove ALL occurrences of our license block (including variations)
        # Match the entire license block
        pattern = r'/\*\s*\n\s*\*\s*Copyright \(c\) 2006.*?\*/\s*\n?'
        content = re.sub(pattern, '', content, flags=re.DOTALL)
        
        # Strip leading whitespace
        content = content.lstrip()
        
        # Add single license header at the top
        new_content = LICENSE_HEADER + content
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        
        print(f"✓ Fixed: {filepath.name} (had {count} headers)")
        return True
        
    except Exception as e:
        print(f"✗ Error: {filepath}: {e}")
        return False

def main():
    base_dir = Path('/Users/chris/hitorro/hitorro-base/src/main/java')
    java_files = list(base_dir.rglob('*.java'))
    
    print(f"Scanning {len(java_files)} files for duplicates...")
    print("-" * 80)
    
    fixed = 0
    for java_file in java_files:
        if fix_file(java_file):
            fixed += 1
    
    print("-" * 80)
    if fixed > 0:
        print(f"\n✓ Fixed {fixed} files with duplicate headers")
    else:
        print(f"\n✓ No duplicates found")

if __name__ == '__main__':
    main()
