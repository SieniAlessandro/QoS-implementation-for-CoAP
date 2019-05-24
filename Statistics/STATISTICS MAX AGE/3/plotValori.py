import pandas as pd
import matplotlib.pyplot as plt
from datetime import datetime
import time

df = pd.read_csv("ObserverLog.csv")

timeAxys = df["Time"].values.tolist()
first = datetime.strptime(timeAxys[0], "%M:%S")
tempi = []
for i in timeAxys:
    t1 = datetime.strptime(i, "%M:%S")
    tempi.append((t1-first).seconds)


fig = plt.figure()
plt.plot(tempi, df["Value"].values.tolist(),marker="D", color="red")
plt.grid("on")
fig.suptitle('Testing MaxAge Algorithm', fontsize=14)
plt.xlabel('Time [s]', fontsize=12)
plt.ylabel('Value', fontsize=12)
plt.savefig("plot.png")
