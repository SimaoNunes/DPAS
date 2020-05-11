package library;

import java.io.Serializable;

public class Quadruplet<F, S, T, FO> implements Serializable {

    private F first;
    private S second;
    private T third;
    private FO fourth;

    public Quadruplet(F first, S second, T third, FO fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public T getThird() {
        return third;
    }

    public void setThird(T third) {
        this.third = third;
    }

    public FO getFourth() {
        return fourth;
    }

    public void setFourth(FO fourth) {
        this.fourth = fourth;
    }
}
