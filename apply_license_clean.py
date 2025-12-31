#!/usr/bin/env python3
"""
Script to apply MIT License header to all Java files in the project, cleaning up duplicates.
Copyright (c) 2006-2025 Chris Collins
"""

import os
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

def remove_all_leading_comments(content):
    """Remove ALL block comments and single-line comments from the start of the file."""
    original_len = len(content)
    
    while True:
        # Strip whitespace
        content = content.lstrip()
        
        # Check for block comment
        if content.startswith('/*'):
            end_pos = content.find('*/')
            if end_pos == -1:
                break  # Unclosed comment, stop
            content = content[end_pos + 2:]
            continue
        
        # Check for single-line comment
        if content.startswith('//'):
            newline_pos = content.find('\n')
            if newline_pos == -1:
                content = ''  # Entire file is a comment
                break
            content = content[newline_pos + 1:]
            continue
        
        # No more leading comments
        break
    
    return content

def apply_license_to_file(filepath):
    """Apply MIT license header to a Java file."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Remove all leading comments (including old licenses and copyrights)
        content_without_comments = remove_all_leading_comments(content)
        
        # Apply the new license header
        new_content = LICENSE_HEADER + content_without_comments
        
        # Write back
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        
        print(f"✓ Licensed: {filepath.name}")
        return True
        
    except Exception as e:
        print(f"✗ Error processing {filepath}: {e}")
        return False

def main():
    """Main function to process all Java files."""
    base_dir = Path('/Users/chris/hitorro/hitorro-base/src/main/java')
    
    if not base_dir.exists():
        print(f"Error: Directory not found: {base_dir}")
        return
    
    java_files = list(base_dir.rglob('*.java'))
    total = len(java_files)
    
    print(f"Found {total} Java files to process...")
    print(f"Cleaning all existing copyright headers and applying MIT License...")
    print("-" * 80)
    
    success_count = 0
    for java_file in java_files:
        if apply_license_to_file(java_file):
            success_count += 1
    
    print("-" * 80)
    print(f"\n✓ Complete: {success_count}/{total} files processed successfully")
    
    if success_count < total:
        print(f"✗ Warning: {total - success_count} files had errors")

if __name__ == '__main__':
    main()
