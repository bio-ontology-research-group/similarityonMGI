import pdb
import numpy as np
from sklearn.metrics import auc
import json



def negcum(rank_vec):
	rank_vec_cum = []
	prev = 0
	for x in rank_vec:
		if x == 0:
			x = x+1
			prev = prev + x
			rank_vec_cum.append(prev)
		else:
			rank_vec_cum.append(prev)
	rank_vec_cum = np.array(rank_vec_cum)
	return rank_vec_cum

diseases = np.genfromtxt('mgi.genes.txt', dtype = 'str')
genes = np.genfromtxt('genes.rank1.txt', dtype = 'str')
sim_scores = np.loadtxt('Resnik.Extrinsic.simResnik.gene-gene.TMrank1.txt', dtype = 'float32')


#old evaluation data
#with open('gdas_human.dict','r') as f:
#	disease_genes = json.load(f)

#diseases_set = np.genfromtxt('common.diseases.txt', dtype = 'str')

# updated evaluation data
with open('gold.mgi-tm.gene-gene.dict','r') as f:
	disease_genes = json.load(f)
	diseases_set = set(diseases).intersection(disease_genes.keys())


# convert sim scores into nxm where n are genes and m are diseases
sim_mat = sim_scores.reshape((len(genes),len(diseases)))
sim_mat = sim_mat.T 


sim_dict = dict()
for i,dis in enumerate(diseases):
	sim_dict[dis] = sim_mat[i]


label_mat = dict()
for dis in diseases_set:
	if dis in disease_genes and dis in sim_dict:
		assoc_genes = disease_genes[dis]
		s1 = list(set(assoc_genes))
		s1 = filter(None, s1)
		s2 = set(genes)
		phenomNet_sim = sim_dict[dis]
		sort_similarity_arg = np.argsort(phenomNet_sim)[::-1]
		sort_gene = [genes[arg] for arg in sort_similarity_arg]
		label_vec = [0]*len(sort_gene)
		test_ranks = []
		for gene in s1:
			if gene in sort_gene:
				idx = sort_gene.index(gene)
				label_vec[idx] = 1
				test_ranks.append(idx)

		label_mat[dis] = label_vec


array_tp = np.zeros((len(label_mat), len(genes)),dtype='float32')
array_fp = np.zeros((len(label_mat), len(genes)), dtype = 'float32')

for i,row in enumerate(label_mat.values()):
	elem = np.asarray(row, dtype='float32')
	tpcum = np.cumsum(elem)
	fpcum = negcum(elem)  	
	array_tp[i] = tpcum
	array_fp[i] = fpcum	


#compute fpr and tpr Rob's way 
tpsum = np.sum(array_tp, axis = 0)
fpsum = np.sum(array_fp, axis = 0)
tpr_r = tpsum/max(tpsum)
fpr_r = fpsum/max(fpsum)
auc_data = np.c_[fpr_r, tpr_r]

print('Number of Disease: {}'.format(len(label_mat)))
print('Number of genes: {}'.format(len(genes)))
print('auc {}'.format(auc(fpr_r, tpr_r)))

no_diseases=format(len(label_mat))
no_genes=format(len(genes))
auc_var=format(auc(fpr_r, tpr_r))

np.savetxt('auc_MGI-gene-gene_TMrank1.txt', auc_data, fmt = "%s")
with open('auc_MGI-gene-gene_TMrank1.txt', 'a') as file:
    file.write("AUC="+auc_var+"\n")
    file.write("#genes="+no_genes+"\n")
    file.write("#mgi-genes="+no_diseases)

#pdb.set_trace()
