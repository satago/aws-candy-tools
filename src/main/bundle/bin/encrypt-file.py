import re
import os
import sys
from replace import my_replace

script_path=os.path.dirname(os.path.abspath(__file__))
encrypt=os.path.join(script_path, 'encrypt')

options = []
if "AWS_CANDY_TOOLS_NAMESPACE" in os.environ:
    options.append("-n")
    options.append(os.getenv("AWS_CANDY_TOOLS_NAMESPACE"))

print re.sub(r'aws:kms:([^:]+):encrypt:(.*)[\n]?',
             lambda match: 'aws:kms:' + match.group(1) + ':'
                           + my_replace(encrypt, options + [match.group(1), match.group(2)]),
             sys.stdin.read())
