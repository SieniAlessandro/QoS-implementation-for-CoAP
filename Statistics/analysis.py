import pandas
import numpy as np
import itertools
from datetime import datetime
from statistics import mean
TIME_FORMAT = "%M:%S"

ProxyDF = pandas.read_csv("LogProxy.csv")
ObserverDF = pandas.read_csv("ObserverLog.csv")

MergeList = ["IPAddress","Value","Type","Critic","Observe"]
result = ProxyDF.merge(ObserverDF,left_on=MergeList,right_on=MergeList)

UniqueType = np.unique(np.array(result['Type'].tolist()))
UniqueSensors = np.unique(np.array(result['IPAddress'].tolist()))
UniqueCritic = ["0","1"]
Unique = list(itertools.product(UniqueType,UniqueSensors,UniqueCritic))
Dataframes = []
for Ut,Us,Uc in Unique:
    #Adding this subset of dataframe
    Dataframes.append(result.loc[(result["Type"] == Ut) & (result['IPAddress'] == Us) & (result['Critic'] == int(Uc))])
Avgs = []
for Dataframe in Dataframes:
    TimeDiff = []
    for t1,t2 in zip(Dataframe['Time_x'].tolist(),Dataframe["Time_y"].tolist()):
        t1 = datetime.strptime(t1,TIME_FORMAT)
        t2 = datetime.strptime(t2,TIME_FORMAT)
        TimeDiff.append((t2-t1).seconds)
    Avgs.append(mean(TimeDiff))

print(Avgs)
