@Grab(group='com.github.sharispe', module='slib-sml', version='0.9.1')
@Grab(group='org.codehaus.gpars', module='gpars', version='1.1.0')

import java.net.*
import org.openrdf.model.vocabulary.*
import slib.sglib.io.loader.*
import slib.sml.sm.core.metrics.ic.utils.*
import slib.sml.sm.core.utils.*
import slib.sglib.io.loader.bio.obo.*
import org.openrdf.model.URI
import slib.graph.algo.extraction.rvf.instances.*
import slib.sglib.algo.graph.utils.*
import slib.utils.impl.Timer
import slib.graph.algo.extraction.utils.*
import slib.graph.model.graph.*
import slib.graph.model.repo.*
import slib.graph.model.impl.graph.memory.*
import slib.sml.sm.core.engine.*
import slib.graph.io.conf.*
import slib.graph.model.impl.graph.elements.*
import slib.graph.algo.extraction.rvf.instances.impl.*
import slib.graph.model.impl.repo.*
import slib.graph.io.util.*
import slib.graph.io.loader.*
import groovyx.gpars.GParsPool

System.setProperty("jdk.xml.entityExpansionLimit", "0");
System.setProperty("jdk.xml.totalEntitySizeLimit", "0");

def factory = URIFactoryMemory.getSingleton()

class Gene {

  int id
  Set annotations

  public Gene(id, annotations) {
    setId(id)
    setAnnotations(annotations)
  }

  void addAnnotation(annotation) {
    annotations.add(annotation);
  }

  def getAnnotations() {
    annotations
  }

}


def getGeneOntology = {

  URI graph_uri = factory.getURI("http://purl.obolibrary.org/obo/")
  G graph = new GraphMemory(graph_uri)

  // Load OBO file to graph "go.obo"
  GDataConf goConf = new GDataConf(GFormat.RDF_XML, "phenomenet5-aug-18.owl")
  GraphLoaderGeneric.populate(goConf, graph)

  // Add virtual root for 3 subontologies__________________________________
  URI virtualRoot = factory.getURI("http://purl.obolibrary.org/obo/virtualRoot")
  graph.addV(virtualRoot)


  new File("textmined_rank1.txt").splitEachLine('\t') { items ->
 if (items[0].startsWith("#")) return;
    String pathId = items[0];//.split("__")[0];

  URI idURI = factory.getURI("http://purl.obolibrary.org/obo/PATH_" + pathId);
    for (int i = 1; i < items.size(); i++) {
	String pheno = items[i].replaceAll(":", "_");
        //  String pheno=items[i]
  	URI phenoURI = factory.getURI("http://purl.obolibrary.org/obo/" + pheno);
    	Edge e = new Edge(idURI, RDF.TYPE, phenoURI);
    	graph.addE(e);
    }    
  }

  GAction rooting = new GAction(GActionType.REROOTING)
  rooting.addParameter("root_uri", virtualRoot.stringValue())
  GraphActionExecutor.applyAction(factory, rooting, graph)
  return graph
}

def getURIfromGO = { go ->
  def id = go.split('\\:')
//println id[0] + "_" + id[1] //bunu iptal et sonra
  return factory.getURI("http://purl.obolibrary.org/obo/" + id[0] + "_" + id[1])

// return factory.getURI("http://purl.obolibrary.org/obo/" + id[0])


}

def getGenes = {
  def genes = [].withDefault {new Gene(0, new LinkedHashSet())}
  def i = 0

//pathogens.4sim.txt

new File("textmined_rank1.txt").splitEachLine('\t') { items ->
    
    for (int j = 1; j < items.size(); j++) {
    genes[i].addAnnotation(getURIfromGO(items[j]))
  
    }
   i++
  }
  return genes
}

def getDiseases = {
  def dis = [].withDefault {new Gene(0, new LinkedHashSet())}
  def i = 0 //test_patient_phenotypes.txt

//pathogens.4sim.txt

 new File("mgi.gene-pheno.4sim.txt").splitEachLine('\t') { items -> 
    
    for (int j = 1; j < items.size(); j++) {
      dis[i].addAnnotation(getURIfromGO(items[j]))
    
    }
   i++
  }
  return dis
}

genes = getGenes()
diseases = getDiseases()

graph = getGeneOntology()



//Printing Phenomenet data

// for (URI v: graph.getV()) {
//   if (v.toString().indexOf("HP_") != -1 || v.toString().indexOf("MP_") != -1)
//     println v.toString();
// }

// return
//


SM_Engine engine = new SM_Engine(graph)

// BMA+Resnik, BMA+Schlicker2006, BMA+Lin1998, BMA+Jiang+Conrath1997,
// DAG-GIC, DAG-NTO, DAG-UI

String[] flags = [
  // SMConstants.FLAG_SIM_GROUPWISE_AVERAGE,
  // SMConstants.FLAG_SIM_GROUPWISE_AVERAGE_NORMALIZED_GOSIM,
  SMConstants.FLAG_SIM_GROUPWISE_BMA,
  SMConstants.FLAG_SIM_GROUPWISE_BMM,
  SMConstants.FLAG_SIM_GROUPWISE_MAX,
  SMConstants.FLAG_SIM_GROUPWISE_MIN,
  SMConstants.FLAG_SIM_GROUPWISE_MAX_NORMALIZED_GOSIM
]

// List<String> pairFlags = new ArrayList<String>(SMConstants.PAIRWISE_MEASURE_FLAGS);
String[] pairFlags = [
  SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_SIM_IC_2010,
  SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_RESNIK_1995,
  SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_2006,
  SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998,
  SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_JIANG_CONRATH_1997_NORM
]


ICconf icConf = new IC_Conf_Corpus("ResnikIC", SMConstants.FLAG_IC_ANNOT_RESNIK_1995_NORMALIZED); //Inf content - extrinsic, resnik

smConfGroupwise = new SMconf("BMA", SMConstants.FLAG_SIM_GROUPWISE_BMA);

//SMconf smConfPairwise = new SMconf("SimIC",  SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_SIM_IC_2010); //rada (similarity)
SMconf smConfPairwise = new SMconf("Resnik",  SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_RESNIK_1995);//resnik
smConfPairwise.setICconf(icConf);            



def result = new Double[genes.size() * diseases.size()]
for (i = 0; i < result.size(); i++) {
  result[i] = i
}

def c = 0

GParsPool.withPool {
  result.eachParallel { val ->
    def i = val.toInteger()
    def x = i.intdiv(diseases.size())
    def y = i % diseases.size()
    result[i] = engine.compare(
            smConfGroupwise,
            smConfPairwise,
            genes[x].getAnnotations(),
            diseases[y].getAnnotations())
    if (c % 100000 == 0)
      println c
    c++
  }
}

def fout = new PrintWriter(new BufferedWriter(
  new FileWriter("Resnik.Extrinsic.simResnik.gene-gene.TMrank1.txt")))
for (i = 0; i < result.size(); i++) {
  def x = i.intdiv(diseases.size())
  def y = i % diseases.size()
  fout.println(result[i])
}
fout.flush()
fout.close()



