from __future__ import print_function
import numpy as np
from dwave.system.samplers import DWaveSampler
from dwave.system.composites import EmbeddingComposite
from dwave.cloud import Client
from dwave_qbsolv import QBSolv
import os
import sys
qubomatrix = np.loadtxt(os.path.join(sys.path[0], 'ResultsFile.txt'))/100
#print('Loaded matrix:\n', qubomatrix, '\n')
# convert into QUBO
qubo = {(i,i):0.0 for i in range(len(qubomatrix))}
#print(type(qubo[(0,0)]))
# necessary to keep the order of the sample columns consistent
for index,value in np.ndenumerate(qubomatrix):
    if value != 0:
        qubo[index] = value
#print('Converted matrix into QUBO for D-Wave:\n', qubo, '\n')
#run qubo
response = QBSolv().sample_qubo(qubo)
#save result
#with open(os.path.join(sys.path[0], 'results.txt'),'a') as file:
#    file.write(str(qubo)+'\n'+str(response)+'\n\n')
print('test'+'\n'+str(response.data)+'\n\n')
with open(os.path.join(sys.path[0], 'input.txt'),'w') as file:
    file.write(str(qubo)+'\n'+str(response.data)+'\n\n')