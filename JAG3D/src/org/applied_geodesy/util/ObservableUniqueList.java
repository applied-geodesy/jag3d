/***********************************************************************
 * Copyright by Michael Loesler, https://software.applied-geodesy.org   *
 *                                                                      *
 * This program is free software; you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation; either version 3 of the License, or    *
 * at your option any later version.                                    *
 *                                                                      *
 * This program is distributed in the hope that it will be useful,      *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of       *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
 * GNU General Public License for more details.                         *
 *                                                                      *
 * You should have received a copy of the GNU General Public License    *
 * along with this program; if not, see <http://www.gnu.org/licenses/>  *
 * or write to the                                                      *
 * Free Software Foundation, Inc.,                                      *
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
 *                                                                      *
 ***********************************************************************/

package org.applied_geodesy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;

// https://github.com/shitapatilptc/java/blob/master/src/javafx.base/com/sun/javafx/collections/ObservableSequentialListWrapper.java
public class ObservableUniqueList<T> extends ModifiableObservableListBase<T> implements ObservableList<T> {
	private final List<T> list;
	private final Set<T> set;

	public ObservableUniqueList() {
		this.list = new ArrayList<T>();
		this.set  = new HashSet<T>();
	}

	public ObservableUniqueList(int size) {
		this.list = new ArrayList<T>(size);
		this.set  = new HashSet<T>(size);
	}

	//	@Override
	//	protected void doAdd(int index, T element) {
	//		if (element == null)
	//			throw new NullPointerException("Error, list does not support null values!");
	//		if (this.set.contains(element))
	//			throw new IllegalArgumentException("Error, list elements must be unique but element already exists " + element + "!");
	//		
	//		this.set.add(element);
	//		this.list.add(index, element);
	//	}
	//
	//	@Override
	//	protected T doRemove(int index) {
	//		T element = this.list.remove(index);
	//		this.set.remove(element);
	//		return element;
	//	}
	//
	//	@Override
	//	protected T doSet(int index, T element) {
	//		if (element == null)
	//			throw new NullPointerException("Error, list does not support null values!");
	//		if (this.set.contains(element))
	//			throw new IllegalArgumentException("Error, list elements must be unique but element already exists " + element + "!");
	//		
	//		this.set.add(element);
	//		return this.list.set(index, element);
	//	}
	//
	//	@Override
	//	public T get(int index) {
	//		return this.list.get(index);
	//	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.set.containsAll(c);
	}

	@Override
	public int indexOf(Object o) {
		return this.list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.list.lastIndexOf(o);
	}

	@Override
	public void clear() {
		this.set.clear();
		this.list.clear();
	}

	@Override
	public boolean contains(Object object) {
		return this.set.contains(object);
	}

	@Override
	public ListIterator<T> listIterator(final int index) {
		return new ListIterator<T>() {

			private final ListIterator<T> backingIt = list.listIterator(index);
			private T lastReturned;

			@Override
			public boolean hasNext() {
				return this.backingIt.hasNext();
			}

			@Override
			public T next() {
				return this.lastReturned = this.backingIt.next();
			}

			@Override
			public boolean hasPrevious() {
				return this.backingIt.hasPrevious();
			}

			@Override
			public T previous() {
				return this.lastReturned = this.backingIt.previous();
			}

			@Override
			public int nextIndex() {
				return this.backingIt.nextIndex();
			}

			@Override
			public int previousIndex() {
				return this.backingIt.previousIndex();
			}

			@Override
			public void remove() {
				try {
					beginChange();
					int idx = previousIndex();
					this.backingIt.remove();
					nextRemove(idx, this.lastReturned);
					set.remove(this.lastReturned);
				}
				finally {
					endChange();
				}
			}

			@Override
			public void set(T e) {
				if (e == null)
					throw new NullPointerException("Error, list does not support null values!");
				if (set.contains(e))
					throw new IllegalArgumentException("Error, list elements must be unique but element already exists " + e + "!");

				try {
					beginChange();
					int idx = previousIndex();
					this.backingIt.set(e);
					set.add(e);
					nextSet(idx, this.lastReturned);
				}
				finally {
					endChange();
				}
			}

			@Override
			public void add(T e) {
				if (e == null)
					throw new NullPointerException("Error, list does not support null values!");
				if (set.contains(e))
					throw new IllegalArgumentException("Error, list elements must be unique but element already exists " + e + "!");

				try {
					beginChange();
					int idx = nextIndex();
					this.backingIt.add(e);
					set.add(e);
					nextAdd(idx, idx + 1);
				}
				finally {
					endChange();
				}
			}
		};
	}

	@Override
	public Iterator<T> iterator() {
		return listIterator();
	}

	@Override
	public T get(int index) {
		try {
			return this.list.listIterator(index).next();
		} 
		catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		try {
			beginChange();
			boolean modified = false;
			ListIterator<T> e1 = this.listIterator(index);
			Iterator<? extends T> e2 = c.iterator();
			while (e2.hasNext()) {
				e1.add(e2.next());
				modified = true;
			}
			return modified;
		} 
		catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		}
		finally {
			endChange();
		}
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		try {
			Objects.requireNonNull(c);
			beginChange();
			boolean modified = false;
			if (size() > c.size()) {
				for (Iterator<?> i = c.iterator(); i.hasNext(); )
					modified |= this.remove(i.next());
			} 
			else {
				Collection<?> removals = null;
				if (c instanceof ObservableUniqueList)
					removals = c;
				else
					removals = new HashSet<>(c);

				for (Iterator<?> i = iterator(); i.hasNext(); ) {
					if (removals.contains(i.next())) {
						i.remove();
						modified = true;
					}
				}
			}
			return modified;
		}
		finally {
			endChange();
		}
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	protected void doAdd(int index, T element) {
		if (element == null)
			throw new NullPointerException("Error, list does not support null values!");
		if (this.set.contains(element))
			throw new IllegalArgumentException("Error, list elements must be unique but element already exists " + element + "!");

		try {
			this.list.listIterator(index).add(element);
			this.set.add(element);
		} catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		}
	}

	@Override
	protected T doSet(int index, T element) {
		if (element == null)
			throw new NullPointerException("Error, list does not support null values!");
		if (this.set.contains(element))
			throw new IllegalArgumentException("Error, list elements must be unique but element already exists " + element + "!");

		try {
			ListIterator<T> e = this.list.listIterator(index);
			T oldVal = e.next();
			this.set.remove(oldVal);
			this.set.add(element);
			e.set(element);
			return oldVal;
		} 
		catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		}
	}

	@Override
	protected T doRemove(int index) {
		try {
			ListIterator<T> e = this.list.listIterator(index);
			T element = e.next();
			this.set.remove(element);
			e.remove();
			return element;
		} 
		catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		}
	}
}
