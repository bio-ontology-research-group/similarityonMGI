# similarityonMGI
This repository contains data and scripts to analize semantic similarity between two given entities (e.g. genes and diseases) based on their phenotypes.

**EXAMPLE 1) GENE-DISEASE Prediction:**

Gene-disease associations can be predicted based on the similarity between gene-phenotypes and disease-phenotypes and evaluation can be done on the MGI gene-disease dataset.


simMGI.groovy  

This script calculates the Resnik similarity between genes and diseases.
It utilizes: phenomenet (lightPhenomeNET-inferred.owl) and gene-phenotypes (MGI.gene-phenotypes.4sim.txt) and disease-phenotypes (HPO.disease-phenotypes.txt)



MGI.human.gene-disease.dict 	and MGI.ortholog.gene-disease.dict


These are the gold standards containing gene-disease associations


get_auc_phenomNet.py 

This script calculates the AUC on the gold standards


steps to follow:
1. run groovy simMGI.groovy to get the similarity scores between gene-phenotypes and disease-phenotypes
2. run python get_auc_phenomeNet.py to estimate the performance on the gold datasets.

All the required files are provided in this directory and file names are hard-coded in the scripts.

Note: By using the files provided, you should get an AUC value of 0.76 on  MGI.human.gene-disease.dict and 0.90 on MGI.ortholog.gene-disease.dict


**EXAMPLE 2) GENE-GENE SIMILARITY:**

Known genes can be recovered by using their phenotypic similarities.


sim_gene-gene_mgi.groovy  

This script calculates the similarities between genes. It utilizes: phenomeNet (phenomenet5-aug-18.owl) and gene-phenotypes from two methods, e.g. text mined extracts (textmined_rank1.txt) and experimental findings from MGI (mgi.gene-pheno.4sim.txt)


get_auc_gene-gene-mouse1.py 

This script calculates the AUC on a gold standard


Gold standard:

gold.mgi-tm.gene-gene.dict


steps to follow:
1. run groovy sim_gene-gene_mgi.groovy to get the similarity scores between gene-phenotypes and gene-phenotypes
2. run python get_auc_gene-gene-mouse1.py to estimate the performance on the gold dataset.

All the required files are provided in this directory and file names are hard-coded in the scripts.

  
  
