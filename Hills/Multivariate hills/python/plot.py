import sys
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

filename = sys.argv[1]  # get filename from Java
Z = np.loadtxt(filename, delimiter=",")
Y, X = np.mgrid[0:Z.shape[0], 0:Z.shape[1]]

fig = plt.figure(figsize=(8,6))
ax = fig.add_subplot(111, projection='3d')
ax.plot_surface(X, Y, Z, cmap='terrain', linewidth=10, antialiased=True)
plt.show()
