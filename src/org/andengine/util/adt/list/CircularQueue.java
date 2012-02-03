package org.andengine.util.adt.list;

import java.util.Arrays;

/**
 * TODO This class could take some kind of AllocationStrategy object.
 *
 * This implementation is particular useful/efficient for enter/poll operations.
 * Its {@link java.util.Queue} like behavior performs better than a plain {@link java.util.ArrayList}, since it automatically shift the contents of its internal Array only when really necessary.
 * Besides sparse allocations to increase the size of the internal Array, {@link CircularQueue} is allocation free (unlike the {@link java.util.LinkedList} family).
 *
 * (c) Zynga 2012
 *
 * @author Greg Haynes
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 15:02:40 - 24.02.2012
 */
public class CircularQueue<T> implements IQueue<T>, IList<T> {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAPACITY_INITIAL_DEFAULT = 1;
	private static final int INDEX_INVALID = -1;

	// ===========================================================
	// Fields
	// ===========================================================

	private Object[] mItems;
	private int mHead;
	private int mSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	public CircularQueue() {
		this(CircularQueue.CAPACITY_INITIAL_DEFAULT);
	}

	public CircularQueue(final int pInitialCapacity) {
		this.mItems = new Object[pInitialCapacity];
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean isEmpty() {
		return this.mSize == 0;
	}

	@Override
	public void enter(final T pItem) {
		this.ensureCapacity();
		this.mItems[this.encodeToInternalIndex(this.mSize)] = pItem;
		this.mSize++;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(final int pIndex) throws ArrayIndexOutOfBoundsException {
		return (T) this.mItems[this.encodeToInternalIndex(pIndex)];
	}

	@Override
	public int indexOf(final T pItem) {
		final int size = this.size();
		if(pItem == null) {
			for(int i = 0; i < size; i++) {
				if(this.get(i) == null) {
					return i;
				}
			}
		} else {
			for(int i = 0; i < size; i++) {
				if(pItem.equals(this.get(i))) {
					this.remove(i);
					return i;
				}
			}
		}
		return CircularQueue.INDEX_INVALID;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T peek() {
		if(this.mSize == 0) {
			return null;
		} else {
			return (T) this.mItems[this.mHead];
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T poll() {
		if(this.mSize == 0) {
			return null;
		} else {
			final T item = (T) this.mItems[this.mHead];
			this.mItems[this.mHead] = null;
			this.mHead++;
			if(this.mHead == this.mItems.length) {
				this.mHead = 0;
			}
			this.mSize--;
			if(this.mSize == 0) {
				this.mHead = 0;
			}
			return item;
		}
	}

	@Override
	public void enter(final int pIndex, final T pItem) {
		int internalIndex = this.encodeToInternalIndex(pIndex);

		this.ensureCapacity();

		final int tail = this.encodeToInternalIndex(this.mSize);
		if(internalIndex == tail) {
			// nothing to shift, tail is free
		} else if(internalIndex == this.mHead) {
			this.mHead--;
			if(this.mHead == -1) {
				this.mHead = this.mItems.length - 1;
			}
			internalIndex--;
			if(internalIndex == -1) {
				internalIndex = this.mItems.length - 1;
			}
		} else if((internalIndex < this.mHead) || (this.mHead == 0)) {
			System.arraycopy(this.mItems, internalIndex, this.mItems, internalIndex + 1, tail - internalIndex);
		} else if(internalIndex > tail) {
			System.arraycopy(this.mItems, this.mHead, this.mItems, this.mHead - 1, pIndex);
			this.mHead--;
			if(this.mHead == -1) {
				this.mHead = this.mItems.length - 1;
			}
			internalIndex--;
			if(internalIndex == -1) {
				internalIndex = this.mItems.length - 1;
			}
		} else if(pIndex < (this.mSize >> 1)) {
			System.arraycopy(this.mItems, this.mHead, this.mItems, this.mHead - 1, pIndex);
			this.mHead--;
			if(this.mHead == -1) {
				this.mHead = this.mItems.length - 1;
			}
			internalIndex--;
			if(internalIndex == -1) {
				internalIndex = this.mItems.length - 1;
			}
		} else {
			System.arraycopy(this.mItems, internalIndex, this.mItems, internalIndex + 1, tail - internalIndex);
		}
		this.mItems[internalIndex] = pItem;
		this.mSize++;
	}

	@Override
	public void add(final T pItem) {
		this.enter(pItem);
	}

	@Override
	public void add(final int pIndex, final T pItem) throws ArrayIndexOutOfBoundsException {
		this.enter(pIndex, pItem);
	}

	@Override
	public boolean remove(final T pItem) {
		final int index = this.indexOf(pItem);
		if(index == CircularQueue.INDEX_INVALID) {
			return false;
		} else {
			this.remove(index);
			return true;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T remove(final int pIndex) {
		final int internalIndex = this.encodeToInternalIndex(pIndex);
		final T removed = (T) this.mItems[internalIndex];

		final int tail = this.encodeToInternalIndex(this.mSize - 1);

		if(internalIndex == tail) {
			this.mItems[tail] = null;
		} else if(internalIndex == this.mHead) {
			this.mItems[this.mHead] = null;
			this.mHead++;
			if(this.mHead == this.mItems.length) {
				this.mHead = 0;
			}
		} else if(internalIndex < this.mHead) {
			System.arraycopy(this.mItems, internalIndex + 1, this.mItems, internalIndex, tail - internalIndex);
			this.mItems[tail] = null;
		} else if(internalIndex > tail) {
			System.arraycopy(this.mItems, this.mHead, this.mItems, this.mHead + 1, pIndex);
			this.mItems[this.mHead] = null;
			this.mHead++;
			if(this.mHead == this.mItems.length) {
				this.mHead = 0;
			}
		} else if(pIndex < (this.mSize >> 1)) {
			System.arraycopy(this.mItems, this.mHead, this.mItems, this.mHead + 1, pIndex);
			this.mItems[this.mHead] = null;
			this.mHead++;
			if(this.mHead == this.mItems.length) {
				this.mHead = 0;
			}
		} else {
			System.arraycopy(this.mItems, internalIndex + 1, this.mItems, internalIndex, tail - internalIndex);
			this.mItems[tail] = null;
		}
		this.mSize--;

		return removed;
	}

	@Override
	public int size() {
		return this.mSize;
	}

	@Override
	public void clear() {
		Arrays.fill(this.mItems, null);
		this.mHead = 0;
		this.mSize = 0;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void ensureCapacity() {
		final int currentCapacity = this.mItems.length;
		if(this.mSize == currentCapacity) {
			final int newCapacity = ((currentCapacity * 3) >> 1) + 1;
			final Object newItems[] = new Object[newCapacity];

			System.arraycopy(this.mItems, this.mHead, newItems, 0, this.mSize - this.mHead);
			System.arraycopy(this.mItems, 0, newItems, this.mSize - this.mHead, this.mHead);

			this.mItems = newItems;
			this.mHead = 0;
		}
	}

	private int encodeToInternalIndex(final int pIndex) {
		int internalIndex = this.mHead + pIndex;
		if(internalIndex >= this.mItems.length) {
			internalIndex -= this.mItems.length;
		}
		return internalIndex;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}