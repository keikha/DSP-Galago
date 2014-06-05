package ciir.umass.edu.qproc;

public class NounPhrase {
	public String text = "";
	public int start = -1;
	public int end = -1;
	public NounPhrase(String text, int start, int end)
	{
		this.text = text;
		this.start = start;
		this.end = end;
	}
}
