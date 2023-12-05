// Alain Ambrose
// al467325
// COP 3503 Spring 2023
// Sublime
// MacOS

import java.io.*;
import java.util.*;


// Node class set up to be able to handle any type of data
// Instance variables store the height and data of a node 
class Node<AnyType>
{
	private int height;
	private AnyType data;
	private ArrayList<Node<AnyType>> references = new ArrayList<>();

	// Initalizes a node with a series of null references 
	// and sets its data to null
	Node(int height)
	{
		this.height = height;
		this.data = null;
		for (int i = 0;i < height;i++)
		{
			references.add(null);
		}
	}

	// Initalizes a node with a series of null references 
	// and sets its data to the passed parameter
	Node(AnyType data, int height)
	{
		this.data = data;
		this.height = height;
		for (int i = 0;i < height;i++)
		{
			references.add(null);
		}
	}

	// Returns the data stored in the node
	public AnyType value()
	{
		return this.data;
	}

	// Returns the height of the node
	public int height()
	{
		return height;
	}

	// Returns a reference to the next node at the 
	// height of the passed parameter
	public Node<AnyType> next(int level)
	{
		if (level < 0 || level > height - 1)
		{
			return null;
		}
		else
		{
			return references.get(level);
		}
	}

	// Sets this nodes next reference at level: 'level'
	public void setNext(int level, Node<AnyType> node)
	{
		references.set(level, node);
	}

	// Grows this nodes list of references by 1
	public void grow()
	{
		height++;
		references.add(null);	
	}

	// 50% chance of growing this nodes list of references by 1
	public void maybeGrow()
	{
		if(Math.round(Math.random()) == 1)
		{
			height++;
			references.add(null);	
		}
	}

	// deletes references until this node is at the desired height
	public void trim()
	{
		references.remove(this.height - 1);
		height--;
	}

	@Override
	public String toString()
	{
		return "(" + data + ", " + height + ")";
	}
}

// SkipList class set up to be able to handle any type of data
public class SkipList<AnyType extends Comparable<AnyType>>
{
	// This skip list is implemented using an ArrayList
	// The head is instantiated with height 0
	// MaxHeightNodes stores all maximally tall nodes
	// TallNodes stores all nodes with height > 1
	ArrayList<Node<AnyType>> SList = new ArrayList<>();
	Node<AnyType> head = new Node<>(0);
	ArrayList<Node<AnyType>> maxHeightNodes = new ArrayList<>();
	ArrayList<Node<AnyType>> tallNodes = new ArrayList<>();
	int nodeCount = 0;

	// Non-specified height skipList constuctor
	public SkipList()
	{
		SList.add(head);
		maxHeightNodes.add(head);
		tallNodes.add(head);
		head.grow();
	}

	// Specified height skipList constuctor
	public SkipList(int height)
	{
		maxHeightNodes.add(head);
		tallNodes.add(head);
		if (height < 1)
		{
			head.grow();
		}
		else
		{
			for (int i = 0;i < height;i++)
			{
				head.grow();
			}
		}
	}

	// Returns size in nodes of skipList
	public int size()
	{
		return nodeCount;
	}

	// Returns the height of the skipList
	public int height()
	{
		return head.height();
	}

	// Returns the head of the skipList
	public Node<AnyType> head()
	{
		return head;
	}

