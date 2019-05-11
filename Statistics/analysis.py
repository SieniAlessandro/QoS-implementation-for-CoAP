import pandas
import numpy as np
import itertools
from datetime import datetime
from statistics import mean
import matplotlib.pyplot as plt
TIME_FORMAT_SENSOR = "%M:%S "
TIME_FORMAT = "%M:%S"

SensorDF = pandas.read_csv("log1.txt")
BASEPATH = "ObserverLog"
avgs = []
for i in range(1,5):
    FILENAME = BASEPATH+str(i)+".csv"
    ObserverDF= pandas.read_csv(FILENAME)
    MergeList = ["IPAddress","Value","Type","Critic","Observe"]
    result = SensorDF.merge(ObserverDF,on=MergeList)
    result = result.loc[result["Critic"] == 1]
    #print(result)
    TimeDiff = []
    for t1,t2 in zip(result['Time_x'].tolist(),result["Time_y"].tolist()):
        t1 = datetime.strptime(t1,TIME_FORMAT_SENSOR)
        t2 = datetime.strptime(t2,TIME_FORMAT)
    #    print(str(t2)+"------"+str(t1))
        TimeDiff.append((t2-t1).seconds)
    print(len(TimeDiff))
    avgs.append(mean(TimeDiff))
print(avgs)
plt.xticks(list(range(1,5)))
plt.yticks(avgs)
plt.plot(list(range(1,5)),avgs,color="r",marker="o")
#for i in range(0,4):
#    plt.annotate(str(avgs[i]),xy=(i+1.05,avgs[i]+avgs[0]/300))
plt.show()
