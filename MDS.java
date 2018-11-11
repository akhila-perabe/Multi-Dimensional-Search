/**
 * @author Akhila Perabe (axp178830), Pooja Srinivasan (pxs176230), Shreeya Girish Degaonkar (sxd174830)
 * Multi Dimensional Search algorithm
 */

package axp178830;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.HashSet;
import  java.util.Set;

public class MDS {

	public class Item {
		Long id;
		Money price;
		List<Long> description;

		public Item (Long id, Money price, List<Long> description) {
			this.id = id;
			this.price = price;
			this.description =  new ArrayList<>(description);
		}

		public void updateItem(Money price, List<Long> description) {
			if(description!=null && description.size()!=0) {
				this.description = new ArrayList<>(description);
			}
			this.price = price;
		}

		public String toString() {
			return "{Id=" + id + ", Price=" + price + ", Description=" + description + "]}";
		}
	}

	TreeMap<Long, Item> tree;
	HashMap<Long, TreeMap<Money, Integer>> table;

	// Constructors
	public MDS() {
		tree = new TreeMap<>();
		table = new HashMap<>();
	}

	/*
	 * Public methods of MDS. Do not change their signatures.
	 * __________________________________________________________________ a.
	 * Insert(id,price,list): insert a new item whose description is given in the
	 * list. If an entry with the same id already exists, then its description and
	 * price are replaced by the new values, unless list is null or empty, in which
	 * case, just the price is updated. Returns 1 if the item is new, and 0
	 * otherwise.
	 */
	public int insert(long id, Money price, List<Long> list) {
		Item item = tree.get(id);
		if(item != null) {
			//Element already exists
			removeFromTable(item.price, item.description);
			item.updateItem(price, list);
			addToTable(item.price, item.description);
			return 0;
		} else {
			// New element
			tree.put(id, new Item(id, price, list));
			addToTable(price, list);
			return 1;
		}
	}

	// b. Find(id): return price of item with given id (or 0, if not found).
	public Money find(long id) {
		Item item = tree.get(id);
		if (item != null)
			return item.price;
		else
			return ZeroDollars();
	}

	/*
	 * c. Delete(id): delete item from storage. Returns the sum of the long ints
	 * that are in the description of the item deleted, or 0, if such an id did not
	 * exist.
	 */
	public long delete(long id) {
		Item item = tree.remove(id);
		if(item != null) {
			return removeFromTable(item.price, item.description);
		} else {
			return 0;
		}
	}

	/*
	 * d. FindMinPrice(n): given a long int, find items whose description contains
	 * that number (exact match with one of the long ints in the item's
	 * description), and return lowest price of those items. Return 0 if there is no
	 * such item.
	 */
	public Money findMinPrice(long n) {
		if(n==1389) {
			System.out.println("hello");
		}
		TreeMap<Money, Integer> item = table.get(n);
		if (item != null) {
			return item.firstKey();
		} else {
			return ZeroDollars();
		}
	}

	/*
	 * e. FindMaxPrice(n): given a long int, find items whose description contains
	 * that number, and return highest price of those items. Return 0 if there is no
	 * such item.
	 */
	public Money findMaxPrice(long n) {
		TreeMap<Money, Integer> item = table.get(n);
		if (item != null) {
			return item.lastKey();
		} else {
			return ZeroDollars();
		}	}

	/*
	 * f. FindPriceRange(n,low,high): given a long int n, find the number of items
	 * whose description contains n, and in addition, their prices fall within the
	 * given range, [low, high].
	 */
	public int findPriceRange(long n, Money low, Money high) {
		TreeMap<Money, Integer> item = table.get(n);
		if (item != null) {
			if (low.compareTo(high) > 0) {
				Money temp = low;
				low = high;
				high = temp;
			}
			return item.subMap(low, high).size();
		} else {
			return 0;
		}
	}

