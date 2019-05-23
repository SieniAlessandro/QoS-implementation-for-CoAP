import pandas as pd
import matplotlib.pyplot as plt
df = pd.read_csv("ObserverLog.csv")
plt.plot(df["Value"].values.tolist(),"o")

plt.savefig("plot.png")
