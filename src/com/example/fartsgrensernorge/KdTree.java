package com.example.fartsgrensernorge;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import java.util.zip.*;

import android.os.Environment;
import android.util.Log;

/**
 * A k-d tree (short for k-dimensional tree) is a space-partitioning data
 * structure for organizing points in a k-dimensional space. k-d trees are a
 * useful data structure for several applications, such as searches involving a
 * multidimensional search key (e.g. range searches and nearest neighbor
 * searches). k-d trees are a special case of binary space partitioning trees.
 * 
 * http://en.wikipedia.org/wiki/K-d_tree
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class KdTree<T extends KdTree.XYZPoint> {

	private KdNode root = null;
	private static int maxDepth = 0;
	private static File externalFilesDir;

	public KdTree(File externalFilesDir) {
		this.externalFilesDir = externalFilesDir;
	}


	private static final Comparator<XYZPoint> X_COMPARATOR = new Comparator<XYZPoint>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(XYZPoint o1, XYZPoint o2) {
			if (o1.x < o2.x)
				return -1;
			if (o1.x > o2.x)
				return 1;
			return 0;
		}
	};

	private static final Comparator<XYZPoint> Y_COMPARATOR = new Comparator<XYZPoint>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(XYZPoint o1, XYZPoint o2) {
			if (o1.y < o2.y)
				return -1;
			if (o1.y > o2.y)
				return 1;
			return 0;
		}
	};



	protected static final int X_AXIS = 0;
	protected static final int Y_AXIS = 1;





	/**
	 * Does the tree contain the value.
	 * 
	 * @param value
	 *            T to locate in the tree.
	 * @return True if tree contains value.
	 */
	public boolean contains(T value) {
		if (value == null)
			return false;

		KdNode node = getNode(this, value);
		return (node != null);
	}

	/**
	 * Locate T in the tree.
	 * 
	 * @param tree
	 *            to search.
	 * @param value
	 *            to search for.
	 * @return KdNode or NULL if not found
	 */
	private static final <T extends KdTree.XYZPoint> KdNode getNode(KdTree<T> tree, T value) {
		if (tree == null || tree.root == null || value == null)
			return null;

		KdNode node = tree.root;
		while (true) {
			if (node.id.equals(value)) {
				return node;
			} else if (KdNode.compareTo(node.depth, node.k, value, node.id) <= 0) {
				// Lesser
				if (node.getLesser()== null) {
					return null;
				} else {
					node = node.getLesser();
				}
			} else {
				// Greater
				if (node.getGreater()== null) {
					return null;
				} else {
					node = node.getGreater();
				}
			}
		}
	}


	/**
	 * Get the (sub) tree rooted at root.
	 * 
	 * @param root
	 *            of tree to get nodes for.
	 * @return points in (sub) tree, not including root.
	 */
	private static final List<XYZPoint> getTree(KdNode root) {
		List<XYZPoint> list = new ArrayList<XYZPoint>();
		if (root == null)
			return list;

		if (root.getLesser() != null) {
			list.add(root.getLesser().id);
			list.addAll(getTree(root.getLesser()));
		}
		if (root.getGreater() != null) {
			list.add(root.getGreater().id);
			list.addAll(getTree(root.getGreater()));
		}

		return list;
	}

	/**
	 * K Nearest Neighbor search
	 * 
	 * @param K
	 *            Number of neighbors to retrieve. Can return more than K, if
	 *            last nodes are equal distances.
	 * @param value
	 *            to find neighbors of.
	 * @return collection of T neighbors.
	 */
	static Set<KdNode> globalExamined;
	@SuppressWarnings("unchecked")
	synchronized public  Collection<T> nearestNeighbourSearch(int K, T value) {
		long startTime = System.currentTimeMillis();
		if (value == null)
			return null;
		Log.e("debug", "IN FIND NEARESTNEIGHBOR");

		// Map used for results
		TreeSet<KdNode> results = new TreeSet<KdNode>(new EuclideanComparator(value));
		// Find the closest leaf node
		KdNode prev = null;
		//KdNode node = root;
		KdNode node = new KdNode(0);
		/*Log.e("debug", "node.x " + node.id.x + " node.y: " + node.id.y);
		Log.e("debug", "lesser node.x " + node.getLesser().id.x + " lesser node.y: " + node.getLesser().id.y);
		for (int i = 0; i < 10; i++) {
			System.out.println("nr " + i + ": " + node.findItem(i));

		}
		System.out.println("nr " + 381115  + ": " + node.findItem(381115 ));
		 */
		System.out.println("value: " + value);
		System.out.println("node.id: " + node.id);
		int nodeDepth = 0;
		while (node != null) {
			//System.out.println("depth " + node.depth);

			if (KdNode.compareTo(node.depth, node.k, value, node.id) <= 0) {
				// Lesser
				prev = node;
				node = node.getLesser();
			} else {
				// Greater
				prev = node;
				node = node.getGreater();
			}
			//if(node != null)
			//	System.out.println("itemnr: " + node.itemNr);
			//System.out.println("node != null");
		}
		KdNode leaf = prev;
		//Log.e("Debug", "leaf: " + leaf.id.x + ", " + leaf.id.y);
		if (leaf != null) {
			// Used to not re-examine nodes
			Set<KdNode> examined = new HashSet<KdNode>();
			globalExamined = examined;
			// Go up the tree, looking for better solutions
			node = leaf;
			int backsteps = 0;
			while (node != null && backsteps++ < 200) {
				// Search node
				//Log.e("Debug", "backsteps: " +backsteps);
				searchNode(value, node, K, results, examined);

				node = node.getParent();

			}
		}


		// Load up the collection of the results
		Collection<T> collection = new ArrayList<T>(K);
		for (KdNode kdNode : results) {
			kdNode.id.timeElapsed  = System.currentTimeMillis() - startTime;
			collection.add((T) kdNode.id);
		}
		return collection;
	}

	KdNode [] getNodesInArrray() {
		KdNode [] treeNodes;
		System.out.println("Maxdepth is: " + maxDepth);
		System.out.println(Integer.MAX_VALUE);
		System.out.println((int) (Math.pow(2, maxDepth+1) -1));
		treeNodes = new KdNode[(int) (Math.pow(2, maxDepth+1) -1)];
		traverse(root, 0, treeNodes);
		return treeNodes;
	}

	void traverse (KdNode node, int nr, KdNode [] treeNodes) {
		treeNodes[nr] = node;
		if(node.getLesser() != null)
			traverse(node.getLesser(), 2*nr +1, treeNodes);
		if(node.getGreater() != null)
			traverse(node.getGreater(), 2*nr + 2, treeNodes);
	}
	static int searchnodecount = 0;
	private static final <T extends KdTree.XYZPoint> void searchNode(T value, KdNode node, int K,
			TreeSet<KdNode> results, Set<KdNode> examined) {

		examined.add(node);
		//System.out.println("searchnodecount: " + searchnodecount++);
		// Search node
		KdNode lastNode = null;
		Double lastDistance = Double.MAX_VALUE;
		if (results.size() > 0) {
			lastNode = results.last();
			lastDistance = lastNode.id.euclideanDistance(value);
		}
		Double nodeDistance = node.id.euclideanDistance(value);
		if (nodeDistance.compareTo(lastDistance) < 0) {
			if (results.size() == K && lastNode != null)
				results.remove(lastNode);
			results.add(node);
		} else if (nodeDistance.equals(lastDistance)) {
			results.add(node);
		} else if (results.size() < K) {
			results.add(node);
		}


		lastNode = results.last();
		lastDistance = lastNode.id.euclideanDistance(value);

		int axis = node.depth % node.k;
		KdNode lesser = node.getLesser();
		KdNode greater = node.getGreater();


		// Search children branches, if axis aligned distance is less than
		// current distance
		if (lesser != null && !examined.contains(lesser)) {
			examined.add(lesser);
			double nodePoint = Double.MIN_VALUE;
			double valuePlusDistance = Double.MIN_VALUE;
			if (axis == X_AXIS) {
				nodePoint = node.id.x;
				valuePlusDistance = value.x - lastDistance;
			} else if (axis == Y_AXIS) {
				nodePoint = node.id.y;
				valuePlusDistance = value.y - lastDistance;
			} else {
				nodePoint = node.id.z;
				valuePlusDistance = value.z - lastDistance;
			}
			boolean lineIntersectsCube = ((valuePlusDistance <= nodePoint) ? true : false);

			// Continue down lesser branch
			if (lineIntersectsCube)
				searchNode(value, lesser, K, results, examined);
		}
		if (greater != null && !examined.contains(greater)) {
			examined.add(greater);
			double nodePoint = Double.MIN_VALUE;
			double valuePlusDistance = Double.MIN_VALUE;
			if (axis == X_AXIS) {
				nodePoint = node.id.x;
				valuePlusDistance = value.x + lastDistance;
			} else if (axis == Y_AXIS) {
				nodePoint = node.id.y;
				valuePlusDistance = value.y + lastDistance;
			} else {
				nodePoint = node.id.z;
				valuePlusDistance = value.z + lastDistance;
			}
			boolean lineIntersectsCube = ((valuePlusDistance >= nodePoint) ? true : false);

			// Continue down greater branch
			if (lineIntersectsCube)
				searchNode(value, greater, K, results, examined);
			
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return null;
		//return TreePrinter.getString(this);
	}



	protected static class EuclideanComparator implements Comparator<KdNode> {

		private XYZPoint point = null;
		public EuclideanComparator(XYZPoint point) {
			this.point = point;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(KdNode o1, KdNode o2) {
			Double d1 = point.euclideanDistance(o1.id);
			Double d2 = point.euclideanDistance(o2.id);
			if (d1.compareTo(d2) < 0)
				return -1;
			else if (d2.compareTo(d1) < 0)
				return 1;
			return o1.id.compareTo(o2.id);
		}
	};

	static HashMap<Integer, XYZPoint> itemCache = new HashMap<Integer, XYZPoint>();
	static int findItemCount = 0;
	public static class KdNode implements Comparable<KdNode> {
		private int k = 2;
		private int depth = 0;
		XYZPoint id = null;
		int itemNr = -1;	
		final int NODE_SIZE = 2*4+4; //X(4 bytes) Y(4 bytes) data(4 bytes)
		final static private String filename = "data.txt";
		private boolean isExtracted [] = new boolean [7];

		public KdNode(XYZPoint id, int itemNr, int depth) {
			this.id = id;
			this.itemNr = itemNr;
			this.depth = depth;

		}

		/*
        public KdNode(int k, int depth, XYZPoint id) {
            this(id);
            this.k = k;
            this.depth = depth;
        }
		 */
		KdNode(int itemNr) {
			this.itemNr = itemNr;
			id = findItem(itemNr);
			System.out.println("in KdNode(), id: " + id);
		}
		XYZPoint findItem(int itemNr){
			//System.out.println(findItemCount++);
			//System.out.println("itemnr: " + itemNr);
			//System.out.println("cache size: " + itemCache.size());
			//System.out.println("itemNr " + itemNr);
			if(itemNr < 0)
				return null;
			
			//System.out.println("in find item");
			if(itemCache.containsKey(itemNr)){
				//System.out.println("cache hit");
				return itemCache.get(itemNr);
			}
			try {
				long startTime = System.currentTimeMillis();
				//Scanner sc = new Scanner(new File(filename));
				//File sdCard = Environment.getExternalFilesDir("coordsdata");
				File file = new File(externalFilesDir, "datab.dat");

				if(!file.exists()){
					Log.e("debug", "file doesnt exist");
					System.out.println("file does not exist");
					return null;
				}
				int ancester = itemNr;
				while(ancester > 7) 
					ancester >>= 1;
				if(!isExtracted[ancester])
					extract(ancester);
				//System.out.println("file size: "+ file.length());
				DataInputStream dis = new DataInputStream(new FileInputStream(file));
				
				double a = -1, b = -1;
				int skipNrOfBytes = (itemNr)*(NODE_SIZE);
				//System.out.println("file length: " + file.length());
				//System.out.println("skipNrOfBytes: " + skipNrOfBytes);

				if(skipNrOfBytes >= file.length()){
					dis.close();
					return null;
				}

				int bytesSkiped = 0;
				while(bytesSkiped < skipNrOfBytes){
					bytesSkiped += dis.skip(skipNrOfBytes - bytesSkiped);
				}
				
				//System.out.println("bytesSkiped: " + bytesSkiped + " of " + skipNrOfBytes);


				a = dis.readInt();
				a/=1000000;
				b = dis.readInt();
				b /= 1000000;
				int speed = dis.readInt();

				dis.close();
				//System.out.println("itemnr: " + itemNr);
				//System.out.println("a: "+a);
				//System.out.println("b: " +b);
				//System.out.println("time: " + (System.currentTimeMillis() - startTime));
				//sc.close();
				/*if(i <= itemNr){
					itemCache.put(itemNr, null);
					return null;
				}
				 */
				if(a > 90 || a < 1){
					itemCache.put(itemNr, null);
					return null;
				}
				XYZPoint xyzpoint = new XYZPoint(a, b, speed);
				itemCache.put(itemNr, xyzpoint);
				return xyzpoint;

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IOException");
				itemCache.put(itemNr, null);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		private void extract(int ancester) throws IOException {

			File file = new File(externalFilesDir, "datac"+ancester+".dat");
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));

			InputStream is = new FileInputStream(file);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			try {
				ZipEntry ze;
				while ((ze = zis.getNextEntry()) != null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] zipBuffer = new byte[1024];
					int count;
					while ((count = zis.read(zipBuffer)) != -1) {
						dos.write(baos.toByteArray(),0,count);
					}
					
				}
			} finally {
				zis.close();
				dos.close();
			}

			
		}

		int parentItemNr(int itemNr){
			if(itemNr == 0)
				return -1;
			return (itemNr - 1)/2;
		}
		int lesserItemNr(int itemNr){
			return 2*itemNr + 1;
		}
		int greaterItemNr(int itemNr){
			return 2*itemNr + 2;
		}


		KdNode getParent(){

			XYZPoint xyPoint = findItem(parentItemNr(itemNr));
			if(xyPoint != null)
				return new KdNode(xyPoint, parentItemNr(itemNr), depth == 0 ? 0 : depth - 1);

			return null; 
		}
		KdNode getLesser(){

			XYZPoint xypoint = findItem(lesserItemNr(itemNr));
			if(xypoint != null)
				return new KdNode(xypoint, lesserItemNr(itemNr), depth + 1);
			return null; 
		}
		KdNode getGreater(){

			XYZPoint xypoint = findItem(greaterItemNr(itemNr));
			if(xypoint != null)
				return new KdNode(xypoint, greaterItemNr(itemNr), depth + 1);
			return null; 
		}

		
		/*      void setParent(KdNode node){
        	this.parent = node;
        }
        void setLesser(KdNode node){
        	this.lesser = node;
        }
        void setGreater(KdNode node){
        	this.greater = node;
        }
		 */
		public static int compareTo(int depth, int k, XYZPoint o1, XYZPoint o2) {

			int axis = depth % k;
			if (axis == X_AXIS)
				return X_COMPARATOR.compare(o1, o2);
			return Y_COMPARATOR.compare(o1, o2);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof KdNode))
				return false;

			KdNode kdNode = (KdNode) obj;
			if (this.compareTo(kdNode) == 0)
				return true;
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(KdNode o) {
			return compareTo(depth, k, this.id, o.id);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("k=").append(k);
			builder.append(" depth=").append(depth);
			builder.append(" id=").append(id.toString());
			return builder.toString();
		}
	}

	public static class XYZPoint implements Comparable<XYZPoint> {
		int randnr = new Random().nextInt();
		double x = Double.NEGATIVE_INFINITY;
		double y = Double.NEGATIVE_INFINITY;
		double z = Double.NEGATIVE_INFINITY;
		int speed = 0;
		public long timeElapsed = -1;

		public XYZPoint(double x, double y, int speed) {
			this.x = x;
			this.y = y;
			this.speed = speed;
			this.z = 0;
		}

		public XYZPoint(double x, int y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		int getSpeed(){
			return (speed>>24 & 0xf)*10;
		}
		boolean isATKclose(){
			return ((speed >> 26) & 1) > 0;
		}
		/**
		 * Computes the Euclidean distance from this point to the other.
		 * 
		 * @param o1
		 *            other point.
		 * @return euclidean distance.
		 */
		public double euclideanDistance(XYZPoint o1) {
			return euclideanDistance(o1, this);
		}

		/**
		 * Computes the Euclidean distance from one point to the other.
		 * 
		 * @param o1
		 *            first point.
		 * @param o2
		 *            second point.
		 * @return euclidean distance.
		 */
		private static final double euclideanDistance(XYZPoint o1, XYZPoint o2) {
			return Math.sqrt(Math.pow((o1.x - o2.x), 2) + Math.pow((o1.y - o2.y), 2) + Math.pow((o1.z - o2.z), 2));

		};

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof XYZPoint))
				return false;

			XYZPoint xyzPoint = (XYZPoint) obj;
			return compareTo(xyzPoint) == 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(XYZPoint o) {
			int xComp = X_COMPARATOR.compare(this, o);
			if (xComp != 0)
				return xComp;
			int yComp = Y_COMPARATOR.compare(this, o);
			return yComp;

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append(randnr+": ");
			builder.append(x).append(", ");
			builder.append(y).append(", ");
			builder.append(z);
			builder.append(")");
			return builder.toString();
		}
	}

}