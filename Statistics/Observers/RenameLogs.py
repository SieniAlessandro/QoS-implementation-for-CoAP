import os
import argparse

parser = argparse.ArgumentParser(description='Rename logs of observer')
parser.add_argument("poolSize", help='number of observers to run')
args = parser.parse_args()

for observer in range(0,int(args.poolSize)):
    dirname = "Observer-" + str(observer)
    os.rename(dirname+"/ObserverLog.csv", dirname+"/ObserverLog" + str(observer) + ".csv")
