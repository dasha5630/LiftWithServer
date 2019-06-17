package lift;


import java.beans.*;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *  Class Lift describe logic of elevator's movement, and notify its current location
 */

public class Lift {

    private ConcurrentLinkedDeque<Integer> queue = new ConcurrentLinkedDeque<>();

    private enum Dir {UP, DOWN, STAND}


    private final Integer numberOfFoors;
    private final Integer numberOfFirstFoor;
    private Integer currentFloor;

    private final long creationTime;
    private final long period; // time to reach next floor
    private long startTime;
    private long currentTime;



    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public Lift() {
        this.numberOfFoors = 7; // max number of floors in this bilding
        this.currentFloor = 7; //lift stay on max floor
        this.numberOfFirstFoor = 1;
        this.creationTime = System.currentTimeMillis();
        this.period = 10000L;
    }

    public Integer getCurrentFloor() {
        return currentFloor;
    }

    public Integer getNumberOfFirstFoor() {
        return numberOfFirstFoor;
    }

    public Integer getNumberOfFoors() {
        return numberOfFoors;
    }

    public void setCurrentFloor(Integer currentFloor) {
        this.currentFloor = currentFloor;
    }

    public boolean addRequestFloor(Integer requestedFloor) {
        if (!queue.isEmpty() && requestedFloor < currentFloor
                && requestedFloor > queue.poll()
                && getDirection() == Dir.DOWN )
        {
            return queue.offerFirst(requestedFloor);
        }
        else return queue.offerLast(requestedFloor);
    }

    public Dir getDirection() {

        if (currentFloor < queue.peek()) {
            return Dir.UP;
        } else if (currentFloor > queue.peek()) {
            return Dir.DOWN;
        }
        return Dir.STAND;
    }

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }


    public void moveOneStepUp() {
             if (currentFloor < numberOfFoors) {
                Integer temp = 0;
                setCurrentFloor(++currentFloor);
                support.firePropertyChange("currentFloor", temp, currentFloor);
        }
    }

    public void moveOneStepDown() {

        if (currentFloor > 0) {
            Integer temp = currentFloor;
            setCurrentFloor(--currentFloor);
            support.firePropertyChange("currentFloor",temp, currentFloor);

        }
    }

    public String toString() {
        return new StringBuilder()
                .append(getDirection())
                .append("requestedFloor")
                .append(queue.peek())
                .append("currentFloor")
                .append(currentFloor)
                .toString();
    }


    public void call(Integer floor){

        addRequestFloor(floor);

        currentTime = System.currentTimeMillis();
        startTime = System.currentTimeMillis();

        if (!queue.isEmpty()){
        while (currentFloor != queue.peek()) {

            currentTime = System.currentTimeMillis();
                switch (getDirection()) {
                    case UP:

                        if (currentTime - startTime >= period) {
                            moveOneStepUp();
                            startTime = System.currentTimeMillis();
                        }

                        break;
                    case DOWN:
                        if (currentTime - startTime >= period) {
                            moveOneStepDown();
                            startTime = System.currentTimeMillis();
                        }
                        break;
                }

        }
        queue.removeFirst();
        }
    }
}
