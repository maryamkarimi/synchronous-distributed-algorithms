import java.util.Vector;

/* Algorithm for counting the number of processors in a mesh network. */

public class CountProcessorsMesh extends Algorithm {

    private static final String SEPARATOR = "-";

    private enum Direction {
        R,
        L,
        T,
        B
    }
    
    /* Do not modify this method */
    public Object run() {
        int status = countProcessors(getID());
        return status;
    }

    private static class MyMessage {
        private int count;
        private Direction direction;

        public MyMessage(final int count, final Direction direction) {
            this.count = count;
            this.direction = direction;
        }

        public String encode() {
            return count + SEPARATOR + direction;
        }

        public void incrementCount() {
            count++;
        }
    }

    private MyMessage generateMessage(Message message) {
        if (message == null) return null;
        final String[] messageParts = message.data().split(SEPARATOR);
        return new MyMessage(Integer.parseInt(messageParts[0]), Direction.valueOf(messageParts[1]));
    }

    public int countProcessors(String id) {
        Vector<String> v = neighbours(); // Set of neighbours of this node.
        String topNeighbour = (String) v.elementAt(0); 
		String rightNeighbour = (String) v.elementAt(1);
		String bottomNeighbour = (String) v.elementAt(2);
		String leftNeighbour = (String) v.elementAt(3);

        final boolean topRightCorner = equal(topNeighbour, "0") && equal(rightNeighbour, "0");
        final boolean topLeftCorner = equal(topNeighbour, "0") && equal(leftNeighbour, "0");
        final boolean bottomLeftCorner = equal(bottomNeighbour, "0") && equal(leftNeighbour, "0");

        // if only one node in network
        if (topRightCorner && bottomLeftCorner) {
            return 1;
        }

        // Your initialization code goes here
        Integer count = null;

        // If the network is linear (single row or column), either width or height will be 1
        if ((topLeftCorner && bottomLeftCorner) || (topLeftCorner && topRightCorner)) {
            count = 1;
        }

        MyMessage message = null;

        // If this machine is the top right corner one and there is more than one column in the network
        // create a message w/ direction = left to calculate the width (moves from top right to top left corner)
        if (topRightCorner && !topLeftCorner) {
            message = new MyMessage(0, Direction.L);
        }

        // If this machine is the bottom left corner one and there is more than one row in the network
        // create a message w/ direction = top to calculate the height (moves from bottom left to top left corner)
        if (bottomLeftCorner && !topLeftCorner) {
            message = new MyMessage(0, Direction.T);
        }

        try {
            while (waitForNextRound()) { // Main loop. All processors wait here for the beginning of the next round.
                // Your code goes here   

                if (message != null) {

                    if (topLeftCorner) {
                        message.incrementCount();

                        // if this is the first number we've received (either width or height), keep it in count variable
                        if (count == null) {
                            count = message.count;
                        }

                        // if we already have one of width or height, and now received the other one, we can multiply them to get the count
                        else {
                            message.count *= count;

                            // now that we have the total count, broadcast it to right and bottom neighbours (if they exist)
                            if (!topRightCorner) {
                                message.direction = Direction.R;
                                send(makeMessage(rightNeighbour, message.encode()));
                            }

                            if (!bottomLeftCorner) {
                                message.direction = Direction.B;
                                send(makeMessage(bottomNeighbour, message.encode()));
                            }

                            // TERMINATE
                            return message.count;
                        }
                    }

                    // If the message has direction = left, it means we're calculating the width so increment and pass to left neighbour
                    else if (message.direction.equals(Direction.L)) {
                        message.incrementCount();
                        send(makeMessage(leftNeighbour, message.encode()));
                    }

                    // If the message has direction = top, it means we're calculating the height so increment and pass to top neighbour
                    else if (message.direction.equals(Direction.T)) {
                        message.incrementCount();
                        send(makeMessage(topNeighbour, message.encode()));
                    }

                    // If we reach this code, we're in the process of broadcasting the count to all the nodes
                    // Pass the message to right and bottom neighbours, and then terminate
                    else {
                        if (!equal(rightNeighbour, "0")) {
                            send(makeMessage(rightNeighbour, message.encode()));
                        }
                        if (!equal(bottomNeighbour, "0") && equal(leftNeighbour, "0")) {
                            message.direction = Direction.B;
                            send(makeMessage(bottomNeighbour, message.encode()));
                        }
                        return message.count;
                    }
                }

                // RECEIVE
                final Message receivedMessage = receive();
                message = generateMessage(receivedMessage);
            }
        } catch(SimulatorException e){
            System.out.println("ERROR: " + e.toString());
        }
    
        // If we got here, something went wrong! (Exception, node failed, etc.)
        return 0;
    }
}
