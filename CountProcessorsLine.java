import java.util.Vector;

/* Algorithm for counting the number of processors in a line network. */

public class CountProcessorsLine extends Algorithm {

    private static final String SEPARATOR = "-";

    private enum Direction {
        R,
        L,
    }

    /* Do not modify this method */
    public Object run() {
        int status = countProcessors(getID());
        return status;
    }

    /* The format of the message that will be transmitted between nodes
     * count is the count of processors
     * direction is either R or L, which is used to know whether to send the message to right or left neighbour
     */
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

        public int getCount() {
            return count;
        }

        public void setDirection(final Direction direction) {
            this.direction = direction;
        }
    }

    private MyMessage convertToMyMessage(final Message message) {
        if (message == null) return null;
        final String[] messageParts = message.data()
                                             .split(SEPARATOR);
        return new MyMessage(Integer.parseInt(messageParts[0]), Direction.valueOf(messageParts[1]));
    }

    public int countProcessors(String id) {
        Vector<String> v = neighbours(); // Set of neighbours of this node.

        final String leftNeighbour = (String) v.elementAt(0);
        final String rightNeighbour = (String) v.elementAt(1);

        final boolean leftmostProcessor = equal(leftNeighbour, "0");
        final boolean rightmostProcessor = equal(rightNeighbour, "0");

        // handle the special case where no messages need to be sent (i.e. only one node in network)
        if (leftmostProcessor && rightmostProcessor) return 1;

        // Your initialization code goes here
        Integer count = null;
        MyMessage message = null;
        // the left most processor will start sending messages
        if (leftmostProcessor) {
            message = new MyMessage(0, Direction.R);
        }

        try {
            while (waitForNextRound()) { // Main loop. All processors wait here for the beginning of the next round.

                // SEND: if a message was received
                if (message != null) {

                    // if we are on the way to the right (still incrementing the counter)
                    if (message.direction.equals(Direction.R)) {
                        message.incrementCount();

                        // if the node is the rightmost processor and has received the count, it should change direction
                        if (rightmostProcessor) {
                            message.setDirection(Direction.L);
                            send(makeMessage(leftNeighbour, message.encode()));
                            count = message.getCount();
                        } else {
                            send(makeMessage(rightNeighbour, message.encode()));
                        }
                    }

                    // if we are on the way to the left, the count is already determined
                    // Just pass the count to the left neighbour
                    else {
                        if (!leftmostProcessor) {
                            send(makeMessage(leftNeighbour, message.encode()));
                        }
                        count = message.getCount();
                    }
                }

                // RECEIVE: receive a message
                final Message receivedMessage = receive();
                message = convertToMyMessage(receivedMessage);

                // TERMINATE: if the count of the processors has already been received
                if (count != null) return count;
            }
        } catch (SimulatorException e) {
            System.out.println("ERROR: " + e.toString());
        }

        // If we got here, something went wrong! (Exception, node failed, etc.)    
        return 0;
    }
}
