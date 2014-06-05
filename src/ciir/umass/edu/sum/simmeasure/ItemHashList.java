package ciir.umass.edu.sum.simmeasure;

import java.util.Enumeration;
import java.util.Hashtable;

public class ItemHashList extends ItemList {
	private Hashtable<String, Item> ht = new Hashtable<String, Item>();
	public void add(Item i)
	{
		ht.put(i.name(), i);
	}
	public int size()
	{
		return ht.size();
	}
	public Item get(String itemName)
	{
		return ht.get(itemName);
	}
	public void clear()
	{
		ht.clear();
	}
	public Enumeration<String> keys()
	{
		return ht.keys();
	}
}
