#!/usr/bin/env python3
"""
Script to apply MIT License header to all Java files in the project.
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

def has_license_header(content):
    """Check if file already has a MIT license header."""
    return "Copyright (c) 2006-2025 Chris Collins" in content[:1000] and "MIT" in content[:2000]

def remove_old_copyright(content):
    """Remove old copyright headers and existing license headers."""
    # Remove any existing block comments at the start (including our license header for re-application)
    # This handles both old copyrights and our new one if we need to reapply
    while content.strip().startswith('/*'):
        # Find the end of the first block comment
        end_pos = content.find('*/')
        if end_pos == -1:
            break
        # Remove this block comment
        content = content[end_pos + 2:].lstrip()
    
    # Also remove any leading single-line comments
    lines = content.split('\n')
    while lines and lines[0].strip().startswith('//'):
        lines.pop(0)
    
    return '\n'.join(lines)

def apply_license_to_file(filepath):
    """Apply MIT license header to a Java file."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Skip if already has the new license
        if has_license_header(content):
            print(f"✓ Already licensed: {filepath}")
            return True
        
        # Remove old copyright headers
        content = remove_old_copyright(content)
        
        # Find the package declaration
        package_match = re.search(r'^package\s+', content, re.MULTILINE)
        
        if package_match:
            # Insert license before package
            new_content = LICENSE_HEADER + content
        else:
            # No package declaration, just prepend
            new_content = LICENSE_HEADER + '\n' + content
        
        # Write back
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        
        print(f"✓ Licensed: {filepath}")
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
    print("-" * 80)
    
    success_count = 0
    for java_file in java_files:
        if apply_license_to_file(java_file):
            success_count += 1
    
    print("-" * 80)
    print(f"\nComplete: {success_count}/{total} files processed successfully")
    
    if success_count < total:
        print(f"Warning: {total - success_count} files had errors")

if __name__ == '__main__':
    main()