	// Inserts a new node into the skipList, this method
	// generates a random height for that new node
	public void insert(AnyType data)
	{
		nodeCount++;
		// if the expecdted logarithmic height is greater than that of the
		// current height then we grow the skip list to accomodate for the 
		// new node
		if (getMaxHeight(nodeCount) > height())
		{
			growSkipList();
		}

		// sets the current height for traversal to the height of the head
		// which is guranteed to be the greatest height in the skiplist
		int currentHeight = head.height();

		// Generates a random height and instantiates the new node with
		// that random height and the given data
		int randHeight = generateRandomHeight(currentHeight);
		Node<AnyType> newNode = new Node<>(data, randHeight);

		// If our new node is maximally tall then we add it to the max
		// height nodes arrayList
		if (newNode.height() == head.height())
		{
			maxHeightNodes.add(newNode);
		}

		if (newNode.height() > 1)
		{
			tallNodes.add(newNode);
		}

		// Creates an arraylist of nodes to adjust after our new node has 
		// been inserted and sets the current node to the head
		ArrayList<Node<AnyType>> nodesToAdjust = new ArrayList<>();

		Node<AnyType> currentNode = head;

		// fills the nodesToAdjust arrayList with the head node so that
		// resseting the values in the array list is possible below
		for (int i = 0; i < newNode.height();i++)
		{
			nodesToAdjust.add(currentNode);
		}
		
		int adjustmentFlag = 0;
		while (currentHeight > 0)
		{
			Node<AnyType> next = currentNode.next(currentHeight-1);
			// if the currentNodes next pointer at our current height is
			// greater than our new data, we have gone too far and need to
			// drop down a level. If the current height is greater than 1 
			// we lower it by one (drop down a level) and if it is also 
			// less than or equal to the height of our new node we add the
			// current node to the nodesToAdjust arryList so that its 
			// references can be adjusted after the loop 
			if (next == null || next.value().compareTo(data) >= 0)
			{
				if (currentHeight > 1)
				{
					if (currentHeight <= randHeight)
					{
						nodesToAdjust.set(currentHeight-1, currentNode);
						adjustmentFlag = 1;
					}
					currentHeight--;
				}
				else
				{
					newNode.setNext(0, currentNode.next(0));
					currentNode.setNext(0, newNode);
					break;
				}
			}
			// moves the current node forward if its value is less than our target
			else
			{
				currentNode = next;
			}
		}
		// Replaces the references for the nodes in the nodes to adjust class
		// with references to our new node and has the new node inherit their
		// references
		if (adjustmentFlag == 1)
		{
			for (int k = 1;k < nodesToAdjust.size();k++)
			{
				newNode.setNext(k, nodesToAdjust.get(k).next(k));
				nodesToAdjust.get(k).setNext(k, newNode);
			}
		}
	}	

	// Inserts a new node into the skipList at the specified
	// height passed into the method
	public void insert(AnyType data, int height)
	{
		nodeCount++;
		// if the expecdted logarithmic height is greater than that of the
		// current height then we grow the skip list to accomodate for the 
		// new node
		if (getMaxHeight(nodeCount) > height())
		{
			growSkipList();
		}

		// sets the current height for traversal to the height of the head
		// which is guranteed to be the greatest height in the skiplist
		int currentHeight = head.height();

		// Applies given height and instantiates the new node with
		// that random height and the given data
		Node<AnyType> newNode = new Node<>(data, height);

		// If our new node is maximally tall then we add it to the max
		// height nodes arrayList
		if (newNode.height() == head.height())
		{
			maxHeightNodes.add(newNode);
		}

		if (newNode.height() > 1)
		{
			tallNodes.add(newNode);
		}

		// Creates an arraylist of nodes to adjust after our new node has 
		// been inserted and sets the current node to the head
		ArrayList<Node<AnyType>> nodesToAdjust = new ArrayList<>();

		Node<AnyType> currentNode = head;

		// fills the nodesToAdjust arrayList with the head node so that
		// resseting the values in the array list is possible below
		for (int i = 0; i < newNode.height();i++)
		{
			nodesToAdjust.add(currentNode);
		}
		
		// Loops thorugh all of the necessary nodes in the SkipList
		int adjustmentFlag = 0;
		while (currentHeight > 0)
		{
			Node<AnyType> next = currentNode.next(currentHeight-1);
			// if the currentNodes next pointer at our current height is
			// greater than our new data, we have gone too far and need to
			// drop down a level. If the current height is greater than 1 
			// we lower it by one (drop down a level) and if it is also 
			// less than or equal to the height of our new node we add the
			// current node to the nodesToAdjust arryList so that its 
			// references can be adjusted after the loop 
			if (next == null || next.value().compareTo(data) >= 0)
			{
				if (currentHeight > 1)
				{
					if (currentHeight <= height)
					{
						nodesToAdjust.set(currentHeight-1, currentNode);
						adjustmentFlag = 1;
					}
					currentHeight--;
				}
				else
				{
					newNode.setNext(0, currentNode.next(0));
					currentNode.setNext(0, newNode);
					break;
				}
			}
			// moves the current node forward if its value is less than our target
			else
			{
				currentNode = next;
			}
		}
		// Replaces the references for the nodes in the nodes to adjust class
		// with references to our new node and has the new node inherit their
		// references
		if (adjustmentFlag == 1)
		{
			for (int k = 1;k < nodesToAdjust.size();k++)
			{
				newNode.setNext(k, nodesToAdjust.get(k).next(k));
				nodesToAdjust.get(k).setNext(k, newNode);
			}
		}
	}	

