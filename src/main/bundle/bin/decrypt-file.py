#!/usr/bin/env python3

import re
import os
import sys
from replace import my_replace

script_path=os.path.dirname(os.path.abspath(__file__))
decrypt=os.path.join(script_path, 'decrypt')

options = []
if "AWS_CANDY_TOOLS_NAMESPACE" in os.environ:
    options.append("-n")
    options.append(os.getenv("AWS_CANDY_TOOLS_NAMESPACE"))

print(re.sub(r'aws:kms:[^:]+:(.*)',
             lambda match: my_replace(decrypt, options + [match.group(1)]),
             sys.stdin.read()))
