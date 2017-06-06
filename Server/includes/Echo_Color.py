import os
##Windows Color Codes
if os.name == 'nt':
    class colors:
        HEADER = '\033[95m'
        BLUE = '\033[94m'
        GREEN = '\033[92m'
        WARNING = '\033[93m'
        FAIL = '\033[91m'
        END = '\033[0m'
        BOLD = '\033[1m'
        UNDERLINE = '\033[4m'
##Posix Color Codes
elif os.name == 'posix':
    class colors:
        HEADER = '\033[95m'
        BLUE = '\033[94m'
        GREEN = '\033[92m'
        WARNING = '\033[93m'
        FAIL = '\033[91m'
        END = '\033[0m'
        BOLD = '\033[1m'
        UNDERLINE = '\033[4m'
        
def ePrint(color, string):
    print(color + string + colors.END)

if __name__ == "__main__":
    ePrint(colors.FAIL, "Please launch Echo_Server.py")
    exit()