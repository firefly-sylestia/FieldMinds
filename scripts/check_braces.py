#!/usr/bin/env python3
"""Brace-balance check for Kotlin files (strips comments, strings, raw strings)."""

def strip_kotlin(src):
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == '/' and i + 1 < n and src[i + 1] == '/':
            while i < n and src[i] != '\n':
                i += 1
        elif c == '/' and i + 1 < n and src[i + 1] == '*':
            j = src.find('*/', i + 2)
            i = n if j == -1 else j + 2
        elif c == '"' and src.startswith('"""', i):
            j = src.find('"""', i + 3)
            i = n if j == -1 else j + 3
        elif c == '"':
            i += 1
            while i < n:
                if src[i] == '\\':
                    i += 2
                    continue
                if src[i] == '"':
                    i += 1
                    break
                i += 1
        elif c == "'":
            i += 1
            while i < n:
                if src[i] == '\\':
                    i += 2
                    continue
                if src[i] == "'":
                    i += 1
                    break
                i += 1
        else:
            out.append(c)
            i += 1
    return ''.join(out)

import sys
for f in sys.argv[1:]:
    s = strip_kotlin(open(f).read())
    depth = 0
    min_depth = 0
    for ch in s:
        if ch == '{':
            depth += 1
        elif ch == '}':
            depth -= 1
            min_depth = min(min_depth, depth)
    status = "BALANCED" if depth == 0 and min_depth >= 0 else f"IMBALANCED (final={depth}, min={min_depth})"
    print(f, status)
