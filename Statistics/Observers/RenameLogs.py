import os
import argparse
import shutil

parser = argparse.ArgumentParser(description='Rename logs of observer')
parser.add_argument("poolSize", help='number of observers to run')
args = parser.parse_args()

newDir = "Test"
os.mkdir(newDir)
for observer in range(0,int(args.poolSize)):
    dirname = "Observer-" + str(observer)
    newName = "/ObserverLog" + str(int(observer/4)) +str((observer%4)+1)+ ".csv"
    os.rename(dirname+"/ObserverLog.csv", dirname + newName)
    shutil.copyfile(newName, newDir + newName)