	/*
	 * g. PriceHike(l,h,r): increase the price of every product, whose id is in the
	 * range [l,h] by r%. Discard any fractional pennies in the new prices of items.
	 * Returns the sum of the net increases of the prices.
	 */
	public Money priceHike(long l, long h, double rate) {
		if (l > h) {
			long temp = l;
			l = h;
			h = temp;
		}
		NavigableMap<Long,Item> map = tree.subMap(l, true, h, true);
		double totalHike = 0;

		rate = rate/100;

		for(Item item: map.values()) {
			//Remove the current price from table
			removeFromTable(item.price, item.description);

			//Update price
			totalHike += item.price.hikeBy(rate);

			//Add the new price
			addToTable(item.price, item.description);

		}

		totalHike = Math.floor(totalHike*100);

		double temp = totalHike/100;
		int dollar = (int)temp;
		int cents = (int)((temp - dollar)*100);
		return new Money(dollar, cents);
	}

	/*
	 * h. RemoveNames(id, list): Remove elements of list from the description of id.
	 * It is possible that some of the items in the list are not in the id's
	 * description. Return the sum of the numbers that are actually deleted from the
	 * description of id. Return 0 if there is no such id.
	 */

	public long removeNames(long id, List<Long> list) {
		Item item = tree.get(id);
		if(item != null) {
			List<Long> validDescr = new ArrayList<>();
			Set<Long> descriptions = new HashSet<>(item.description);
			for(Long desc : list){
				if(descriptions.contains(desc)){
					validDescr.add(desc);
					item.description.remove(desc);
				}
			}
			if(validDescr.size()==0) return 0;
			return removeFromTable(item.price, validDescr);
		} else {
			return 0;
		}
	}

	private void addToTable(Money price, List<Long> list) {
		for (Long descr: list) {
			TreeMap<Money, Integer> entry = table.get(descr);
			if (entry == null ) {
				// New description
				TreeMap<Money, Integer> m = new TreeMap<>();
				m.put(price, 1);
				table.put(descr, m);
			} else {
				Integer count = entry.get(price);
				if(count == null) {
					//Add new price
					entry.put(price, 1);
				} else {
					//Update price counts
					entry.replace(price, count.intValue() + 1);
				}
			}
		}
	}

	private long removeFromTable(Money price, List<Long> list) {
		long total = 0;
		for (Long descr: list) {
			TreeMap<Money, Integer> entry = table.get(descr);
			if (entry != null) {
				total += descr;

				Integer count = entry.get(price);
				if(count != null) {
					if(count == 1) {
						//Remove price when only 1 item for this price
						entry.remove(price);
						if(entry.size()==0) table.remove(descr);
					} else {
						//Update price count
						entry.replace(price, count.intValue() - 1);
					}

				}
			}
		}
		return total;
	}

	private Money ZeroDollars() {
		return new Money(0,0);
	}

	// Do not modify the Money class in a way that breaks LP3Driver.java
	public static class Money implements Comparable<Money> {
		long d;
		int c;

		public Money() {
			d = 0;
			c = 0;
		}

		public Money(long d, int c) {
			this.d = d;
			this.c = c;
		}

		public Money(String s) {
			String[] part = s.split("\\.");
			int len = part.length;
			if (len < 1) {
				d = 0;
				c = 0;
			} else if (part.length == 1) {
				d = Long.parseLong(s);
				c = 0;
			} else {
				d = Long.parseLong(part[0]);
				c = Integer.parseInt(part[1]);
			}
		}

		public long dollars() {
			return d;
		}

		public int cents() {
			return c;
		}

		public int compareTo(Money other) { // Complete this, if needed
			if(d < other.d) {
				return -1;
			} else if (d > other.d) {
				return 1;
			} else {
				if(c < other.c) {
					return -1;
				} else if (c > other.c) {
					return 1;
				} else {
					return 0;
				}
			}
		}

		public String toString() {
			return d + "." + c;
		}

		private double hikeBy(double rate) {
			double price = d*100+c;
			double oldPrice = d + 0.01*c;



			price += price*rate;

			price = Math.floor(price);
			double temp = price/100;
			double rem = price%100; //have to check
			d = (int)temp;
			//c = (int)((temp-d)*100);
			c = (int)rem;
			return (temp - oldPrice);

			/*long price = d*100+c;
			String r= String.valueOf(rate);
			String[] tokens = r.split(".");
			StringBuffer sb = new StringBuffer();
			sb.append(tokens[0]);
			if(tokens.length==2){
				sb.append(tokens[1]);
			}
            long r = Long.parseLong(sb.toString());
			*/
		}
	}

}
