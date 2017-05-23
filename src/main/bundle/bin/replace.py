from subprocess import Popen, PIPE

def my_replace(executable, match):
    p = Popen([executable] + match, stdin=PIPE, stdout=PIPE, stderr=PIPE)
    output, err = p.communicate()
    if p.returncode != 0:
        raise ValueError('Error replacing ["' + ('", "'.join(match)) + '"]\n' + err)
    return output
