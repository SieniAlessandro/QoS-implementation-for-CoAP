import pandas as pd
import matplotlib.pyplot as plt
df = pd.read_csv("ObserverLog.csv")
fig = plt.figure()
plt.plot(df["Value"].values.tolist(),"o")
fig.suptitle('Always Send Packet', fontsize=14)

plt.savefig("plot.png")