	// Returns true if the skipList contains the passed in value
	// and false otherwise
	public boolean contains(AnyType data)
	{
		// sets the current height for traversal to the height of the head
		// which is guranteed to be the greatest height in the skiplist
		int currentHeight = head.height();

		Node<AnyType> currentNode = head;
		
		// Loops thorugh all of the necessary nodes in the SkipList
		while (currentHeight > 0)
		{
			Node<AnyType> next = currentNode.next(currentHeight-1);
			// if the currentNodes next pointer at our current height is
			// greater than our new data, we have gone too far and need to
			// drop down a level. If the current height is greater than 1 
			// we lower it by one (drop down a level)
			if (next == null || next.value().compareTo(data) > 0)
			{
				if (currentHeight > 1)
				{
					currentHeight--;
				}
				else
				{
					return false;
				}
			}
			else if (next.value().compareTo(data) == 0)
			{
				return true;
			}
			else
			{
				currentNode = next;
			}
		}
		return false;
	}	

	// Returns a reference to a node with the given data.
	// It always finds the first sighting of the passed in data,
	// but doesn't necessarily traverse to the lowest level to 
	// ensure that it is the earliest occurence of that data
	public Node<AnyType> get(AnyType data)
	{
		// sets the current height for traversal to the height of the head
		// which is guranteed to be the greatest height in the skiplist
		int currentHeight = head.height();

		Node<AnyType> currentNode = head;
		
		// Loops thorugh all of the necessary nodes in the SkipList
		while (currentHeight > 0)
		{
			Node<AnyType> next = currentNode.next(currentHeight-1);

			// if the currentNodes next pointer at our current height is
			// greater than our new data, we have gone too far and need to
			// drop down a level. If the current height is greater than 1 
			// we lower it by one (drop down a level)
			if (next == null || next.value().compareTo(data) > 0)
			{
				if (currentHeight > 1)
				{
					currentHeight--;
				}
				else
				{
					return null;
				}
			}
			else if (next != null && next.value().compareTo(data) == 0)
			{
				return next;
			}
			else
			{
				currentNode = next;
			}
		}
		return null;
	}

	// Returns a reference to a node with the given data.
	// Makes sure to traverse to the lowest level of the skipList
	// and returns a reference to the sequentially earliest 
	// occurence of the desired data.
	public Node<AnyType> getFirstOccurrence(AnyType data)
	{
		// sets the current height for traversal to the height of the head
		// which is guranteed to be the greatest height in the skiplist
		int currentHeight = head.height();

		Node<AnyType> currentNode = head;
		
		// Loops thorugh all of the necessary nodes in the SkipList
		while (currentHeight > 0)
		{
			Node<AnyType> next = currentNode.next(currentHeight-1);
			// if the currentNodes next pointer at our current height is
			// greater than our new data, we have gone too far and need to
			// drop down a level. If the current height is greater than 1 
			// we lower it by one (drop down a level) and if it is also 
			// less than or equal to the height of our new node we add the
			// current node to the nodesToAdjust arryList so that its 
			// references can be adjusted after the loop 
			//ystem.out.println("Current Height = " + currentHeight);
			//System.out.println("currentNode.next @ "+ currentHeight +" = " + next);
			if (next == null || next.value().compareTo(data) >= 0)
			{
				if (currentHeight > 1)
				{
					currentHeight--;
				}
				else if (next != null && next.value().compareTo(data) == 0)
				{
					return next;
				}
				else
				{
					return null;
				}
			}
			else
			{
				currentNode = next;
			}
		}
		return null;
	}

