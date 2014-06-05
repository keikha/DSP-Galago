package ciir.umass.edu.retrieval.dts;

public class Document {
	public String docID = "";
	public double score = -1.0;
	public double divScore = -1.0;
	public Document(String docID, double score){
		this.docID = docID;
		this.score = score;
	}
}
