package ciir.umass.edu.sum.simmeasure;

import java.util.ArrayList;
import java.util.List;

import ciir.umass.edu.utilities.Sorter;

public class ItemArrayList extends ItemList {
	private List<Item> list = new ArrayList<Item>();
	private int[] idx = null;
	
	public void add(Item i)
	{
		list.add(i);
	}
	public int size()
	{
		return list.size();
	}
	public Item get(int i)
	{
		if(i >= list.size())
			return null;
		
		if(idx == null)
			return list.get(i);
		return list.get(idx[i]);
	}
	public void clear()
	{
		list.clear();
		idx = null;
	}
	
	public void sort(boolean asc)
	{
		List<String> l = new ArrayList<String>();
		for(int i=0;i<list.size();i++)
			l.add(list.get(i).name());
		idx = Sorter.sortString(l, asc);
	}
}
