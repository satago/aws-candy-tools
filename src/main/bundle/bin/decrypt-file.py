import re
import os
import sys
from replace import my_replace

script_path=os.path.dirname(os.path.abspath(__file__))
decrypt=os.path.join(script_path, 'decrypt')

print re.sub(r'aws:kms:[^:]+:(.*)',
             lambda match: my_replace(decrypt, [match.group(1)]),
             sys.stdin.read())
