package ciir.umass.edu.tweet;
import java.io.*;
import java.util.*;

import ciir.umass.edu.qproc.POSTagger;
/**
 * Created by ashishjain on 4/1/14.
 */
public class Phrase {
    //This function retrieves phrase frequency for a given entity

    public static void main(String[] args) {
    try {
        POSTagger tagger = new POSTagger();
        File folder = new File("Nov");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            Map <String, Integer> hm = new HashMap<String, Integer>();
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                BufferedReader br = new BufferedReader(new FileReader("Nov/"+listOfFiles[i].getName()));
                String line;
                while ((line = br.readLine()) != null) {
                    // process the line.
                    List<String> nps = tagger.tag(line);
                    Iterator itr =  nps.iterator();
                    while(itr.hasNext()) {
                        String key = itr.next().toString();
                        Integer val = hm.get(key);
			System.out.println(key);
                        if (val!=null)
                            hm.put(key, val + 1);
                        else
                            hm.put(key, 1);
                    }
                }
                br.close();

            }
            PrintWriter writer = new PrintWriter("NovPhrase/"+listOfFiles[i].getName(), "UTF-8");
            for (Map.Entry<String, Integer> entry : hm.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
            writer.close();
        }
    }
    catch(Exception e) {
        System.out.println("Error in HierarchyStatistics.save(): " + e.toString());
    }
   }
}
