package ciir.umass.edu.retrieval.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class IndriQueryFileWriter {
	private BufferedWriter out = null;
	private String collection = "";
	private String outputFile = "";
	
	public IndriQueryFileWriter(String fn, String collection)
	{
		outputFile = fn;
		this.collection = collection;
	}
	public void write(List<String> qid, List<String> qText)
	{
		try {
			out = new BufferedWriter(
		            new OutputStreamWriter(
		            new FileOutputStream(outputFile), "ASCII"));
			writeHeader();
			writeQuery(qid, qText);
			finishHeader();
			close();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
		
	}

	private void close()
	{
		try {
			out.close();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	private void writeHeader()
	{
		try {
			out.write("<parameters>");
			out.newLine();
			if(collection.compareTo("") != 0)
			{
				out.write("   <index>" + collection + "</index>");
				out.newLine();
				out.write("   <trecFormat>true</trecFormat>");
				out.newLine();
			}			
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	private void writeQuery(String qid, String qText)
	{
		try {
			out.write("   <query>");
			out.newLine();
			out.write("      <number>" + qid + "</number>");
			out.newLine();
			out.write("      <text>" + qText + "</text>");
			out.newLine();
			out.write("   </query>");
			out.newLine();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	private void writeQuery(List<String >qid, List<String> qText)
	{
		for(int i=0;i<qid.size();i++)
			writeQuery(qid.get(i), qText.get(i));
	}
	private void finishHeader()
	{
		try {
			out.write("</parameters>");
			out.newLine();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
}