	// Removes the first sequential occurence of a node in the skipList
	public void delete(AnyType data)
	{
		Node<AnyType> delNode = getFirstOccurrence(data);
		if (delNode != null)
		{
			nodeCount--;
			// if the expecdted logarithmic height is less than that of the
			// current height then we trim the skip list
			if (getMaxHeight(nodeCount) < height() && getMaxHeight(nodeCount) > 0)
			{
				trimSkipList(getMaxHeight(nodeCount));
			}

			// sets the current height for traversal to the height of the head
			// which is guranteed to be the greatest height in the skiplist
			int currentHeight = head.height();

			// Creates an arraylist of nodes to adjust after our new node has 
			// been inserted and sets the current node to the head
			ArrayList<Node<AnyType>> nodesToAdjust = new ArrayList<>();

			Node<AnyType> currentNode = head;

			// fills the nodesToAdjust arrayList with the head node so that
			// resseting the values in the array list is possible below
			for (int i = 0; i < delNode.height();i++)
			{
				nodesToAdjust.add(currentNode);
			}
			
			// Loops thorugh all of the necessary nodes in the SkipList
			int adjustmentFlag = 0;
			while (currentHeight > 0)
			{
				Node<AnyType> next = currentNode.next(currentHeight-1);
				// if the currentNodes next pointer at our current height is
				// greater than our new data, we have gone too far and need to
				// drop down a level. If the current height is greater than 1 
				// we lower it by one (drop down a level) and if it is also 
				// less than or equal to the height of our new node we add the
				// current node to the nodesToAdjust arryList so that its 
				// references can be adjusted after the loop 
				if (next == null || next.value().compareTo(data) >= 0)
				{
					if (currentHeight > 1)
					{
						if (currentHeight <= delNode.height())
						{
							nodesToAdjust.set(currentHeight-1, currentNode);
							adjustmentFlag = 1;
						}
						currentHeight--;
					}
					else if (next.value().compareTo(data) == 0)
					{
						currentNode.setNext(0, delNode.next(0));
						break;
					}
				}
				// moves the current node forward if its value is less than our target
				else
				{
					currentNode = next;
					//System.out.println("Made it to " + currentNode + "!");
				}
			}
			// Replaces the references for the nodes in the nodes to adjust class
			// with references to our new node and has the new node inherit their
			// references
			if (adjustmentFlag == 1)
			{
				for (int k = 1;k < nodesToAdjust.size();k++)
				{
					nodesToAdjust.get(k).setNext(k, delNode.next(k));
				}
			}
			SList.remove(delNode);
			maxHeightNodes.remove(delNode);
		}
	}

	// Returns the logarithmic maximum height of a skipList with n nodes
	private static int getMaxHeight(int n)
	{
		int logSize = (int) Math.ceil((Math.log(n) / Math.log(2)));
		return logSize;
	}

	// Generates a random height from 1 to maxHeight
	private static int generateRandomHeight(int maxHeight)
	{
		int height = 1;
		for (int i = 1;i < maxHeight;i++)
		{
			if (Math.round(Math.random()) == 1)
				height++;
			else
				break;
		}
		return height;
	}

	// Grows the skipList by one level if our logarithmic height becomes larger
	// than our actual height
	private void growSkipList()
	{
		ArrayList<Node<AnyType>> newMaxHeightNodes = new ArrayList<>();
		ArrayList<Node<AnyType>> newTallNodes = new ArrayList<>();
		head.grow();
		newMaxHeightNodes.add(head);
		Node<AnyType> currentNode = new Node<>(0);
		for (int i = 1;i < maxHeightNodes.size();i++)
		{
			currentNode = maxHeightNodes.get(i);
			currentNode.maybeGrow();
			if (currentNode.height() > 1)
			{
				newTallNodes.add(currentNode);
			}
			if (currentNode.height() == head.height())
			{
				newMaxHeightNodes.add(currentNode);
			}
		}
		maxHeightNodes.clear();
		maxHeightNodes = newMaxHeightNodes;

		for (int i = 0;i < newTallNodes.size();i++)
		{
			if (!tallNodes.contains(newTallNodes.get(i)))
			{
				tallNodes.add(newTallNodes.get(i));
			}
		}

		for (int i = 0;i < maxHeightNodes.size() - 1;i++)
		{
			currentNode = maxHeightNodes.get(i);
			currentNode.setNext(height()-1, maxHeightNodes.get(i + 1));
		}
	}

	// Trims the skip list by however many levels it takes to bring
	// it back to logarithmic harmony
	private void trimSkipList(int logSize)
	{
		for (int i = 0;i < tallNodes.size();i++)
		{
			int nodeHeight = tallNodes.get(i).height();
			for (int j = 0; j < nodeHeight - logSize;j++)
			{
				tallNodes.get(i).trim();
			}
		}
	}

	// Difficulty rating of this assignment on a scale of 1-5
	// (1 = ridiculously easy)
	// (5 = insanely difficult)
	public static double difficultyRating()
	{
		return 5.0;
	}

	// Number of hours spent on this assignment
	public static double hoursSpent()
	{
		return 25.0;
	}
}
