package nl.tue.robots.drones.algorithm;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.model.Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Algorithm {

	// Expanded nodes contain:
	// a node,
	// the distance from it to the root,
	// the expected distance from it to the destination
	// a parentExpandedNode
	private ExpandedNode emptyNode = new ExpandedNode(null, 0, 0, null);

   /**
	* Function findPath
	*
	* @param startNode is the node at which the drone currently is.
	* @param destinationNode is the node the drone has to go to
	* @return The function returns the set of transitions the node has to take
	* 		  to get to his destination.
	*		  If there is not an available path, the function throws an exception.
	*
	*/
	public ArrayList<Transition> findPath(Node startNode, Node destinationNode) {

		// Setup the expanded node list
		ExpandedNode rootNode = new ExpandedNode(startNode, 0,
                Model.getHeuristic(startNode, destinationNode), emptyNode);
		Set<ExpandedNode> seenNodes = new HashSet<ExpandedNode>();
		seenNodes.add(rootNode);

		// Setup the frontier list
		ArrayList<ExpandedNode> frontier = new ArrayList<ExpandedNode>();
		frontier.add(rootNode);

		ExpandedNode foundDestination = pathSearch(destinationNode, seenNodes, frontier);
		ArrayList<Transition> transitionList = new ArrayList<Transition>();

		// Check whether foundDestination is the actual destination
		if (foundDestination.getNode() == destinationNode){
			getPath(foundDestination, transitionList);
		} else {
			// something went horribly wrong. Fix it!
            System.out.println("This should not happen");
		}

		return transitionList;
	}

   /**
	* Function pathSearch
	*
	* @param destinationNode is the node the drone has to go to
	* @param seenNodes is the set of nodes that have already been visited
	* @param frontier ìs the set of nodes that still have to be expanded
	* @return The function returns an ExpandedNode object with the destinationNode in it.
	*
	*/
	public ExpandedNode pathSearch(Node destinationNode, Set<ExpandedNode> seenNodes,
                                   ArrayList<ExpandedNode> frontier) {
		// First check the frontier isn't empty
		if (frontier.size() > 0){
			// The frontier is not empty: expand the best node
			ExpandedNode nodeToExpand = frontier.get(0);
			List<Node> newNodes = Model.getOptions(nodeToExpand);
			ArrayList<ExpandedNode> newExpandedNodes = new ArrayList<ExpandedNode>();

			// Check whether one of the nodes is the destination node
			for (Node node : newNodes) {
				newExpandedNodes.add(createExpandedNode(node, destinationNode, nodeToExpand));
				if (node == destinationNode){
					//
					return createExpandedNode(node, destinationNode, nodeToExpand);
				}
			}

			// Now that the new elements have been found, the node can be removed
			frontier.remove(0);

			// Now that the first node is removed, the new ones can be added.
			for (ExpandedNode node : newExpandedNodes){
				// TODO: If the given node only has one available transition and is not
				// the destination, it does not have to be added to the frontier!
				if(seenNodes.add(node)){
					sortedAdd(node, frontier);
				}
			}

			// We have updated the frontier with new nodes, so it can start all over again.
			return pathSearch(destinationNode, seenNodes, frontier);
		} else {
			// Then the frontier is empty, which means there is no path
			// Still have to come up with what will be returned in this case.
			return new ExpandedNode(null, 0, 0, null);
		}
	}

   /**
	* Function getPath
	* Generates a transitionList
	*
	* @param lastNode is the closest node to the start node of  which the path has not been created
	* @param transitions contains a transitionList from lastNode to the destinationNode
	* @return The function returns an ArrayList of Transitions which the drones has to take to reach
	*		  its goal.
	*/
	public void getPath(ExpandedNode lastNode, ArrayList<Transition> transitions){
        if (lastNode.getParent().getNode() != null) {
            // Then we have not yet found the root.
            Node currentNode = lastNode.getNode();
            Node parentNode = lastNode.getParent().getNode();
            Transition currentTransition = Model.getTransition(parentNode, currentNode);
            transitions.add(0, currentTransition);
            //recursive call
            getPath(lastNode.getParent(), transitions);
        }
        // If not then we have found the root, so we don't need to add to the transition list!
    }

   /**
	* Function createExpandedNode
	* Generates and returns a new ExpandedNode
	*
	* @param node the node for which an ExpandedNode has to be created
	* @param destination the destination node for the drone
	* @param parentNodeExpanded is the ExpandedNode of the parent of {@code node}
	* @return an ExpandedNode based around {@code node}
	*
	*/
	public ExpandedNode createExpandedNode(Node node, Node destination,
                                           ExpandedNode parentNodeExpanded){
		Node parentNode = parentNodeExpanded.getNode();
		int transitionDistance = parentNodeExpanded.getDistanceTravelled() +
                Model.getTransitionDistance(parentNode, node);
		int heuristicDistance = Model.getHeuristic(node, destination);
        return new ExpandedNode(node, transitionDistance, heuristicDistance, parentNodeExpanded);
	}

   /**
	* Function sortedAdd
	* Adds the nodes in newNode to the sorted frontier
	*
	* @param newNode a list with nodes that has to be integrated in {@code frontier}
	* @param frontier all nodes that still have to be expanded
	* @return frontier with all nodes in newNode integrated
	*
	*/
	public void sortedAdd(ExpandedNode newNode, ArrayList<ExpandedNode> frontier){
		int newDistance = newNode.getDistanceTravelled() + newNode.getHeuristicDistance();
		ExpandedNode expandedNode;
		int arraySize = frontier.size();

		//todo move to just adding and then "normal" sort, we control ExpandedNode from algo anyway
		for (int i = 0; i < arraySize; i++){
			expandedNode = frontier.get(i);
			int distance = expandedNode.getDistanceTravelled() + expandedNode.getHeuristicDistance();
			if ((newDistance < distance) || (newDistance == distance &&
                    newNode.getHeuristicDistance() < expandedNode.getHeuristicDistance())){
				frontier.add(i, newNode);
				return;
			}
		}

		// If it has not been place yet, it has the highest distance yet, so can be added at the end
		frontier.add(newNode);
	}
}