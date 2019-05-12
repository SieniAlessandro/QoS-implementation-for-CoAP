import pandas
import numpy as np
import itertools
from datetime import datetime
from statistics import mean
import matplotlib.pyplot as plt
import sys
import argparse

TIME_FORMAT_SENSOR = "%M:%S "
TIME_FORMAT = "%M:%S"
BASEPATH = "\ObserverLog"
MERGEKEYS = ["IPAddress","Value","Type","Critic","Observe"]
avgs = []
tmp = []
Dataframes = {}

parser = argparse.ArgumentParser()
parser.add_argument("-r","--root",help="Select the root folder relatives to the csv files,inside the Dati folder")
parser.add_argument("-n","--number",help="The number of csv files",type=int)
args = parser.parse_args()
root = args.root
nlogs = args.number

#APERTURA DEL DATAFRAME RELATIVO AL SENSORE
SensorDF = pandas.read_csv("Dati\\"+root+"\log1.txt")
#Scrolling the sets


for set in range(0,int(int(nlogs)/4)):
    Dataframes[str(set)] = []
    for priority in range(1,5):
        FILENAME = "Dati\\"+root+BASEPATH+str(set)+str(priority)+".csv"
        ObserverDF= pandas.read_csv(FILENAME)
        result = SensorDF.merge(ObserverDF,on=MERGEKEYS)
        # Choosing only the critic value
        result = result.loc[result["Critic"] == 1]
        Dataframes[str(set)].append(result)

#Computing the average timediff for all the priorities
for priority in range(0,4):
    tmp = []
    for sets in range(0,int(int(nlogs)/4)):
        TimeDiff = []
        actualDF = Dataframes[str(sets)][priority]
        for t1,t2 in zip(actualDF['Time_x'].values.tolist(),actualDF["Time_y"].values.tolist()):
            t1 = datetime.strptime(t1,TIME_FORMAT_SENSOR)
            t2 = datetime.strptime(t2,TIME_FORMAT)
            TimeDiff.append((t2-t1).seconds)
        tmp.append(mean(TimeDiff))
    avgs.append(mean(tmp))

#Preparing and plotting the line graph
plt.xticks(list(range(1,5)))
plt.yticks(avgs)
plt.plot(list(range(1,5)),avgs,color="r",marker="o")
plt.savefig("Dati\\"+root+"\plot.png")
plt.show()
