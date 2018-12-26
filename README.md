# similarityonMGI
this repsoitory contains data and scripts to evaluate semantic similarity involving genes and diseases based on their phenotypes.

GENE-DISEASE Prediction:

This is based on similarity between gene-phenotypes and disease-phenotypes on the MGI gene-disease datasets

Gold standards containing gene-disease:

MGI.human.gene-disease.dict 	

MGI.ortholog.gene-disease.dict

Script to calculate resnik similarity:

simMGI.groovy   (this script utilizes: phenomenet (lightPhenomeNET-inferred.owl.zip) and gene-phenotypes (MGI.gene-phenotypes.4sim.txt) and disease-phenotypes (HPO.disease-phenotypes.txt)


get_auc_phenomNet.py calculates the AUC on the gold standards.


steps to follow:
1. run groovy simMGI.groovy to get the similarity scores between gene-phenotypes and disease-phenotypes
2. run python get_auc_phenomeNet.py to estimate the performace on the gold datasets.

All the requierd files are provided in this directory and file names are hard-coded in the scripts.

Note: By using the files provided, you should get an AUC value of 0.76 on  MGI.human.gene-disease.dict and 0.90 on MGI.ortholog.gene-disease.dict


GENE-GENE SIMILARITY:

This is based on similarity between gene-phenotypes on the MGI dataset.

Gold standard containing ......

x.dict	


Script to calculate resnik similarity:

sim_gene-gene_mgi.groovy  (this script utilizes: phenomenet (lightPhenomeNET-inferred.owl.zip) and gene-phenotypes (MGI.gene-phenotypes.4sim.txt) and disease-phenotypes (HPO.disease-phenotypes.txt)


get_auc_phenomNet.py would calculate the AUC on the gold standards.


steps to follow:
1. run groovy sim_gene-gene_mgi.groovy to get the similarity scores between gene-phenotypes and gene-phenotypes
2. run python get_auc_phenomeNet.py to estimate the performace on the gold dataset.

All the requierd files are provided in this directory and file names are hard-coded in the scripts.

  
  
