/**
 * @author Akhila Perabe (axp178830), Pooja Srinivasan (pxs176230), Shreeya Girish Degaonkar (sxd174830)
 * Multi Dimensional Search algorithm
 */

package axp178830;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MDS {

	public class Item {
		Long id;
		Money price;
		Set<Long> description;

		public Item (Long id, Money price, List<Long> description) {
			this.id = id;
			this.price = price;
			this.description =  new HashSet<>(description);
		}

		public void updateItem(Money price, List<Long> description) {
			if(description!=null && description.size()!=0) {
				this.description = new HashSet<>(description);
			}
			this.price = price;
		}

		public String toString() {
			return "{Id=" + id + ", Price=" + price + ", Description=" + description + "]}";
		}
	}

	TreeMap<Long, Item> tree;
	HashMap<Long, TreeMap<Money, Integer>> table;

	/**
	 *  Constructor
	 */
	public MDS() {
		tree = new TreeMap<>();
		table = new HashMap<>();
	}

	/*
	 * Public methods of MDS. Do not change their signatures.
	 * __________________________________________________________________
	 */
	
	/**
	 * a. Insert(id,price,list): insert a new item whose description is given in the
	 * list. If an entry with the same id already exists, then its description and
	 * price are replaced by the new values, unless list is null or empty, in which
	 * case, just the price is updated. Returns 1 if the item is new, and 0
	 * otherwise.
	 * @param id
	 * @param price
	 * @param list
	 * @return
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
			addToTable(price, new HashSet<>(list));
			return 1;
		}
	}

	/**
	 *  b. Find(id): return price of item with given id (or 0, if not found).
	 * @param id
	 * @return
	 */
	public Money find(long id) {
		Item item = tree.get(id);
		if (item != null)
			return item.price;
		else
			return ZeroDollars();
	}

	/**
	 * c. Delete(id): delete item from storage. Returns the sum of the long ints
	 * that are in the description of the item deleted, or 0, if such an id did not
	 * exist.	 * @param id
	 * @return
	 */
	public long delete(long id) {
		Item item = tree.remove(id);
		if(item != null) {
			return removeFromTable(item.price, item.description);
		} else {
			return 0;
		}
	}

	/**
	 * d. FindMinPrice(n): given a long int, find items whose description contains
	 * that number (exact match with one of the long ints in the item's
	 * description), and return lowest price of those items. Return 0 if there is no
	 * such item.
	 * @param n
	 * @return
	 */
	public Money findMinPrice(long n) {
		TreeMap<Money, Integer> item = table.get(n);
		if (item != null) {
			return item.firstKey();
		} else {
			return ZeroDollars();
		}
	}

	/**
	 * e. FindMaxPrice(n): given a long int, find items whose description contains
	 * that number, and return highest price of those items. Return 0 if there is no
	 * such item.
	 * @param n
	 * @return
	 */
	public Money findMaxPrice(long n) {
		TreeMap<Money, Integer> item = table.get(n);
		if (item != null) {
			return item.lastKey();
		} else {
			return ZeroDollars();
		}	}

	/**
	 * f. FindPriceRange(n,low,high): given a long int n, find the number of items
	 * whose description contains n, and in addition, their prices fall within the
	 * given range, [low, high].
	 * @param n
	 * @param low
	 * @param high
	 * @return
	 */
	public int findPriceRange(long n, Money low, Money high) {
		
		// If given range is invalid
	    if (low.compareTo(high) > 0) {
			return 0;
		}
	    
		TreeMap<Money, Integer> item = table.get(n);
		if (item != null) {
			int count = 0;
			SortedMap<Money,Integer> priceMap =  item.subMap(low, true, high, true);
            Iterator<Map.Entry<Money, Integer>> itr = priceMap.entrySet().iterator();
            while(itr.hasNext())
            {
                Map.Entry<Money, Integer> entry = itr.next();
                count+=entry.getValue();
            }
            return count;
		} else {
			return 0;
		}
	}

	/**
	 * g. PriceHike(l,h,r): increase the price of every product, whose id is in the
	 * range [l,h] by r%. Discard any fractional pennies in the new prices of items.
	 * Returns the sum of the net increases of the prices.
	 * @param l
	 * @param h
	 * @param rate
	 * @return
	 */
	public Money priceHike(long l, long h, double rate) {
		if(l > h) {
			return ZeroDollars();
		}
		
		NavigableMap<Long,Item> map = tree.subMap(l, true, h, true);
		double totalHike = 0;

		rate = rate/100;

		for(Item item: map.values()) {
			//Remove the current price from table
			removeFromTable(item.price, item.description);
			//Update price
			totalHike += item.price.hikeBy(rate);
			//Add the new price to table
			addToTable(item.price, item.description);
		}

		totalHike = Math.floor(totalHike*100);

		double temp = totalHike/100;
		int dollar = (int)temp;
		int cents = (int)((temp - dollar)*100);
		return new Money(dollar, cents);
	}

	/**
	 * h. RemoveNames(id, list): Remove elements of list from the description of id.
	 * It is possible that some of the items in the list are not in the id's
	 * description. Return the sum of the numbers that are actually deleted from the
	 * description of id. Return 0 if there is no such id.
	 * @param id
	 * @param list
	 * @return
	 */
	public long removeNames(long id, List<Long> list) {
		Item item = tree.get(id);
		if(item != null) {
			Set<Long> validDescr = new HashSet<>();
			Set<Long> descriptions = item.description;
			for(Long desc : list){
				boolean removed = descriptions.remove(desc);
				if(removed){
					validDescr.add(desc);
				}
			}
			if(validDescr.size()==0) return 0;
			return removeFromTable(item.price, validDescr);
		} else {
			return 0;
		}
	}

	/**
	 * Add the price to the table for all the elements in the given list
	 * @param money
	 * @param list
	 */
	private void addToTable(Money money, Set<Long> list) {

		Money price = new Money(money.d, money.c);
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
                    entry.put(price,count.intValue()+1);
				}
			}
        }
	}

	/**
	 * Removes the price for all elements in the given list
	 * @param price
	 * @param list
	 * @return
	 */
	private long removeFromTable(Money price, Set<Long> list) {
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
						entry.put(price, count.intValue() - 1);
					}
				}
			}
		}
		return total;
	}

	/**
	 * Returns a  zero dollar object
	 * @return
	 */
	private Money ZeroDollars() {
		return new Money(0,0);
	}

	/**
	 *   Money class
	 */
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
		
		public boolean equals(Object o) {
			    if (o == this) {
			      return true;
			    }
			    if (!(o instanceof Money)) {
			      return false;
			    }
			    Money m = (Money)o;
			    if(m.d == this.d && m.c == this.c)
			    	return true;
			    else
			    	return false;
		}

		public String toString() {
			return d + "." + c;
		}

		private double hikeBy(double rate) {
			BigDecimal hundred = new BigDecimal(100);
			BigDecimal dollar = new BigDecimal(d);
			BigDecimal cent = new BigDecimal(c);
			BigDecimal price = dollar.multiply(hundred).add(cent);
			BigDecimal oldPrice = price.divide(hundred);
			
			price = price.add(price.multiply(new BigDecimal(rate)));
			price = price.setScale(0, RoundingMode.FLOOR);
			BigDecimal[] arr = price.divideAndRemainder(hundred);
			d = arr[0].longValue();
			c = arr[1].intValue();
			
			return price.divide(hundred).subtract(oldPrice).doubleValue();
		}
	}

}
