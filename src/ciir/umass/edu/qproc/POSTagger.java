package ciir.umass.edu.qproc;

import java.io.*;
import java.util.*;
import cmu.arktweetnlp.RunTagger;
import cmu.arktweetnlp.Tagger;

/**
 * Created by ashishjain on 3/25/14.
 */
public class POSTagger {
	private static Tagger tagger = null;
	
	public POSTagger() {
		if(tagger == null) {
			tagger = new Tagger();
			try {
				tagger.loadModel("model.20120919");
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
    private boolean checkNoun(String tag) {
        if (tag.startsWith("N") || tag.startsWith("^")) {
            return true;
        }
        else
            return false;
    }
    public ArrayList<String> tag(String tweet) {

    	ArrayList<String> nounList = new ArrayList<String>();
        ArrayList<String> tagList = new ArrayList<String>();
        try {
			List<Tagger.TaggedToken> t = tagger.tokenizeAndTag(tweet);
        	for (Tagger.TaggedToken tags: t){
            	    System.out.println(tags.token+"\t"+tags.tag);
        	}
        	boolean flag = false;
        	int index = -1;
		for (Tagger.TaggedToken tags: t) {

                    tagList.add(tags.tag);

                    if (checkNoun(tags.tag) && flag == false) {
                        nounList.add(tags.token);
                        flag = true;
                        index++;
                    }
                    else if (checkNoun(tags.tag) && flag == true) {
                        String noun = nounList.get(index);
                        noun = noun + " " + tags.token;
                        nounList.set(index, noun); //replacing with updated noun phrase in arraylist
                    }
                    else
                        flag = false;
        	}


    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

    return nounList;
    }
	public String convert(String tweet) {
		String output = "";
		KStemmer st = new KStemmer();
        List<Tagger.TaggedToken> t = tagger.tokenizeAndTag(tweet);
       	output = st.stem(t.get(0).token);
		for(int i=1;i<t.size();i++) {
			if(!checkNoun(t.get(i).tag) || !checkNoun(t.get(i-1).tag))
				output += " " + st.stem(t.get(i).token);
			else
				output += "_" + st.stem(t.get(i).token);
		}
		return output;
    }
	public List<NounPhrase> extract(String tweet) {
		List<NounPhrase> nps = new ArrayList<NounPhrase>();
		List<Tagger.TaggedToken> t = tagger.tokenizeAndTag(tweet);
		nps.add(new NounPhrase(t.get(0).token, 0, 0));
		for(int i=1;i<t.size();i++) {
			if(!checkNoun(t.get(i).tag) || !checkNoun(t.get(i-1).tag))
				nps.add(new NounPhrase(t.get(i).token, i, i));
			else {
				NounPhrase np = nps.get(nps.size()-1);
				nps.set(nps.size()-1, new NounPhrase(np.text + " " + t.get(i).token, np.start, i));
			}
		}
		return nps;
    }
	
	public static void main(String[] args)
	{
		POSTagger t = new POSTagger();
		List<NounPhrase> nps = t.extract("President obama is coming to town");
		for(int i=0;i<nps.size();i++)
			System.out.println(nps.get(i).text + "\t" + nps.get(i).start + "\t" + nps.get(i).end);
	}
}
