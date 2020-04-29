package src.rdmEvents;

//Class to store a card swap event to be sent over network
public class Swap {
    //Player and the selected card who initiated the swap
    int originalPlayerIndex;
    int originalPlayerCardNumber;
    //Player and the selected card who the swap is performed on
    int otherPlayerIndex;
    int otherPlayerCardNumber;
    //Status indicates whether the player chose to swap
    String status;

    //Standard constructor
    public Swap(int currentPlayer, int currentPlayerCardNumber, int rdmPlayerIndex, int rdmPlayerCardNumber, String status) {
        this.originalPlayerIndex = currentPlayer;
        this.originalPlayerCardNumber = currentPlayerCardNumber;
        this.otherPlayerIndex = rdmPlayerIndex;
        this.otherPlayerCardNumber = rdmPlayerCardNumber;
        this.status = status;
    }

    //Standard getters

    public int getOriginalPlayerIndex() {
        return originalPlayerIndex;
    }

    public int getOriginalPlayerCardNumber() {
        return originalPlayerCardNumber;
    }

    public int getOtherPlayerIndex() {
        return otherPlayerIndex;
    }

    public int getOtherPlayerCardNumber() {
        return otherPlayerCardNumber;
    }

    public String getStatus() {
        return status;
    }
}
