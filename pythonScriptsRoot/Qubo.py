from __future__ import print_function  # allow it to run on python2 and python3
import numpy as np
import dwave.inspector
from dwave.system.samplers import DWaveSampler
from dwave.system.composites import EmbeddingComposite
from dwave.cloud import Client
import os
import sys
qubomatrix = np.loadtxt(os.path.join(sys.path[0], 'ResultsFile.txt'))
print('Loaded matrix:\n', qubomatrix, '\n')
# convert into QUBO
qubo = {(i,i):0.0 for i in range(len(qubomatrix))}
print(type(qubo[(0,0)]))
# necessary to keep the order of the sample columns consistent
for index,value in np.ndenumerate(qubomatrix):
    if value != 0:
        qubo[index] = value
print('Converted matrix into QUBO for D-Wave:\n', qubo, '\n')
# embed and run on the D-Wave with 7500 reads
#Token hier einfügen
response = EmbeddingComposite(DWaveSampler(token='Insert Token here')).sample_qubo(qubo, num_reads=7500)
print('Response from the D-Wave:\n', response,response.data, '\n')
# save results in results.txt
with open(os.path.join(sys.path[0], 'results2.txt'),'a') as file:
    file.write(str(qubo)+'\n'+str(response.truncate(20))+'\n\n')
with open(os.path.join(sys.path[0], 'input.txt'),'w') as file:
    file.write(str(qubo)+'\n'+str(response.first)+'\n\n')
with open(os.path.join(sys.path[0], 'console.txt'),'a') as file:
    file.write(str(qubo)+'\n'+str(response.truncate(20))+'\n\n')
dwave.inspector.show(response,block='once')
sys.exit()