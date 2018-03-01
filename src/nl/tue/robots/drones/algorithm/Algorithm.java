package nl.tue.robots.drones.algorithm;

public class Algorithm {

	// Expanded nodes contain:
	// a node,
	// the distance from it to the root,
	// the expected distance from it to the destination
	// a parentExpandedNode
	ExpandedNode testNode = new ExpandedNode(Node node, int distanceTravelled, int heuristicDistance, ExpandedNode parent);
	ExpandedNode emptyNode = ExpandedNode(Node null, int null, int null, ExpandedNode null);

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
		ExpandedNode rootNode = new ExpandedNode(startNode, 0, Model.getHeuristic(startNode, destinatinoNode), emptyNode)
		Set<ExpandedNode> seenNodes = new HashSet<ExpandedNode>();
		seenNodes.add(rootNode);

		// Setup the frontier list
		ArrayList<ExpandedNode> frontier = new ArrayList<ExpandedNode>();
		frontier.add(rootNode);

		ExpandedNode foundDestination = pathSearch(destinationNode, seenNodes, frontier);
		ArrayList<Transition> transitionList = new ArrayList<Transition>();

		// Check whether foundDestination is the actual destination
		if (foundDestination.node == destinationNode){
			ArrayList<Transition> transitionList = getPath(destination, transitionList);	
		} else {
			// something went horribly wrong. Fix it!
		}

		return transitionList;
	}

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
	public ExpandedNode pathSearch(Node destinationNode, Set<ExpandedNode> seenNodes, ArrayList<ExpandedNode> frontier) {
		if (frontier.size() > 0){
			// Then the frontier is not empty
			ExpandedNode nodeToExpand = frontier.get(0);
			ArrayList<Node> newNodes = Model.getOptions(nodeToExpand);
			ArrayList<ExpandedNode> newExpandedNodes = new ArrayList<ExpandedNode>();

			// Check whether one of the nodes is the destination node
			for (Node node : newNodes) {
				newExpandedNodes.add(createExpandedNode(node, destinationNode, nodeToExpand));
				if (node == destinationNode){
					// TODO: make sure it all works!
				}
			}

			// Now that the new elements have been found, the node can be removed
			frontier.remove(0);

			// Now that the first node is removed, the new ones can be added.
			for (ExpandedNode node : newExpandedNodes){
				if(seenNode.add(node)){
					frontier = sortedAdd(node, frontier);
				}
			}

			// We have updated the frontier with new nodes, so it can start all over again.
			pathSearch(destinationNode, seenNodes, frontier);		

		} else {
			// Then the frontier is empty, which means there is no path

		}
	}

	public ArrayList<Transition> getPath(ExpandedNode lastNode, ArrayList<Transition> transitions){
		if(lastNode.parent.node == null){
			// Then we have found the root, so we can return the transition list!
			return transitions;
		} else {
			Node currentNode = lastNode.node;
			Node parentNode = lastNode.parentNode.node;
			Transition currentTransition = Model.getTransition(parentNode, currentNode);
			transitions.add(0, currentTransition);
			return getPath(lastNode.parentNode, transitons);
		}
	}

	public ExpandedNode createExpandedNode(Node node, Node destination, ExpandedNode parentNodeExpanded){
		Node parentNode = parentNodeExpanded.node;
		int transitionDistance = parentNodeExpanded.distance + Model.getTransitionDistance(parentNode, node);
		int heuristicDistance = Model.getHeuristic(node, destination);
		ExpandedNode newNode = new ExpandedNode(node, transitionDistance, heuristicDistance, parentNodeExpanded);
		return newNode;
	}



	public ArrayList<ExpandedNode> sortedAdd(ExpandedNode newNode, ArrayList<ExpandedNode> frontier){
		int newDistance = newNode.distanceTravelled + newNode.heuristicDistance;
		ExpandedNode tempNodeExpanded;
		int arraySize = frontier.size;

		for (int i = 0; i < arraySize; i++){
			tempNode = frontier.get(i);
			int tempDistance = tempNode.distanceTravelled + tempNode.heuristicDistance;
			if ((newDistance < tempDistance) || (newDistance == tempDistance && newNode.heuristicDistance < tempNode.heuristicDistance)){
				frontier.add(i, newNode);
				return frontier;
			}
		}

		// If it has not been place yet, it has the highest distance yet, so can be added at the end
		frontier.add(newNode);
		return frontier;
	}
}
