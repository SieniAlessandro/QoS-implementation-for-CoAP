import argparse
import os
import shutil
import subprocess
from time import sleep

parser = argparse.ArgumentParser(description='Run a pool of observer for testing purpose')
parser.add_argument("poolSize", help='number of observers to run')
args = parser.parse_args()

outputList = []

ipAddress = "fd00::c30c:0:0:1"

for pinger in range(0,int(args.poolSize)):
    outputList.append(subprocess.Popen(["ping", "192.168.4.1"], shell=True))
