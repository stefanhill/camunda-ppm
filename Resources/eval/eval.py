import sqlite3
import pandas as pd
from matplotlib import pyplot as plt
from matplotlib import patches as mpatches
from matplotlib import dates as mdates
import datetime
import numpy as np

class Utils:

    @staticmethod
    def sql_to_df(db_name, table_name):
        conn = sqlite3.connect("C:/git/fg-bks-camunda/Resources/eval/db/" + db_name + ".db")
        df = pd.read_sql_query("SELECT * FROM " + table_name, conn)
        conn.commit()
        conn.close()
        return df


db = "Simulation_DomesticDeclarations_BayesianOptimizer_large"

datasets = ["DomesticDeclarations", "InternationalDeclarations"]
optimizer = ["GridOptimizer", "RandomOptimizer", "BayesianOptimizer"]
sizes = ["small", "medium", "large"]
classifier = ["NGramClassifier", "IBkClassifier", "NaiveBayesClassifier", "RandomForestClassifier", "HoeffdingClassifier"]

data = []
for d in datasets:
    for o in optimizer:
        for s in sizes:
            filename = "Simulation_" + d + "_" + o + "_" + s
            df = Utils.sql_to_df(filename, "Head_Result_Table")
            t = df.sum(axis=0)["time_in_ms"]
            cs = [d, o, s]
            accs = [d, o, s]
            table_string = [d, o, s]
            for c in classifier:
                time = df[df["classifier"] == c].sum(axis=0)["time_in_ms"]
                accuracy = round(df[df["classifier"] == c].max()["ACC"]*100)
                cs.append(time)
                accs.append(accuracy)
                table_string.append(str(time / 1000) + "s - " + str(accuracy) + "%")
            data.append([filename, d, o, s, df, t, cs, accs, table_string])

dataframe = pd.DataFrame(data, columns=["Filename", "Dataset", "Optimizer", "Size", "Dataframe", "Time", "Classifier", "Accuracies", "Table_String"])

colorpalette = ["tab:blue" for _ in range(3)] + ["tab:orange" for _ in range(3)] + ["tab:green" for _ in range(3)]

handles = []
handles.append(mpatches.Patch(color='tab:blue', label='Grid Optimizer'))
handles.append(mpatches.Patch(color='tab:orange', label='Random Optimizer'))
handles.append(mpatches.Patch(color='tab:green', label='Bayesian Optimizer'))

fig, axes = plt.subplots(1, 2, sharey=True)
dataframe[:9].plot(y="Time", x="Size", kind="bar", color=colorpalette, ax=axes[0], title="Domestic Declarations")
dataframe[9:].plot(y="Time", x="Size", kind="bar", color=colorpalette, ax=axes[1], title="International Declarations")
axes[0].get_legend().remove()

plt.yticks([x * 3600000 for x in range(10)], [str(x) + ':00' for x in range(10)])

plt.legend(handles=handles)
axes[0].set_ylabel("Time in h")
plt.show()


#pd.DataFrame(dataframe["Classifier"]).to_csv("table_classifier_result.csv")
#pd.DataFrame(dataframe["Accuracies"]).to_csv("table_classifier_accuracy.csv")
pd.DataFrame(dataframe["Table_String"]).to_csv("results.csv")
