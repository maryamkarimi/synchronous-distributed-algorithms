import java.util.Vector; 

/* Algorithm for counting the number of processors with even ID in a synchronous ring network. */

public class CountLargerID extends Algorithm {

    private static final String SEPARATOR = "-";

    private static class MyMessage {
        int id;
        int count;

        public MyMessage(final int id, final int count) {
            this.id = id;
            this.count = count;
        }

        public String encode() {
            return id + SEPARATOR + count;
        }
    }

    private MyMessage generateMessage(Message message) {
        if (message == null) return null;
        final String[] messageParts = message.data().split(SEPARATOR);
        return new MyMessage(Integer.parseInt(messageParts[0]), Integer.parseInt(messageParts[1]));
    }

    /* Do not modify this method */
    public Object run() {
		int status = countLargerIDs(getID());
        return status;
    }
 
    public int countLargerIDs(String id) {
        final int currentId = Integer.parseInt(id);

        Vector<String> v = neighbours(); // Set of neighbours of this node.
        String rightNeighbour = (String) v.elementAt(1); // Neighbour on the right

        // Your initialization code goes here
        MyMessage message = new MyMessage(currentId, 0);

        try {
            while (waitForNextRound()) { // Main loop. All processors wait here for the beginning of the next round.

                // SEND
                if (message != null) {

                    // Update the count of the message if the current id is greater than the id of the message
                    if (currentId > message.id) {
                        message.count = message.count + 1;
                    }

                    // Send message to right neighbour
                    send(makeMessage(rightNeighbour, message.encode()));
                }

                // RECEIVE
                final Message receivedMessage = receive();
                message = generateMessage(receivedMessage);

                // TERMINATE
                if (message != null && message.id == currentId) {
                    return message.count;
                }
            }
        } catch(SimulatorException e){
            System.out.println("ERROR: " + e.toString());
        }   
        // If we got here, something went wrong! (Exception, node failed, etc.)
		return 0;
    }
}
