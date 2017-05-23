import re
import os
import sys
from replace import my_replace

script_path=os.path.dirname(os.path.abspath(__file__))
encrypt=os.path.join(script_path, 'encrypt')

print re.sub(r'aws:kms:([^:]+):encrypt:(.*)[\n]?',
             lambda match: 'aws:kms:' + match.group(1) + ':'
                           + my_replace(encrypt, [match.group(1), match.group(2)]),
             sys.stdin.read())
