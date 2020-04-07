package src.rdmEvents;

public class Swap {
    int originalPlayer;
    int originalPlayerCardNumber;
    int rdmPlayerIndex;
    int rdmPlayerCardNumber;
    String status;

    public Swap(int currentPlayer, int currentPlayerCardNumber, int rdmPlayerIndex, int rdmPlayerCardNumber, String status) {
        this.originalPlayer = currentPlayer;
        this.originalPlayerCardNumber = currentPlayerCardNumber;
        this.rdmPlayerIndex = rdmPlayerIndex;
        this.rdmPlayerCardNumber = rdmPlayerCardNumber;
        this.status = status;
    }

    public int getOriginalPlayer() {
        return originalPlayer;
    }

    public void setOriginalPlayer(int originalPlayer) {
        this.originalPlayer = originalPlayer;
    }

    public int getOriginalPlayerCardNumber() {
        return originalPlayerCardNumber;
    }

    public void setOriginalPlayerCardNumber(int originalPlayerCardNumber) {
        this.originalPlayerCardNumber = originalPlayerCardNumber;
    }

    public int getRdmPlayerIndex() {
        return rdmPlayerIndex;
    }

    public void setRdmPlayerIndex(int rdmPlayerIndex) {
        this.rdmPlayerIndex = rdmPlayerIndex;
    }

    public int getRdmPlayerCardNumber() {
        return rdmPlayerCardNumber;
    }

    public void setRdmPlayerCardNumber(int rdmPlayerCardNumber) {
        this.rdmPlayerCardNumber = rdmPlayerCardNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
