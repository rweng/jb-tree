/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package de.rwhq.btree;

import de.rwhq.btree.BTree.NodeType;
import de.rwhq.btree.LeafNode.Header;
import de.rwhq.comparator.IntegerComparator;
import de.rwhq.io.rm.PageManager;
import de.rwhq.io.rm.RawPage;
import de.rwhq.serializer.IntegerSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @deprecated 
 */
public class LeafNodeUnitTest {
	
	private LeafNode<Integer, Integer> node;
	
	// dependencies
	private RawPage rawPage;
	private int minNumberOfValues = 3;
	private int rawPageSize = 34;
	@Mock private PageManager<LeafNode<Integer, Integer>> leafPageManager;


	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this); 
		rawPage = new RawPage(ByteBuffer.allocate(rawPageSize), 100);
		final RawPage rawPage2 = new RawPage(ByteBuffer.allocate(rawPageSize), 101);
		node = new LeafNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE, leafPageManager, minNumberOfValues);
		node.initialize();
	}
	
	@Test
	public void load(){
		node.insert(1, 101);
		node.insert(2, 201);
		assertThat( node.getNumberOfEntries()).isEqualTo(2);
		
		node = new LeafNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE, leafPageManager, minNumberOfValues);
		
		
		node.load();
		assertThat( node.getNumberOfEntries()).isEqualTo(2);
		assertThat( (int) node.getFirst(1)).isEqualTo(101);
		assertThat( (int) node.getFirst(2)).isEqualTo(201);
		
	}
	
	@Test
	public void destroy(){
		node.insert(1, 101);
		node.destroy();
		verify(leafPageManager).removePage(100);
	}
	
	@Test
	public void minNumberOfValues(){
		final int tmpValues = minNumberOfValues;
		final int tmpSize = rawPageSize;
		
		minNumberOfValues = 1;
		rawPageSize = Header.size() + 2*IntegerSerializer.INSTANCE.getSerializedLength();
		
		// this should work
		setUp();
		
		// this shouldn't work
		try{
			rawPageSize--;
			setUp();
			throw new IllegalStateException("this shouldn't work");
		} catch (Exception e) {
		}
		
		minNumberOfValues = 0;
		rawPageSize = Header.size();
		
		// should work
		setUp();
		
		// this shouldn't work
		try{
			rawPageSize--;
			setUp();
			throw new IllegalStateException("this shouldn't work");
		} catch (Exception e) {
		}
		
		minNumberOfValues = 2;
		rawPageSize = Header.size() + 4*IntegerSerializer.INSTANCE.getSerializedLength();
		
		// should work
		setUp();
		
		// this shouldn't work
		try{
			rawPageSize--;
			setUp();
			throw new IllegalStateException("this shouldn't work");
		} catch (Exception e) {
		}
		
		rawPageSize +=2;
		
		// should work
		setUp();
		
		// reset values
		minNumberOfValues = tmpValues;
		rawPageSize = tmpSize;
	}
	
	@Test
	public void testInitialize(){
		final ByteBuffer buf = rawPage.bufferForReading(0);
		assertThat( buf.getChar()).isEqualTo(NodeType.LEAF_NODE.serialize());
		assertThat( buf.getInt()).isEqualTo(0);
		assertThat( buf.getInt()).isEqualTo((int)LeafNode.NO_NEXT_LEAF);
	}
	
	@Test
	public void firstInsert(){
		node.insert(1, 101);
		ensureKeyValueInRawPage(rawPage, Header.size(), 1, 101);
		
		assertThat( node.get(1).size()).isEqualTo(1);
		assertThat( (int) node.get(1).get(0)).isEqualTo(101);
	}
	
	private void ensureKeyValueInRawPage(final RawPage rp, final int offset, final int key, final int value){
		final ByteBuffer buf = rp.bufferForReading(offset);
		final byte[] bytes = new byte[IntegerSerializer.INSTANCE.getSerializedLength()];
		buf.get(bytes);
		assertThat( (int) IntegerSerializer.INSTANCE.deserialize(bytes)).isEqualTo(key);
		buf.get(bytes);
		assertThat( (int) IntegerSerializer.INSTANCE.deserialize(bytes)).isEqualTo(value);
	}
	
	@Test
	public void secondInsert(){
		firstInsert();
		node.insert(10, 1001);
		ensureKeyValueInRawPage(rawPage, Header.size() + 2*IntegerSerializer.INSTANCE.getSerializedLength(), 10, 1001);
		

		assertThat( node.get(1).size()).isEqualTo(1);
		assertThat( (int) node.get(1).get(0)).isEqualTo(101);

		assertThat( node.get(10).size()).isEqualTo(1);
		assertThat( (int) node.get(10).get(0)).isEqualTo(1001);
	}
	
	@Test
	public void doubleInsert(){
		secondInsert();
		node.insert(1, 102);
		ensureKeyValueInRawPage(rawPage, Header.size(), 1, 102);
		
		assertThat( node.get(1).size()).isEqualTo(2);
		assertThat( (int) node.get(1).get(0)).isEqualTo(102);
		assertThat( (int) node.get(1).get(1)).isEqualTo(101);
	}
	
	@Test
	public void insertionInTheMiddle(){
		secondInsert();
		node.insert(5, 501);
		ensureKeyValueInRawPage(rawPage, Header.size() + 0*IntegerSerializer.INSTANCE.getSerializedLength(), 1, 101);
		ensureKeyValueInRawPage(rawPage, Header.size() + 2*IntegerSerializer.INSTANCE.getSerializedLength(), 5, 501);
		ensureKeyValueInRawPage(rawPage, Header.size() + 4* IntegerSerializer.INSTANCE.getSerializedLength(), 10, 1001);
	}
}
