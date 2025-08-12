package com.mysql.cj.protocol.a.result;

/**
 * 高效环形缓冲实现，支持 O(1) 的头部移除操作，
 * 并新增按需随机访问 get(index) 与按指定容量调整 resizeTo().
 * <p>
 * 注意：这个实现不是线程安全的；若多个线程并发访问需额外同步/上锁。
 */
public class CircularBuffer<E> {
    private E[] buffer;
    private int head = 0;  // 头部指针
    private int tail = 0;  // 尾部指针（始终指向下一个可写位置）
    private int size = 0;  // 当前元素数量
    private int capacity;  // 总容量

    @SuppressWarnings("unchecked")
    public CircularBuffer(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be positive");
        }
        this.capacity = initialCapacity;
        this.buffer = (E[]) new Object[capacity];
    }

    /**
     * 添加元素到缓冲尾部
     */
    public void add(E element) {
        if (size == capacity) {
            // 自动扩容
            resize();
        }

        buffer[tail] = element;
        tail = (tail + 1) % capacity;
        size++;
    }

    /**
     * 移除并返回头部元素
     */
    public E removeFirst() {
        if (size == 0) {
            throw new java.util.NoSuchElementException("Buffer is empty");
        }

        E element = buffer[head];
        buffer[head] = null; // 清除引用，帮助GC
        head = (head + 1) % capacity;
        size--;
        return element;
    }

    /**
     * 获取但不移除头部元素
     */
    public E peekFirst() {
        if (size == 0) {
            return null;
        }
        return buffer[head];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    /**
     * 随机访问（0..size-1），0 表示头部元素
     */
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        }
        int pos = (head + index) % capacity;
        return buffer[pos];
    }

    /**
     * 把缓冲调整到指定容量（newCapacity 必须 > 当前 size，否则会至少增到 size+1）
     */
    @SuppressWarnings("unchecked")
    public void resizeTo(int newCapacity) {
        if (newCapacity <= size) {
            newCapacity = size + 1;
        }
        E[] newBuffer = (E[]) new Object[newCapacity];

        for (int i = 0; i < size; i++) {
            newBuffer[i] = get(i);
        }

        this.buffer = newBuffer;
        this.capacity = newCapacity;
        this.head = 0;
        this.tail = size % capacity;
    }

    /**
     * 动态扩容（倍增），内部调用 resizeTo
     */
    private void resize() {
        resizeTo(capacity * 2);
    }

    /**
     * 清除缓冲并释放内存
     */
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buffer[i] = null; // 清除所有引用
        }
        head = tail = size = 0;
    }
}
