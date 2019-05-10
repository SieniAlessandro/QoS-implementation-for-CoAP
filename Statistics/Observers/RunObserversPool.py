import argparse
import os
import shutil
import subprocess
from time import sleep

parser = argparse.ArgumentParser(description='Run a pool of observer for testing purpose')
parser.add_argument("poolSize", help='number of observers to run')
args = parser.parse_args()

portList = [10000, 20000, 30000, 40000]
pidList = []

for observer in range(0,int(args.poolSize)):
    dirname = "Observer-" + str(observer)
    if os.path.isdir(dirname):
        shutil.rmtree(dirname)
    os.mkdir(dirname)
    shutil.copyfile("Observer.jar", dirname + "/Observer.jar")

    port = str(portList[observer%4] + observer)
    print("Starting " + dirname + " with port " + port + " and priority " + str(observer%4+1))
    os.chdir(dirname)
    pidList.append(subprocess.Popen(["java", "-jar", "Observer.jar", "192.168.4.1", "5683", str(port), "true", "false", str(observer%4+1)], shell=True))
    sleep(1)
    os.chdir("..")
pause = input()
for pid in pidList:
    os.kill(pid)
