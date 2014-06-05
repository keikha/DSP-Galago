package ciir.umass.edu.sum.simmeasure;

public class StringDistance {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//int dist = StringDistance.LevenshteinCharDistance("sunday", "saturday");
		int dist = StringDistance.LevenshteinWordDistance("I go to", "I to");
		System.out.println("Edit distance = " + dist);
	}
	
	public static int LevenshteinCharDistance(String s1, String s2)
	{
		char[] st = (" " + s1).toCharArray();
		char[] dt = (" " + s2).toCharArray();
		int m = st.length;
		int n = dt.length;
		int[][] d = new int[m][n];
		for(int i=0;i<m;i++)
			d[i][0] = i;
		for(int j=0;j<n;j++)
			d[0][j] = j;
		int cost;
		int[] ins_upt_del = new int[3];
		for(int i=1;i<m;i++)
		{
			for(int j=1;j<n;j++)
			{
				if(st[i] == dt[j])
					cost = 0;
				else
					cost = 1;
				ins_upt_del[0] = d[i-1][j] + 1;//insertion
				ins_upt_del[1] = d[i][j-1] + 1;//deletion
				ins_upt_del[2] = d[i-1][j-1] + cost;//substitution
				d[i][j] = ins_upt_del[0];
				for(int k=1;k<ins_upt_del.length;k++)
					if(d[i][j] > ins_upt_del[k])
						d[i][j] = ins_upt_del[k];
			}
		}
		
		/*for(int i=0;i<m;i++)
		{
			for(int j=0;j<n;j++)
			{
				System.out.print(d[i][j] + "\t");
			}
			System.out.println("");
		}*/
		return d[m-1][n-1];
	}
	public static int LevenshteinWordDistance(String s1, String s2)
	{
		String[] st = ("<DUMMY> " + s1).split(" ");
		String[] dt = ("<DUMMY> " + s2).split(" ");
		//System.out.println(st.length + " - " + dt.length);
		int m = st.length;
		int n = dt.length;
		int[][] d = new int[m][n];
		for(int i=0;i<m;i++)
			d[i][0] = i;
		for(int j=0;j<n;j++)
			d[0][j] = j;
		int cost;
		int[] ins_upt_del = new int[3];
		for(int i=1;i<m;i++)
		{
			for(int j=1;j<n;j++)
			{
				if(st[i].compareTo(dt[j])==0)
					cost = 0;
				else
					cost = 1;
				ins_upt_del[0] = d[i-1][j] + 1;//insertion
				ins_upt_del[1] = d[i][j-1] + 1;//deletion
				ins_upt_del[2] = d[i-1][j-1] + cost;//substitution
				d[i][j] = ins_upt_del[0];
				for(int k=1;k<ins_upt_del.length;k++)
					if(d[i][j] > ins_upt_del[k])
						d[i][j] = ins_upt_del[k];
			}
		}
		
		/*for(int i=0;i<m;i++)
		{
			for(int j=0;j<n;j++)
			{
				System.out.print(d[i][j] + "\t");
			}
			System.out.println("");
		}*/
		return d[m-1][n-1];
	}
}
